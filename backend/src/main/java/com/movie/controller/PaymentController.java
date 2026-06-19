package com.movie.controller;

import com.movie.common.JwtUtil;
import com.movie.common.Result;
import com.movie.dto.VipProductVO;
import com.movie.service.PaymentService;
import com.movie.service.impl.PaymentServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取VIP套餐列表
     */
    @GetMapping("/products")
    public Result<List<VipProductVO>> getProducts() {
        return Result.success(paymentService.getProducts());
    }

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result<Map<String, String>> createOrder(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> body) {
        String productId = body.get("productId");
        String form = paymentService.createOrder(userId, productId);
        Map<String, String> data = new HashMap<>();
        data.put("form", form);
        return Result.success(data);
    }

    /**
     * 模拟支付成功（开发测试用，自动将PENDING订单置为SUCCESS）
     * 返回新生成的VIP角色JWT Token，前端需替换旧token
     */
    @PostMapping("/mock/{orderNo}")
    public Result<Map<String, Object>> mockPay(@PathVariable String orderNo) {
        String newToken = null;
        if (paymentService instanceof PaymentServiceImpl) {
            newToken = ((PaymentServiceImpl) paymentService).mockPaySuccess(orderNo);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("status", "SUCCESS");
        if (newToken != null) {
            data.put("token", newToken);
            // 将新Token存入Redis
            Long userId = jwtUtil.getUserIdFromToken(newToken);
            redisTemplate.opsForValue().set("user:token:" + newToken, userId, 24, TimeUnit.HOURS);
        }
        return Result.success(data);
    }

    /**
     * 支付宝异步通知
     */
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> params.put(key, value[0]));
        return paymentService.handleNotify(params);
    }

    /**
     * 支付宝同步跳转（携带订单号传给前端结果页，自动刷新VIP状态）
     */
    @GetMapping("/return")
    public void handleReturn(@RequestParam(required = false) String out_trade_no,
                             @RequestParam(required = false) String trade_no,
                             HttpServletResponse response) throws IOException {
        if (out_trade_no != null) {
            // 支付宝回调携带了订单号，传给前端结果页自动mock支付
            response.sendRedirect("http://localhost:5173/payment/result?orderNo=" + out_trade_no);
        } else {
            response.sendRedirect("http://localhost:5173/payment/result");
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    public Result<Map<String, String>> getStatus(@PathVariable String orderNo) {
        String status = paymentService.getPaymentStatus(orderNo);
        Map<String, String> data = new HashMap<>();
        data.put("status", status);
        return Result.success(data);
    }
}
