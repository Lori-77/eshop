package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.common.BusinessException;
import com.eshop.dto.RefundDTO;
import com.eshop.entity.Order;
import com.eshop.entity.Refund;
import com.eshop.mapper.OrderMapper;
import com.eshop.mapper.RefundMapper;
import com.eshop.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public void applyRefund(Long userId, RefundDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_DELIVERY".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不支持退款");
        }
        // 已完成订单需在7天内
        if ("COMPLETED".equals(order.getStatus())) {
            if (order.getCompleteTime() != null
                    && order.getCompleteTime().plusDays(7).isBefore(LocalDateTime.now())) {
                throw new BusinessException("已超过7天退款期限");
            }
        }

        // 检查是否已有退款申请
        Long count = refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getOrderId, order.getId())
                        .ne(Refund::getStatus, "REJECTED")
        );
        if (count > 0) {
            throw new BusinessException("已有退款申请在处理中");
        }

        Refund refund = new Refund();
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setMerchantId(order.getMerchantId());
        refund.setAmount(order.getTotalAmount());
        refund.setReason(dto.getReason());
        refund.setStatus("PENDING");
        refundMapper.insert(refund);

        // 更新订单状态
        order.setStatus("REFUNDING");
        orderMapper.updateById(order);
    }

    @Override
    public List<Refund> getUserRefunds(Long userId) {
        return refundMapper.selectList(
                new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getUserId, userId)
                        .orderByDesc(Refund::getCreatedAt)
        );
    }

    @Override
    public List<Refund> getMerchantRefunds(Long merchantId) {
        return refundMapper.selectList(
                new LambdaQueryWrapper<Refund>()
                        .eq(Refund::getMerchantId, merchantId)
                        .orderByDesc(Refund::getCreatedAt)
        );
    }

    @Override
    @Transactional
    public void handleRefund(Long merchantId, Long refundId, Boolean approved, String rejectReason) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null || !refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException("退款申请不存在");
        }
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BusinessException("该退款申请已处理");
        }

        if (approved) {
            refund.setStatus("APPROVED");
            // 更新订单状态
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                order.setStatus("REFUNDED");
                orderMapper.updateById(order);
            }
        } else {
            refund.setStatus("REJECTED");
            refund.setRejectReason(rejectReason);
            // 恢复订单状态
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                // 恢复为之前的正常状态
                if (order.getDeliveryTime() != null) {
                    order.setStatus("DELIVERED");
                } else if (order.getPayTime() != null) {
                    order.setStatus("PENDING_DELIVERY");
                }
                orderMapper.updateById(order);
            }
        }
        refund.setResultTime(LocalDateTime.now());
        refundMapper.updateById(refund);
    }
}
