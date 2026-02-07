package com.overdue.h5.controller;

import com.overdue.h5.domain.dto.CategoryDTO;
import com.overdue.manager.pms.domain.entity.ProductCategory;
import com.overdue.manager.pms.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/no-auth/category")
public class CategoryController {
    @Autowired
    private ProductCategoryService categoryService;

    @GetMapping("/all-categories")
    public ResponseEntity<List<ProductCategory>> allCategories() {
        return ResponseEntity.ok(categoryService.h5Categories());
    }

    /**
     * 获取所有分类以及分类下面的商品列表
     * 用于分类页面初始化
     *
     * @return 分类及其商品列表
     */
    @GetMapping("/categories-with-products")
    public ResponseEntity<List<CategoryDTO>> getCategoriesWithProducts() {
        List<CategoryDTO> result = categoryService.getCategoryPageData();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category-by-id")
    public ResponseEntity<List<ProductCategory>> getBrotherAndChild(@RequestParam Long id,
            @RequestParam(name = "withChild", defaultValue = "false") boolean withChild) {
        return ResponseEntity.ok(categoryService.getBrotherAndChild(id, withChild));
    }
}
