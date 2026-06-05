package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.common.BusinessException;
import com.eshop.common.PageResult;
import com.eshop.dto.ProductQueryDTO;
import com.eshop.dto.ProductSaveDTO;
import com.eshop.entity.Product;
import com.eshop.mapper.ProductMapper;
import com.eshop.service.ProductService;
import com.eshop.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public PageResult<ProductVO> queryProducts(ProductQueryDTO dto) {
        Page<ProductVO> page = new Page<>(dto.getPage(), dto.getSize());
        Page<ProductVO> result = productMapper.selectProductPage(
                page, dto.getCategoryId(), dto.getKeyword(), dto.getStatus(), dto.getMerchantId(), dto.getSort()
        );
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    public ProductVO getProduct(Long id) {
        ProductVO vo = productMapper.selectProductById(id);
        if (vo == null) {
            throw new BusinessException("商品不存在");
        }
        return vo;
    }

    @Override
    public void saveProduct(Long merchantId, ProductSaveDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setMerchantId(merchantId);
        product.setStatus("ON_SALE");
        product.setSales(0);
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(Long merchantId, ProductSaveDTO dto) {
        Product product = productMapper.selectById(dto.getId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException("商品不存在");
        }
        BeanUtils.copyProperties(dto, product, "id", "merchantId", "status", "sales");
        productMapper.updateById(product);
    }

    @Override
    public void updateStatus(Long merchantId, Long productId, String status) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long merchantId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BusinessException("商品不存在");
        }
        productMapper.deleteById(productId);
    }
}
