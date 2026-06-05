package com.eshop.controller;

import com.eshop.common.PageResult;
import com.eshop.common.Result;
import com.eshop.dto.ProductQueryDTO;
import com.eshop.dto.ProductSaveDTO;
import com.eshop.service.ProductService;
import com.eshop.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> queryProducts(ProductQueryDTO dto) {
        return Result.success(productService.queryProducts(dto));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProduct(id));
    }

    @PostMapping
    public Result<?> saveProduct(Authentication authentication, @Valid @RequestBody ProductSaveDTO dto) {
        Long merchantId = (Long) authentication.getPrincipal();
        productService.saveProduct(merchantId, dto);
        return Result.success("商品添加成功");
    }

    @PutMapping
    public Result<?> updateProduct(Authentication authentication, @Valid @RequestBody ProductSaveDTO dto) {
        Long merchantId = (Long) authentication.getPrincipal();
        productService.updateProduct(merchantId, dto);
        return Result.success("商品更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(Authentication authentication, @PathVariable Long id, @RequestParam String status) {
        Long merchantId = (Long) authentication.getPrincipal();
        productService.updateStatus(merchantId, id, status);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(Authentication authentication, @PathVariable Long id) {
        Long merchantId = (Long) authentication.getPrincipal();
        productService.deleteProduct(merchantId, id);
        return Result.success("商品删除成功");
    }
}
