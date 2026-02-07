package com.overdue.manager.oms.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.overdue.h5.domain.dto.OrderProductListDTO;
import com.overdue.manager.ums.domain.entity.Member;
import com.github.pagehelper.PageHelper;
import com.overdue.common.utils.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.overdue.manager.oms.mapper.OrderItemMapper;
import com.overdue.manager.oms.domain.entity.OrderItem;
import com.overdue.manager.oms.domain.query.OrderItemQuery;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单中所包含的商品Service业务层处理
 *
 *
 * @author zcc
 */
@Slf4j
@Service
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> {
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 查询订单中所包含的商品
     *
     * @param id 订单中所包含的商品主键
     * @return 订单中所包含的商品
     */
    public OrderItem selectById(Long id) {
        return orderItemMapper.selectById(id);
    }

    /**
     * 查询订单中所包含的商品列表
     *
     * @param query 查询条件
     * @param page  分页条件
     * @return 订单中所包含的商品
     */
    public List<OrderItem> selectList(OrderItemQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        QueryWrapper<OrderItem> qw = new QueryWrapper<>();
        Long orderId = query.getOrderId();
        if (orderId != null) {
            qw.eq("order_id", orderId);
        }
        Long productId = query.getProductId();
        if (productId != null) {
            qw.eq("product_id", productId);
        }
        String outProductId = query.getOutProductId();
        if (!StringUtils.isEmpty(outProductId)) {
            qw.eq("out_product_id", outProductId);
        }
        Long skuId = query.getSkuId();
        if (skuId != null) {
            qw.eq("sku_id", skuId);
        }
        String outSkuId = query.getOutSkuId();
        if (!StringUtils.isEmpty(outSkuId)) {
            qw.eq("out_sku_id", outSkuId);
        }
        Long productSnapshotId = query.getProductSnapshotId();
        if (productSnapshotId != null) {
            qw.eq("product_snapshot_id", productSnapshotId);
        }
        Long skuSnapshotId = query.getSkuSnapshotId();
        if (skuSnapshotId != null) {
            qw.eq("sku_snapshot_id", skuSnapshotId);
        }
        String pic = query.getPic();
        if (!StringUtils.isEmpty(pic)) {
            qw.eq("pic", pic);
        }
        String productNameLike = query.getProductNameLike();
        if (!StringUtils.isEmpty(productNameLike)) {
            qw.like("product_name", productNameLike);
        }
        BigDecimal salePrice = query.getSalePrice();
        if (salePrice != null) {
            qw.eq("sale_price", salePrice);
        }
        BigDecimal purchasePrice = query.getPurchasePrice();
        if (purchasePrice != null) {
            qw.eq("purchase_price", purchasePrice);
        }
        Integer quantity = query.getQuantity();
        if (quantity != null) {
            qw.eq("quantity", quantity);
        }
        Long productCategoryId = query.getProductCategoryId();
        if (productCategoryId != null) {
            qw.eq("product_category_id", productCategoryId);
        }
        String spData = query.getSpData();
        if (!StringUtils.isEmpty(spData)) {
            qw.eq("sp_data", spData);
        }
        return orderItemMapper.selectList(qw);
    }

    /**
     * 新增订单中所包含的商品
     *
     * @param orderItem 订单中所包含的商品
     * @return 结果
     */
    public int insert(OrderItem orderItem) {
        orderItem.setCreateTime(LocalDateTime.now());
        return orderItemMapper.insert(orderItem);
    }

    /**
     * 修改订单中所包含的商品
     *
     * @param orderItem 订单中所包含的商品
     * @return 结果
     */
    public int update(OrderItem orderItem) {
        return orderItemMapper.updateById(orderItem);
    }

    /**
     * 删除订单中所包含的商品信息
     *
     * @param id 订单中所包含的商品主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return orderItemMapper.deleteById(id);
    }

    @Transactional
    public void saveOrderItem(Member member, LocalDateTime optTime,
            Long orderId, List<OrderProductListDTO> list) {
        List<OrderItem> addOrderItemList = new ArrayList<>();
        list.forEach(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(IDGenerator.generateId());
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProduct().getId());
            orderItem.setOutProductId(item.getProduct().getOutProductId());
            orderItem.setSkuId(item.getSku().getId());
            orderItem.setOutSkuId(item.getSku().getOutSkuId());
            // 优先使用SKU的图片，如果SKU没有图片则使用商品的图片
            String pic = item.getSku().getPic();
            log.info("订单商品[{}] SKU图片: {}", item.getProduct().getName(), pic);

            if (StringUtils.isEmpty(pic)) {
                pic = item.getProduct().getPic();
                log.info("订单商品[{}] 使用商品图片: {}", item.getProduct().getName(), pic);
            }

            orderItem.setPic(pic);
            log.info("订单商品[{}] 最终保存的图片: {}", item.getProduct().getName(), pic);
            orderItem.setProductName(item.getProduct().getName());
            orderItem.setSalePrice(item.getSku().getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProductCategoryId(item.getProduct().getCategoryId());
            orderItem.setSpData(item.getSku().getSpData());
            orderItem.setCreateBy(member.getId());
            orderItem.setCreateTime(optTime);
            addOrderItemList.add(orderItem);
        });
        boolean flag = saveBatch(addOrderItemList);
        if (!flag) {
            throw new RuntimeException("新增订单item失败");
        }
    }

    /**
     * 批量更新订单商品图片
     * 用于修复现有订单中缺失的图片信息
     */
    @Transactional
    public void updateOrderItemImages() {
        // 查询所有没有图片的订单商品
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("pic").or().eq("pic", "");
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        if (orderItems.isEmpty()) {
            log.info("没有需要更新图片的订单商品");
            return;
        }

        log.info("开始更新{}个订单商品的图片信息", orderItems.size());

        // 获取所有相关的商品ID和SKU ID
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> skuIds = orderItems.stream()
                .map(OrderItem::getSkuId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询商品和SKU信息
        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            QueryWrapper<Product> productQuery = new QueryWrapper<>();
            productQuery.in("id", productIds);
            productMap = productMapper.selectList(productQuery).stream()
                    .collect(Collectors.toMap(Product::getId, product -> product, (v1, v2) -> v2));
        }

        Map<Long, Sku> skuMap = new HashMap<>();
        if (!skuIds.isEmpty()) {
            QueryWrapper<Sku> skuQuery = new QueryWrapper<>();
            skuQuery.in("id", skuIds);
            skuMap = skuMapper.selectList(skuQuery).stream()
                    .collect(Collectors.toMap(Sku::getId, sku -> sku, (v1, v2) -> v2));
        }

        // 更新每个订单商品的图片
        int updatedCount = 0;
        for (OrderItem orderItem : orderItems) {
            String pic = null;

            // 优先从SKU获取图片
            if (orderItem.getSkuId() != null && skuMap.containsKey(orderItem.getSkuId())) {
                Sku sku = skuMap.get(orderItem.getSkuId());
                if (StringUtils.isNotEmpty(sku.getPic())) {
                    pic = sku.getPic();
                }
            }

            // 如果SKU没有图片，从商品获取图片
            if (StringUtils.isEmpty(pic) && orderItem.getProductId() != null
                    && productMap.containsKey(orderItem.getProductId())) {
                Product product = productMap.get(orderItem.getProductId());
                if (StringUtils.isNotEmpty(product.getPic())) {
                    pic = product.getPic();
                }
            }

            // 如果找到了图片，更新订单商品
            if (StringUtils.isNotEmpty(pic)) {
                orderItem.setPic(pic);
                orderItem.setUpdateTime(LocalDateTime.now());
                orderItemMapper.updateById(orderItem);
                updatedCount++;
            }
        }

        log.info("成功更新{}个订单商品的图片信息", updatedCount);
    }
}
