package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.dto.CartAddDTO;
import com.eshop.service.CartService;
import com.eshop.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartVO>> getCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(cartService.getCart(userId));
    }

    @PostMapping
    public Result<?> addToCart(Authentication authentication, @Valid @RequestBody CartAddDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.addToCart(userId, dto);
        return Result.success("已加入购物车");
    }

    @PutMapping("/{id}/quantity")
    public Result<?> updateQuantity(Authentication authentication,
                                     @PathVariable Long id, @RequestParam Integer quantity) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.updateQuantity(userId, id, quantity);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/selected")
    public Result<?> updateSelected(Authentication authentication,
                                     @PathVariable Long id, @RequestParam Integer selected) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.updateSelected(userId, id, selected);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> removeItem(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.removeItem(userId, id);
        return Result.success("已删除");
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }
}
