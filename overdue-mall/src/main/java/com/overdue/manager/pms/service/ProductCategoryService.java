package com.overdue.manager.pms.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.h5.domain.dto.CategoryDTO;
import com.overdue.manager.pms.convert.ProductCategoryConvert;
import com.overdue.manager.pms.convert.ProductConvert;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.ProductCategory;
import com.overdue.manager.pms.mapper.ProductCategoryMapper;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.domain.query.ProductCategoryQuery;
import com.overdue.manager.pms.domain.vo.ProductCategoryVO;
import com.github.pagehelper.PageHelper;
import com.overdue.common.exception.base.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类Service业务层处理
 *
 * @author zcc
 */
@Service
public class ProductCategoryService {
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductCategoryConvert convert;
    @Autowired
    private ProductConvert productConvert;

    /**
     * 查询商品分类
     *
     * @param id 商品分类主键
     * @return 商品分类
     */
    public ProductCategory selectById(Long id) {
        return productCategoryMapper.selectById(id);
    }

    /**
     * 查询商品分类列表
     *
     * @param query 查询条件
     * @param page  分页条件
     * @return 商品分类
     */
    public List<ProductCategoryVO> selectList(ProductCategoryQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        QueryWrapper<ProductCategory> qw = new QueryWrapper<>();
        Long parentId = query.getParentId();
        if (parentId != null) {
            qw.eq("parent_id", parentId);
        }
        String nameLike = query.getNameLike();
        if (!StringUtils.isEmpty(nameLike)) {
            qw.like("name", nameLike);
        }
        Integer level = query.getLevel();
        if (level != null) {
            qw.eq("level", level);
        }
        Integer showStatus = query.getShowStatus();
        if (showStatus != null) {
            qw.eq("show_status", showStatus);
        }
        Integer sort = query.getSort();
        if (sort != null) {
            qw.eq("sort", sort);
        }
        String icon = query.getIcon();
        if (!StringUtils.isEmpty(icon)) {
            qw.eq("icon", icon);
        }
        qw.orderByAsc("sort");

        List<ProductCategory> productCategories = productCategoryMapper.selectList(qw);
        List<ProductCategoryVO> productCategoryVOS = convert.dos2vos(productCategories);
        return formatTree(productCategoryVOS);

    }

    private List<ProductCategoryVO> formatTree(List<ProductCategoryVO> nodes) {
        List<ProductCategoryVO> tree = new ArrayList<>();
        List<ProductCategoryVO> children = new ArrayList<>();
        // 1）先获取到所有根节点
        for (ProductCategoryVO node : nodes) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                tree.add(node);
            } else {
                children.add(node);
            }
        }
        // 2）把所有除根结点外的节点作为子节点，然后遍历每一个根节点
        for (ProductCategoryVO node : tree) {
            // 3）递归构建此根的子节点
            recur(node, children);
        }
        return tree;
    }

    private void recur(ProductCategoryVO rootNode, List<ProductCategoryVO> children) {
        // 1）遍历剩余子节点，找出当前根的子节点
        for (ProductCategoryVO node : children) {
            // 2）如果子节点的父id等于根节点的id，那么就将这个节点加到根节点的children列表中
            if (rootNode.getId() == node.getParentId()) {
                if (rootNode.getChildren() == null) {
                    rootNode.setChildren(new ArrayList<>());
                }
                rootNode.getChildren().add(node);
                // 3）以当前节点作为根节点进行递归，检查是否还有子节点。
                recur(node, children);
            }
        }
    }

    /**
     * 新增商品分类
     *
     * @param productCategory 商品分类
     * @return 结果
     */
    public int insert(ProductCategory productCategory) {
        productCategory.setCreateTime(LocalDateTime.now());
        return productCategoryMapper.insert(productCategory);
    }

    /**
     * 修改商品分类
     *
     * @param productCategory 商品分类
     * @return 结果
     */
    public int update(ProductCategory productCategory) {
        return productCategoryMapper.updateById(productCategory);
    }

    /**
     * 删除商品分类信息
     *
     * @param id 商品分类主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return productCategoryMapper.deleteById(id);
    }

    /**
     * 获取所有分类以及分类下面的商品列表
     * 用于分类页面初始化
     *
     * @return 分类及其商品列表
     */
    public List<CategoryDTO> queryCategoryWithProductsForH5() {
        QueryWrapper<ProductCategory> qw1 = new QueryWrapper<>();
        qw1.eq("show_status", 1);
        qw1.eq("level", 0); // 只获取一级分类
        qw1.orderByAsc("sort");
        // 获取所有显示的一级分类
        List<ProductCategory> categories = productCategoryMapper.selectList(qw1);
        if (CollUtil.isEmpty(categories)) {
            return Collections.emptyList();
        }

        return categories.stream().map(it -> {
            CategoryDTO dto = convert.do2dto(it);
            // 寻找该分类下的所有子类
            List<Long> allChildCate = queryAllChildCate(Collections.singletonList(it.getId()), 0);
            QueryWrapper<Product> qw = new QueryWrapper<>();
            qw.select("id", "pic", "name", "price", "category_id");
            qw.in("category_id", allChildCate);
            qw.eq("publish_status", 1); // 只查询已发布的商品
            qw.orderByAsc("sort"); // 按排序值升序
            qw.orderByDesc("create_time"); // 按创建时间降序
            List<Product> categoryId2List = productMapper.selectList(qw);
            dto.setProductList(productConvert.dos2dtos(categoryId2List));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取分类页面所需的所有数据
     * 包括一级分类、二级分类和每个分类下的商品
     *
     * @return 分类及其商品列表
     */
    public List<CategoryDTO> getCategoryPageData() {
        // 获取所有显示的分类
        QueryWrapper<ProductCategory> qw = new QueryWrapper<>();
        qw.eq("show_status", 1);
        qw.orderByAsc("sort");
        List<ProductCategory> allCategories = productCategoryMapper.selectList(qw);

        if (CollUtil.isEmpty(allCategories)) {
            return Collections.emptyList();
        }

        // 转换为DTO
        List<CategoryDTO> categoryDTOs = convert.dos2dtos(allCategories);

        // 构建分类树
        List<CategoryDTO> tree = buildCategoryTree(categoryDTOs);

        // 为每个分类添加商品
        for (CategoryDTO category : tree) {
            addProductsToCategory(category);
        }

        return tree;
    }

    /**
     * 构建分类树结构
     */
    private List<CategoryDTO> buildCategoryTree(List<CategoryDTO> categories) {
        List<CategoryDTO> tree = new ArrayList<>();
        Map<Long, CategoryDTO> categoryMap = new HashMap<>();

        // 创建映射
        for (CategoryDTO category : categories) {
            categoryMap.put(category.getId(), category);
        }

        // 构建树结构
        for (CategoryDTO category : categories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                // 根节点
                tree.add(category);
            } else {
                // 子节点
                CategoryDTO parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }

        return tree;
    }

    /**
     * 为分类添加商品
     */
    private void addProductsToCategory(CategoryDTO category) {
        // 获取该分类及其子分类下的所有商品
        List<Long> allCategoryIds = new ArrayList<>();
        allCategoryIds.add(category.getId());

        // 递归获取所有子分类ID
        if (category.getChildren() != null) {
            collectChildCategoryIds(category.getChildren(), allCategoryIds);
        }

        // 查询商品
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.select("id", "pic", "name", "price", "category_id");
        qw.in("category_id", allCategoryIds);
        qw.eq("publish_status", 1); // 只查询已发布的商品
        qw.orderByAsc("sort"); // 按排序值升序
        qw.orderByDesc("create_time"); // 按创建时间降序
        List<Product> products = productMapper.selectList(qw);
        category.setProductList(productConvert.dos2dtos(products));

        // 递归为子分类添加商品
        if (category.getChildren() != null) {
            for (CategoryDTO child : category.getChildren()) {
                addProductsToCategory(child);
            }
        }
    }

    /**
     * 递归收集子分类ID
     */
    private void collectChildCategoryIds(List<CategoryDTO> children, List<Long> categoryIds) {
        for (CategoryDTO child : children) {
            categoryIds.add(child.getId());
            if (child.getChildren() != null) {
                collectChildCategoryIds(child.getChildren(), categoryIds);
            }
        }
    }

    /**
     * 获取人气推荐商品
     * 从各个分类中选取热门商品作为人气推荐
     * 
     * @return 人气推荐商品列表
     */
    public List<Product> getPopularProductsForH5() {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.select("id", "pic", "name", "price", "category_id");
        qw.eq("publish_status", 1); // 只选择已发布的商品
        qw.le("sort", 50); // 排序值小于等于50的商品（认为是热门商品）
        qw.orderByAsc("sort"); // 按排序值升序
        qw.orderByDesc("create_time"); // 按创建时间降序
        qw.last("LIMIT 20"); // 限制返回20个商品
        return productMapper.selectList(qw);
    }

    /**
     * 根据分类获取人气推荐商品
     * 从指定分类及其子分类中选取商品作为人气推荐
     * 
     * @param categoryIds 分类ID列表
     * @param limit       限制数量
     * @return 人气推荐商品列表
     */
    public List<Product> getPopularProductsByCategoryIds(List<Long> categoryIds, int limit) {
        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }

        // 获取所有子分类
        List<Long> allCategoryIds = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            List<Long> childCateIds = queryAllChildCate(Collections.singletonList(categoryId), 0);
            allCategoryIds.addAll(childCateIds);
        }

        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.select("id", "pic", "name", "price", "category_id");
        qw.eq("publish_status", 1); // 只选择已发布的商品
        qw.in("category_id", allCategoryIds); // 在指定分类中选择
        qw.le("sort", 100); // 排序值小于等于100的商品
        qw.orderByAsc("sort"); // 按排序值升序
        qw.orderByDesc("create_time"); // 按创建时间降序
        qw.last("LIMIT " + limit); // 限制返回数量
        return productMapper.selectList(qw);
    }

    private List<Long> queryAllChildCate(List<Long> categoryIds, int level) {
        List<Long> res = new ArrayList<>();
        QueryWrapper<ProductCategory> qw = new QueryWrapper<>();
        qw.select("id");
        List<Long> ids = categoryIds;
        while (true) {
            qw.clear();
            qw.in("parent_id", ids);
            qw.eq("level", level + 1);
            qw.eq("show_status", 1);
            ids = productCategoryMapper.selectList(qw).stream().map(ProductCategory::getId)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(ids)) {
                break;
            }
            res.addAll(ids);
            level++;
        }
        res.addAll(categoryIds);
        return res;
    }

    public List<ProductCategory> h5Categories() {
        QueryWrapper<ProductCategory> qw = new QueryWrapper<>();
        qw.select("id", "parent_id", "name", "level", "sort", "icon", "show_status");
        qw.eq("show_status", 1);
        // qw.le("level", 2);
        return productCategoryMapper.selectList(qw);
    }

    public List<ProductCategory> getBrotherAndChild(Long id, boolean withChild) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BaseException("参数错误");
        }
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductCategory::getParentId, category.getParentId());
        qw.eq(ProductCategory::getLevel, category.getLevel());
        qw.eq(ProductCategory::getShowStatus, 1);
        qw.select(ProductCategory::getId, ProductCategory::getParentId, ProductCategory::getName,
                ProductCategory::getLevel, ProductCategory::getSort, ProductCategory::getIcon,
                ProductCategory::getShowStatus);
        List<ProductCategory> res = productCategoryMapper.selectList(qw);
        if (withChild) {
            qw.clear();
            qw.eq(ProductCategory::getParentId, category.getId());
            qw.eq(ProductCategory::getLevel, category.getLevel() + 1);
            qw.eq(ProductCategory::getShowStatus, 1);
            List<ProductCategory> childs = productCategoryMapper.selectList(qw);
            res.addAll(childs);
        }
        if (category.getParentId() != null && category.getParentId() != -1) {
            res.add(productCategoryMapper.selectById(category.getParentId()));
        }
        return res;
    }
}
