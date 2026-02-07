package com.overdue.manager.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.pms.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品信息Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 查询商品信息列表
     *
     * @param product 商品信息
     * @return 商品信息集合
     */
    List<Product> selectByEntity(Product product);

    /**
     * 增加商品热度值
     *
     * @param productId 商品ID
     * @param quantity  增加的数量
     * @return 更新的行数
     */
    int increaseHotness(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 查询轮播图商品列表
     *
     * @param limit 限制数量
     * @return 轮播图商品集合
     */
    List<Product> selectBannerProducts(@Param("limit") Integer limit);
}
