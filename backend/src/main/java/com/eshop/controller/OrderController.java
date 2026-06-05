package com.eshop.controller;

import com.eshop.common.PageResult;
import com.eshop.common.Result;
import com.eshop.dto.OrderSubmitDTO;
import com.eshop.service.OrderService;
import com.eshop.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> submitOrder(Authentication authentication, @Valid @RequestBody OrderSubmitDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success("下单成功", orderService.submitOrder(userId, dto));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> getUserOrders(Authentication authentication,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.getUserOrders(userId, status, page, size));
    }

    @GetMapping("/merchant")
    public Result<PageResult<OrderVO>> getMerchantOrders(Authentication authentication,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        Long merchantId = (Long) authentication.getPrincipal();
        return Result.success(orderService.getMerchantOrders(merchantId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.cancelOrder(userId, id);
        return Result.success("订单已取消");
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceive(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.confirmReceive(userId, id);
        return Result.success("确认收货成功");
    }
}
