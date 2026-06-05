package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.dto.AddressDTO;
import com.eshop.entity.Address;
import com.eshop.service.UserService;
import com.eshop.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserVO> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(Authentication authentication, @RequestBody UserVO vo) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateProfile(userId, vo);
        return Result.success("更新成功");
    }

    @GetMapping("/addresses")
    public Result<List<Address>> getAddresses(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getAddresses(userId));
    }

    @GetMapping("/addresses/{id}")
    public Result<Address> getAddress(@PathVariable Long id) {
        return Result.success(userService.getAddressById(id));
    }

    @PostMapping("/addresses")
    public Result<?> saveAddress(Authentication authentication, @Valid @RequestBody AddressDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        userService.saveAddress(userId, dto);
        return Result.success("添加成功");
    }

    @PutMapping("/addresses")
    public Result<?> updateAddress(Authentication authentication, @Valid @RequestBody AddressDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateAddress(userId, dto);
        return Result.success("修改成功");
    }

    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAddress(userId, id);
        return Result.success("删除成功");
    }
}
