package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.dto.RefundDTO;
import com.eshop.dto.RefundHandleDTO;
import com.eshop.entity.Refund;
import com.eshop.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public Result<?> applyRefund(Authentication authentication, @Valid @RequestBody RefundDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        refundService.applyRefund(userId, dto);
        return Result.success("退款申请已提交");
    }

    @GetMapping("/user")
    public Result<List<Refund>> getUserRefunds(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(refundService.getUserRefunds(userId));
    }

    @GetMapping("/merchant")
    public Result<List<Refund>> getMerchantRefunds(Authentication authentication) {
        Long merchantId = (Long) authentication.getPrincipal();
        return Result.success(refundService.getMerchantRefunds(merchantId));
    }

    @PostMapping("/handle")
    public Result<?> handleRefund(Authentication authentication, @Valid @RequestBody RefundHandleDTO dto) {
        Long merchantId = (Long) authentication.getPrincipal();
        refundService.handleRefund(merchantId, dto.getRefundId(), dto.getApproved(), dto.getRejectReason());
        return Result.success(dto.getApproved() ? "已同意退款" : "已拒绝退款");
    }
}
