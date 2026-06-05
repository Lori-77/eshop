package com.eshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eshop.entity.CartItem;
import com.eshop.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    List<CartVO> selectCartByUserId(@Param("userId") Long userId);
}
