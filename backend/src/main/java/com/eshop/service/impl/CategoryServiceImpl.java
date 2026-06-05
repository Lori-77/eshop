package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.entity.Category;
import com.eshop.mapper.CategoryMapper;
import com.eshop.service.CategoryService;
import com.eshop.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder)
        );

        // 找出顶级分类
        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        return roots.stream().map(root -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(root.getId());
            vo.setName(root.getName());
            vo.setIcon(root.getIcon());
            vo.setSortOrder(root.getSortOrder());
            vo.setChildren(getChildren(root.getId(), all));
            return vo;
        }).collect(Collectors.toList());
    }

    private List<CategoryVO> getChildren(Long parentId, List<Category> all) {
        List<Category> children = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parentId))
                .collect(Collectors.toList());

        if (children.isEmpty()) return new ArrayList<>();

        return children.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIcon(c.getIcon());
            vo.setSortOrder(c.getSortOrder());
            vo.setChildren(getChildren(c.getId(), all));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Category> getSubCategories(Long parentId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, parentId)
                        .orderByAsc(Category::getSortOrder)
        );
    }

    @Override
    public void saveCategory(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Category category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
