package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.common.BusinessException;
import com.eshop.dto.CartAddDTO;
import com.eshop.entity.CartItem;
import com.eshop.entity.Product;
import com.eshop.mapper.CartItemMapper;
import com.eshop.mapper.ProductMapper;
import com.eshop.service.CartService;
import com.eshop.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CartVO> getCart(Long userId) {
        return cartItemMapper.selectCartByUserId(userId);
    }

    @Override
    @Transactional
    public void addToCart(Long userId, CartAddDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 检查是否已在购物车中
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, dto.getProductId())
        );

        if (existing != null) {
            int newQty = existing.getQuantity() + dto.getQuantity();
            if (newQty > product.getStock()) {
                throw new BusinessException("库存不足");
            }
            existing.setQuantity(newQty);
            cartItemMapper.updateById(existing);
        } else {
            if (dto.getQuantity() > product.getStock()) {
                throw new BusinessException("库存不足");
            }
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(dto.getProductId());
            item.setQuantity(dto.getQuantity());
            item.setSelected(1);
            cartItemMapper.insert(item);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        Product product = productMapper.selectById(item.getProductId());
        if (quantity > product.getStock()) {
            throw new BusinessException("库存不足");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    public void updateSelected(Long userId, Long cartItemId, Integer selected) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        item.setSelected(selected);
        cartItemMapper.updateById(item);
    }

    @Override
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId)
        );
    }
}
