package com.eshop.service;

import com.eshop.dto.CartAddDTO;
import com.eshop.vo.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> getCart(Long userId);
    void addToCart(Long userId, CartAddDTO dto);
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);
    void updateSelected(Long userId, Long cartItemId, Integer selected);
    void removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
