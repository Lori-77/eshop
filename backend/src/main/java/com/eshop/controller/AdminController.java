package com.eshop.controller;

import com.eshop.common.PageResult;
import com.eshop.common.Result;
import com.eshop.dto.StatusDTO;
import com.eshop.service.AdminService;
import com.eshop.vo.StatisticsVO;
import com.eshop.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 用户管理
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUsers(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.getUsers(keyword, "USER", status, page, size));
    }

    @PutMapping("/users/status")
    public Result<?> updateUserStatus(@Valid @RequestBody StatusDTO dto) {
        adminService.updateUserStatus(dto.getId(), dto.getStatus());
        return Result.success(dto.getStatus() == 1 ? "已解冻" : "已冻结");
    }

    // 商家管理
    @GetMapping("/merchants")
    public Result<PageResult<UserVO>> getMerchants(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.getMerchants(keyword, status, page, size));
    }

    @PutMapping("/merchants/status")
    public Result<?> updateMerchantStatus(@Valid @RequestBody StatusDTO dto) {
        adminService.updateMerchantStatus(dto.getId(), dto.getStatus());
        return Result.success(dto.getStatus() == 1 ? "已解冻" : "已冻结");
    }

    // 数据统计
    @GetMapping("/stats")
    public Result<StatisticsVO> getStats() {
        return Result.success(adminService.getStats());
    }

    @GetMapping("/chart")
    public Result<Object> getChartData(@RequestParam(defaultValue = "order") String type) {
        return Result.success(adminService.getChartData(type));
    }
}
