package com.movie.service;

import com.movie.dto.VipProductVO;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    /**
     * 获取VIP套餐列表
     */
    List<VipProductVO> getProducts();

    /**
     * 创建支付订单
     * @param userId 用户ID
     * @param productId 套餐ID
     * @return 支付宝form表单字符串
     */
    String createOrder(Long userId, String productId);

    /**
     * 处理支付宝异步通知
     * @param params 通知参数
     * @return success/fail
     */
    String handleNotify(Map<String, String> params);

    /**
     * 查询订单支付状态
     */
    String getPaymentStatus(String orderNo);
}
