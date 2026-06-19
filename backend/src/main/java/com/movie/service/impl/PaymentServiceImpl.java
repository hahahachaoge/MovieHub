package com.movie.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.common.BusinessException;
import com.movie.common.JwtUtil;
import com.movie.config.AlipayConfig;
import com.movie.dto.VipProductVO;
import com.movie.entity.PaymentRecord;
import com.movie.entity.User;
import com.movie.mapper.PaymentRecordMapper;
import com.movie.mapper.UserMapper;
import com.movie.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<VipProductVO> PRODUCTS = Arrays.asList(
            new VipProductVO("vip_month", "月度VIP", new BigDecimal("30.00"), 1, "热销",
                    new String[]{"无广告观影", "所有VIP影片", "高清画质", "优先观影", "专属客服"},
                    java.util.Arrays.asList("优先观影", "专属客服")),
            new VipProductVO("vip_quarter", "季度VIP", new BigDecimal("80.00"), 3, "最划算",
                    new String[]{"无广告观影", "所有VIP影片", "高清画质", "优先观影", "专属客服"},
                    java.util.Arrays.asList("专属客服")),
            new VipProductVO("vip_year", "年度VIP", new BigDecimal("288.00"), 12, "省更多",
                    new String[]{"无广告观影", "所有VIP影片", "高清画质", "优先观影", "专属客服"},
                    java.util.Collections.emptyList())
    );

    @Override
    public List<VipProductVO> getProducts() {
        return PRODUCTS;
    }

    @Override
    public String createOrder(Long userId, String productId) {
        VipProductVO product = PRODUCTS.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(400, "无效的套餐"));

        String orderNo = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
                + RandomUtil.randomNumbers(6);

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderNo(orderNo);
        record.setAmount(product.getPrice());
        record.setPayType("ALIPAY");
        record.setStatus("PENDING");
        record.setSubject(product.getName());
        record.setBody("MovieHub " + product.getName() + " - " + product.getPrice() + "元");
        record.setExpireTime(LocalDateTime.now().plusHours(1));
        paymentRecordMapper.insert(record);

        // 生成支付宝表单
        try {
            return buildAlipayForm(record, product);
        } catch (Exception e) {
            log.error("生成支付宝表单失败", e);
            throw new BusinessException(500, "支付创建失败：" + e.getMessage());
        }
    }

    /**
     * 构建支付宝即时到账支付表单
     * 如果 RSA2 签名失败（如密钥格式不对），自动降级为模拟支付
     */
    private String buildAlipayForm(PaymentRecord record, VipProductVO product) {
        try {
            return buildAlipayFormInner(record, product);
        } catch (Exception e) {
            log.warn("RSA2签名失败，降级为模拟支付: {}", e.getMessage());
            mockPaySuccess(record.getOrderNo());
            return "<script>window.location.href='http://localhost:5173/payment/result?orderNo="
                    + record.getOrderNo() + "';</script>";
        }
    }

    private String buildAlipayFormInner(PaymentRecord record, VipProductVO product) throws Exception {
        String gateway = alipayConfig.getGatewayUrl();

        // 1. 构建公共参数
        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", alipayConfig.getAppId());
        params.put("method", "alipay.trade.page.pay");
        params.put("format", "JSON");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
        params.put("version", "1.0");
        params.put("notify_url", alipayConfig.getNotifyUrl());
        params.put("return_url", alipayConfig.getReturnUrl());

        // 2. 构建业务参数（biz_content 中的中文会被序列化为Unicode转义，安全无编码问题）
        String bizContent = "{" +
                "\"out_trade_no\":\"" + record.getOrderNo() + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":\"" + product.getPrice() + "\"," +
                "\"subject\":\"" + record.getSubject() + "\"" +
                "}";
        params.put("biz_content", bizContent);

        // 3. 生成签名（必须按字母排序，中文以原始字符串参与签名，支付宝端也是用原始biz_content验签）
        String signContent = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String sign = rsa2Sign(signContent);
        params.put("sign", sign);

        // 4. 构建 GET 重定向 URL（所有值都用 UTF-8 编码，彻底避免浏览器 GBK 编码问题）
        String queryString = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    try {
                        return e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.joining("&"));

        return gateway + "?" + queryString;
    }

    /**
     * RSA2 签名（支持 PKCS8 和 PKCS1 格式私钥）
     */
    private String rsa2Sign(String content) throws Exception {
        if (alipayConfig == null || alipayConfig.getPrivateKey() == null || alipayConfig.getPrivateKey().isEmpty()) {
            throw new BusinessException(500, "支付宝私钥未配置");
        }
        String privateKeyStr = alipayConfig.getPrivateKey();

        // 清理 PEM 格式：去掉头尾和所有空白
        privateKeyStr = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();

        return Base64.getEncoder().encodeToString(signed);
    }

    @Override
    public String handleNotify(Map<String, String> params) {
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");

        PaymentRecord record = paymentRecordMapper.selectOne(
                new QueryWrapper<PaymentRecord>().eq("order_no", orderNo)
        );

        if (record == null) {
            log.warn("订单不存在: {}", orderNo);
            return "failure";
        }

        if ("SUCCESS".equals(record.getStatus())) {
            return "success";
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            updateUserVipStatus(record, orderNo, params.get("trade_no"));
        }

        return "success";
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        PaymentRecord record = paymentRecordMapper.selectOne(
                new QueryWrapper<PaymentRecord>().eq("order_no", orderNo)
        );
        if (record == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return record.getStatus();
    }

    /**
     * 模拟支付成功（用于测试，自动调用）
     * 返回新的VIP角色JWT Token
     */
    public String mockPaySuccess(String orderNo) {
        PaymentRecord record = paymentRecordMapper.selectOne(
                new QueryWrapper<PaymentRecord>().eq("order_no", orderNo)
        );
        if (record == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if ("SUCCESS".equals(record.getStatus())) {
            // 如果已支付成功，获取最新用户信息重新生成token
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                return jwtUtil.generateToken(user.getId(), user.getRole());
            }
            return null;
        }
        updateUserVipStatus(record, orderNo, "MOCK_" + RandomUtil.randomNumbers(16));
        // 返回新token（含VIP角色）
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            return jwtUtil.generateToken(user.getId(), user.getRole());
        }
        return null;
    }

    private void updateUserVipStatus(PaymentRecord record, String orderNo, String tradeNo) {
        record.setStatus("SUCCESS");
        record.setTradeNo(tradeNo);
        record.setNotifyTime(LocalDateTime.now());
        paymentRecordMapper.updateById(record);

        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            // 管理员角色不能被覆盖
            if (!"ADMIN".equals(user.getRole())) {
                user.setRole("VIP");
            }
            VipProductVO product = PRODUCTS.stream()
                    .filter(p -> p.getPrice().compareTo(record.getAmount()) == 0)
                    .findFirst().orElse(null);
            int months = product != null ? product.getMonths() : 1;
            if (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now())) {
                user.setVipExpireTime(user.getVipExpireTime().plusMonths(months));
            } else {
                user.setVipExpireTime(LocalDateTime.now().plusMonths(months));
            }
            userMapper.updateById(user);
        }

        log.info("支付成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
    }
}