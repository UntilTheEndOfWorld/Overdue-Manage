package com.overdue.manager.pms.service;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.h5.domain.vo.ProductDetailVO;
import com.overdue.manager.pms.convert.ProductConvert;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.mapper.BrandMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import com.overdue.manager.pms.domain.vo.ProductVO;
import com.github.pagehelper.PageHelper;
import com.overdue.common.utils.MemberSecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.query.ProductQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品信息Service业务层处理
 *
 *
 * @author zcc
 */
@Service
@Slf4j
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private ProductConvert convert;

    /**
     * 查询商品信息
     *
     * @param id 商品信息主键
     * @return 商品信息
     */
    public ProductVO selectById(Long id) {
        Product product = productMapper.selectById(id);
        ProductVO productVO = convert.do2vo(product);
        QueryWrapper<Sku> qw = new QueryWrapper<>();
        qw.eq("product_id", product.getId());
        List<Sku> skus = skuMapper.selectList(qw);
        productVO.setSkuList(skus);
        return productVO;
    }

    /**
     * 查询商品信息列表
     *
     * @param query 查询条件
     * @param page  分页条件
     * @return 商品信息
     */
    public List<Product> selectList(ProductQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (StringUtils.isNoneEmpty(query.getOrderField())) {
            // 将Java属性名转换为数据库字段名
            String dbField = convertToDbField(query.getOrderField());
            if (StringUtils.isNotEmpty(query.getOrderSort()) && "desc".equalsIgnoreCase(query.getOrderSort())) {
                qw.orderByDesc(dbField);
            } else {
                qw.orderByAsc(dbField);
            }
        } else {
            qw.orderByDesc("publish_status");
            qw.orderByAsc("sort");
        }
        Long categoryId = query.getCategoryId();
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        Integer publishStatus = query.getPublishStatus();
        if (publishStatus != null) {
            qw.eq("publish_status", publishStatus);
        }
        String search = query.getSearch();
        if (StringUtils.isNoneEmpty(search)) {
            qw.like("name", "%".concat(query.getSearch().trim()).concat("%"));
        }
        if (CollectionUtil.isNotEmpty(query.getExcludeProductIds())) {
            qw.notIn("id", query.getExcludeProductIds());
        }
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            qw.in("id", query.getIds());
        }
        return productMapper.selectList(qw);
    }

    /**
     * 将Java属性名转换为数据库字段名
     */
    private String convertToDbField(String javaField) {
        if (javaField == null) {
            return null;
        }

        // 常见的字段映射
        switch (javaField) {
            case "createTime":
                return "create_time";
            case "updateTime":
                return "update_time";
            case "createBy":
                return "create_by";
            case "updateBy":
                return "update_by";
            case "categoryId":
                return "category_id";
            case "brandId":
                return "brand_id";
            case "outProductId":
                return "out_product_id";
            case "albumPics":
                return "album_pics";
            case "publishStatus":
                return "publish_status";
            case "detailHtml":
                return "detail_html";
            case "detailMobileHtml":
                return "detail_mobile_html";
            case "brandName":
                return "brand_name";
            case "productCategoryName":
                return "product_category_name";
            case "productAttr":
                return "product_attr";
            case "supportExpress":
                return "support_express";
            default:
                // 如果没有特殊映射，使用通用的驼峰转下划线转换
                return javaField.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
    }

    /**
     * 新增商品信息
     *
     * @param productVO 商品信息
     * @return 结果
     */
    @Transactional
    public int insert(ProductVO productVO) {

        Product product = convert.vo2do(productVO);
        product.setCreateTime(LocalDateTime.now());
        List<Sku> skuList = productVO.getSkuList();
        productMapper.insert(product);
        if (skuList != null) {
            skuList.forEach(sku -> {
                sku.setProductId(product.getId());
                sku.setCreateTime(LocalDateTime.now());
                skuMapper.insert(sku);
            });
        }
        return 1;
    }

    /**
     * 修改商品信息
     *
     * @param productVO 商品信息
     * @return 结果
     */
    @Transactional
    public int update(ProductVO productVO) {
        Product dbProduct = productMapper.selectById(productVO.getId());
        List<Long> idList = productVO.getSkuList().stream().filter(it -> it.getId() != null).map(it -> it.getId())
                .collect(Collectors.toList());
        if (dbProduct == null) {
            return 0;
        }
        Long userId = MemberSecurityUtils.getMemberId();
        Product product = convert.vo2do(productVO);
        List<Sku> skuList = productVO.getSkuList();
        product.setUpdateBy(userId);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        // 查找库中所有的sku
        Map<String, Object> map = new HashMap<>();
        map.put("product_id", product.getId());
        Map<Long, Sku> skuMap = skuMapper.selectByMap(map).stream()
                .collect(Collectors.toMap(it -> it.getId(), it -> it, (v1, v2) -> v2));
        // 针对已有的进行编辑
        List<Sku> updateList = productVO.getSkuList().stream().filter(it -> it.getId() != null)
                .collect(Collectors.toList());
        if (!CollectionUtil.isEmpty(updateList)) {
            log.info("共有{}个sku需要修改，{}，productId：{}", updateList.size(), JSONUtil.toJsonStr(updateList),
                    productVO.getId());
            updateList.forEach(it -> {
                Sku sku = skuMap.get(it.getId());
                sku.setUpdateBy(MemberSecurityUtils.getMemberId());
                sku.setUpdateTime(LocalDateTime.now());
                sku.setPrice(it.getPrice());
                sku.setSpData(it.getSpData());
                sku.setPic(it.getPic());
                sku.setOutSkuId(it.getOutSkuId());
                sku.setStock(it.getStock());
                skuMapper.updateById(sku);
            });
        }
        // 针对没有的进行新增
        List<Sku> addList = productVO.getSkuList().stream().filter(it -> it.getId() == null)
                .collect(Collectors.toList());
        if (!CollectionUtil.isEmpty(addList)) {
            log.info("共有{}个sku需要新增，{}，productId：{}", addList.size(), JSONUtil.toJsonStr(addList), productVO.getId());
            addList.forEach(sku -> {
                sku.setProductId(product.getId());
                sku.setCreateTime(LocalDateTime.now());
                skuMapper.insert(sku);
            });
        }
        // 删除
        List<Long> deleteIds = skuMap.keySet().stream().filter(it -> !idList.contains(it)).collect(Collectors.toList());
        if (!CollectionUtil.isEmpty(deleteIds)) {
            log.info("共有{}个sku需要删除，{}，productId：{}", deleteIds.size(), JSONUtil.toJsonStr(deleteIds),
                    productVO.getId());
            skuMapper.deleteBatchIds(deleteIds);
        }
        return 1;
    }

    /**
     * 删除商品信息信息
     *
     * @param id 商品信息主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return productMapper.deleteById(id);
    }

    public ProductDetailVO queryDetail(Long id) {
        ProductDetailVO res = new ProductDetailVO();
        Product d = productMapper.selectById(id);
        res.setProduct(d);
        LambdaQueryWrapper<Sku> qw = new LambdaQueryWrapper<>();
        qw.eq(Sku::getProductId, id);
        res.setSkus(skuMapper.selectList(qw));
        if (d.getBrandId() != null) {
            res.setBrand(brandMapper.selectById(d.getBrandId()));
        }
        return res;
    }

    /**
     * 获取热门商品列表
     * 按热度值获取热门商品
     *
     * @param limit 限制数量
     * @return 热门商品列表
     */
    public List<Product> getHotProducts(int limit) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        // 只查询已发布的商品
        qw.eq("publish_status", 1);
        // 按热度值倒序排列（热度值越高越热门）
        qw.orderByDesc("hotness");
        // 按创建时间倒序排列（热度值相同时，最新优先）
        qw.orderByDesc("create_time");
        // 限制数量
        qw.last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(qw);
        log.info("获取热门商品成功，数量: {}, 按热度值排序", products.size());
        return products;
    }

    /**
     * 获取新品推荐列表
     * 按创建时间获取最新商品
     *
     * @param limit 限制数量
     * @return 新品列表
     */
    public List<Product> getNewProducts(int limit) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        // 只查询已发布的商品
        qw.eq("publish_status", 1);
        // 按创建时间倒序排列（最新优先）
        qw.orderByDesc("create_time");
        // 限制数量
        qw.last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(qw);
        log.info("获取新品推荐成功，数量: {}", products.size());
        return products;
    }

    /**
     * 增加商品热度值
     * 每卖出一件商品，热度值增加1
     *
     * @param productId 商品ID
     * @param quantity  销售数量
     */
    public void increaseHotness(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            log.warn("增加商品热度值参数无效: productId={}, quantity={}", productId, quantity);
            return;
        }

        try {
            int updatedRows = productMapper.increaseHotness(productId, quantity);
            log.info("商品热度值增加成功: productId={}, quantity={}, updatedRows={}", productId, quantity, updatedRows);
        } catch (Exception e) {
            log.error("增加商品热度值失败: productId={}, quantity={}", productId, quantity, e);
        }
    }

    /**
     * 获取轮播图商品列表
     *
     * @param limit 限制数量
     * @return 轮播图商品列表
     */
    public List<Product> getBannerProducts(Integer limit) {
        return productMapper.selectBannerProducts(limit);
    }
}
