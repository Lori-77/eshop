package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.common.BusinessException;
import com.eshop.common.PageResult;
import com.eshop.dto.OrderSubmitDTO;
import com.eshop.dto.OrderSubmitDTO.OrderItemDTO;
import com.eshop.entity.*;
import com.eshop.mapper.*;
import com.eshop.service.OrderService;
import com.eshop.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartItemMapper cartItemMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public OrderVO submitOrder(Long userId, OrderSubmitDTO dto) {
        // 获取地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        // 获取选中的购物车项
        List<CartItem> selectedItems;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            // 从指定商品下单（立即购买）
            selectedItems = new ArrayList<>();
            for (OrderItemDTO item : dto.getItems()) {
                CartItem ci = new CartItem();
                ci.setUserId(userId);
                ci.setProductId(item.getProductId());
                ci.setQuantity(item.getQuantity());
                selectedItems.add(ci);
            }
        } else {
            // 从购物车中获取选中的商品
            selectedItems = cartItemMapper.selectList(
                    new LambdaQueryWrapper<CartItem>()
                            .eq(CartItem::getUserId, userId)
                            .eq(CartItem::getSelected, 1)
            );
        }

        if (selectedItems.isEmpty()) {
            throw new BusinessException("没有要结算的商品");
        }

        // 按商家分组（本次简化：所有商品属于同一商家，或分订单）
        Long merchantId = null;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : selectedItems) {
            Product product = productMapper.selectById(ci.getProductId());
            if (product == null || !"ON_SALE".equals(product.getStatus())) {
                throw new BusinessException("商品【" + (product != null ? product.getName() : "未知") + "】已下架");
            }
            if (ci.getQuantity() > product.getStock()) {
                throw new BusinessException("商品【" + product.getName() + "】库存不足");
            }

            if (merchantId == null) {
                merchantId = product.getMerchantId();
            }

            // 扣库存
            product.setStock(product.getStock() - ci.getQuantity());
            product.setSales((product.getSales() == null ? 0 : product.getSales()) + ci.getQuantity());
            productMapper.updateById(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setProductImage(product.getMainImage());
            oi.setPrice(product.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setTotalAmount(itemTotal);
            orderItems.add(oi);
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setReceiverName(address.getReceiverName());
        order.setPhone(address.getPhone());
        order.setAddress(address.getProvince() + address.getCity() + address.getDistrict() + " " + address.getDetail());
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 保存订单明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 清除购物车中已下单的商品
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            List<Long> productIds = selectedItems.stream().map(CartItem::getProductId).toList();
            if (!productIds.isEmpty()) {
                cartItemMapper.delete(
                        new LambdaQueryWrapper<CartItem>()
                                .eq(CartItem::getUserId, userId)
                                .in(CartItem::getProductId, productIds)
                );
            }
        }

        return orderMapper.selectOrderDetail(order.getId());
    }

    @Override
    public PageResult<OrderVO> getUserOrders(Long userId, String status, Integer page, Integer size) {
        Page<OrderVO> p = new Page<>(page, size);
        Page<OrderVO> result = orderMapper.selectOrderPage(p, userId, null, status);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public PageResult<OrderVO> getMerchantOrders(Long merchantId, String status, Integer page, Integer size) {
        Page<OrderVO> p = new Page<>(page, size);
        Page<OrderVO> result = orderMapper.selectOrderPage(p, null, merchantId, status);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        OrderVO vo = orderMapper.selectOrderDetail(orderId);
        if (vo == null) {
            throw new BusinessException("订单不存在");
        }
        return vo;
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付的订单");
        }

        order.setStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 恢复库存
        restoreStock(orderId);
    }

    @Override
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new BusinessException("只能确认已发货的订单");
        }

        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return datePart + randomPart;
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, (product.getSales() == null ? 0 : product.getSales()) - item.getQuantity()));
                productMapper.updateById(product);
            }
        }
    }
}
