package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.common.BusinessException;
import com.eshop.dto.ShipDTO;
import com.eshop.entity.Logistics;
import com.eshop.entity.Order;
import com.eshop.entity.Product;
import com.eshop.entity.User;
import com.eshop.mapper.LogisticsMapper;
import com.eshop.mapper.OrderMapper;
import com.eshop.mapper.ProductMapper;
import com.eshop.mapper.UserMapper;
import com.eshop.service.MerchantService;
import com.eshop.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final OrderMapper orderMapper;
    private final LogisticsMapper logisticsMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void shipOrder(Long merchantId, ShipDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_DELIVERY".equals(order.getStatus())) {
            throw new BusinessException("只能对待发货订单操作");
        }

        // 创建物流记录
        Logistics logistics = new Logistics();
        logistics.setOrderId(order.getId());
        logistics.setOrderNo(order.getOrderNo());
        logistics.setCompany(dto.getCompany());
        logistics.setTrackingNo(dto.getTrackingNo());
        logistics.setStatus("IN_TRANSIT");
        logisticsMapper.insert(logistics);

        // 更新订单状态
        order.setStatus("DELIVERED");
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public StatisticsVO getMerchantStats(Long merchantId) {
        Map<String, Object> stats = orderMapper.selectStats(merchantId);

        long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId)
        );

        return StatisticsVO.builder()
                .totalOrders(stats.get("total_orders") != null ? ((Number) stats.get("total_orders")).longValue() : 0)
                .totalSales(stats.get("total_sales") != null ? new BigDecimal(stats.get("total_sales").toString()) : BigDecimal.ZERO)
                .pendingOrders(stats.get("pending_delivery") != null ? ((Number) stats.get("pending_delivery")).longValue() : 0)
                .pendingRefunds(stats.get("refunding") != null ? ((Number) stats.get("refunding")).longValue() : 0)
                .totalProducts(productCount)
                .build();
    }
}
