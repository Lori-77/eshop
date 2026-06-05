package com.eshop.service.impl;

import com.eshop.common.BusinessException;
import com.eshop.dto.PayDTO;
import com.eshop.entity.Order;
import com.eshop.entity.Payment;
import com.eshop.mapper.OrderMapper;
import com.eshop.mapper.PaymentMapper;
import com.eshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public void pay(Long userId, PayDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("该订单无需支付");
        }

        // 模拟支付：直接支付成功
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(order.getTotalAmount());
        payment.setPayMethod(dto.getPayMethod() != null ? dto.getPayMethod() : "ALIPAY");
        payment.setStatus("SUCCESS");
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        // 更新订单状态
        order.setStatus("PENDING_DELIVERY");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
}
