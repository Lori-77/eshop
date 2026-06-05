package com.eshop.service;

import com.eshop.common.PageResult;
import com.eshop.dto.ProductQueryDTO;
import com.eshop.dto.ProductSaveDTO;
import com.eshop.vo.ProductVO;

public interface ProductService {
    PageResult<ProductVO> queryProducts(ProductQueryDTO dto);
    ProductVO getProduct(Long id);
    void saveProduct(Long merchantId, ProductSaveDTO dto);
    void updateProduct(Long merchantId, ProductSaveDTO dto);
    void updateStatus(Long merchantId, Long productId, String status);
    void deleteProduct(Long merchantId, Long productId);
}
