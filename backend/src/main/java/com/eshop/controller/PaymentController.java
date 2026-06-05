package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.dto.PayDTO;
import com.eshop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public Result<?> pay(Authentication authentication, @Valid @RequestBody PayDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        paymentService.pay(userId, dto);
        return Result.success("支付成功");
    }
}
