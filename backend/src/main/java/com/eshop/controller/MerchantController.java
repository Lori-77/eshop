package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.dto.ShipDTO;
import com.eshop.service.MerchantService;
import com.eshop.vo.StatisticsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/ship")
    public Result<?> shipOrder(Authentication authentication, @Valid @RequestBody ShipDTO dto) {
        Long merchantId = (Long) authentication.getPrincipal();
        merchantService.shipOrder(merchantId, dto);
        return Result.success("发货成功");
    }

    @GetMapping("/stats")
    public Result<StatisticsVO> getStats(Authentication authentication) {
        Long merchantId = (Long) authentication.getPrincipal();
        return Result.success(merchantService.getMerchantStats(merchantId));
    }
}
