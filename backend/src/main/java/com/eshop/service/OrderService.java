package com.eshop.service;

import com.eshop.common.PageResult;
import com.eshop.dto.OrderSubmitDTO;
import com.eshop.vo.OrderVO;

public interface OrderService {
    OrderVO submitOrder(Long userId, OrderSubmitDTO dto);
    PageResult<OrderVO> getUserOrders(Long userId, String status, Integer page, Integer size);
    PageResult<OrderVO> getMerchantOrders(Long merchantId, String status, Integer page, Integer size);
    OrderVO getOrderDetail(Long orderId);
    void cancelOrder(Long userId, Long orderId);
    void confirmReceive(Long userId, Long orderId);
}
