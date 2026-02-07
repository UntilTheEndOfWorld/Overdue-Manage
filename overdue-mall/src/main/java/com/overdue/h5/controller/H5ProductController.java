package com.overdue.h5.controller;

import com.overdue.h5.domain.dto.CategoryDTO;
import com.overdue.h5.domain.vo.H5ProductVO;
import com.overdue.h5.domain.vo.ProductDetailVO;
import com.overdue.manager.pms.convert.ProductConvert;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.query.ProductQuery;
import com.overdue.manager.pms.service.ProductService;
import com.overdue.manager.pms.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/no-auth/product")
public class H5ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductConvert productConvert;
    @Autowired
    private ProductCategoryService categoryService;

    @PostMapping("/list")
    public ResponseEntity<Page<H5ProductVO>> queryGoodByPage(@RequestBody ProductQuery query, Pageable page) {
        List<Product> pageRes = productService.selectList(query, page);
        return ResponseEntity.ok(new PageImpl<>(productConvert.dos2dtos(pageRes), page,
                ((com.github.pagehelper.Page) pageRes).getTotal()));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDetailVO> queryDetail(@PathVariable Long id) {
        ProductDetailVO detail = productService.queryDetail(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * 获取热门商品列表
     *
     * @param limit 限制数量，默认8个
     * @return 热门商品列表
     */
    @GetMapping("/hot")
    public ResponseEntity<List<H5ProductVO>> getHotProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<Product> products = productService.getHotProducts(limit);
        List<H5ProductVO> result = productConvert.dos2dtos(products);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取新品推荐列表
     *
     * @param limit 限制数量，默认8个
     * @return 新品列表
     */
    @GetMapping("/new")
    public ResponseEntity<List<H5ProductVO>> getNewProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<Product> products = productService.getNewProducts(limit);
        List<H5ProductVO> result = productConvert.dos2dtos(products);
        return ResponseEntity.ok(result);
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
}
