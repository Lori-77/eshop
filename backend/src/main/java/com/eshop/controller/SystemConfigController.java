package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.entity.SystemConfig;
import com.eshop.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SystemConfig>> getAllConfigs() {
        return Result.success(systemConfigService.getAllConfigs());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateConfig(@PathVariable Long id, @RequestParam String value) {
        systemConfigService.updateConfig(id, value);
        return Result.success("更新成功");
    }
}
