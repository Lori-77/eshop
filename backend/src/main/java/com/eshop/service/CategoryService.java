package com.eshop.service;

import com.eshop.entity.Category;
import com.eshop.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> getCategoryTree();
    List<Category> getSubCategories(Long parentId);
    void saveCategory(Category category);
    void updateCategory(Category category);
    void deleteCategory(Long id);
}
