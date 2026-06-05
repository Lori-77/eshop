package com.eshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.entity.Product;
import com.eshop.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    Page<ProductVO> selectProductPage(Page<ProductVO> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword,
                                       @Param("status") String status,
                                       @Param("merchantId") Long merchantId,
                                       @Param("sort") String sort);

    ProductVO selectProductById(@Param("id") Long id);
}
