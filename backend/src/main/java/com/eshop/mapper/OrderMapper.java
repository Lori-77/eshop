package com.eshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.entity.Order;
import com.eshop.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    Page<OrderVO> selectOrderPage(Page<OrderVO> page,
                                   @Param("userId") Long userId,
                                   @Param("merchantId") Long merchantId,
                                   @Param("status") String status);

    OrderVO selectOrderDetail(@Param("id") Long id);

    Map<String, Object> selectStats(@Param("merchantId") Long merchantId);
}
