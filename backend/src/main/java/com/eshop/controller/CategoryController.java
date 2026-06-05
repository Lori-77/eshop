package com.eshop.controller;

import com.eshop.common.Result;
import com.eshop.entity.Category;
import com.eshop.service.CategoryService;
import com.eshop.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/{parentId}/children")
    public Result<List<Category>> getSubCategories(@PathVariable Long parentId) {
        return Result.success(categoryService.getSubCategories(parentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> saveCategory(@RequestBody Category category) {
        categoryService.saveCategory(category);
        return Result.success("分类添加成功");
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }
}
