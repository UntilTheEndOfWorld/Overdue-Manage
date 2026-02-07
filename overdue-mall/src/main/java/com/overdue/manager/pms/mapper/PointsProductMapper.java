package com.overdue.manager.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分商品信息Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface PointsProductMapper extends BaseMapper<PointsProduct> {
  /**
   * 查询积分商品信息列表
   *
   * @param pointsProduct 积分商品信息
   * @return 积分商品信息集合
   */
  List<PointsProduct> selectByEntity(PointsProduct pointsProduct);

  /**
   * 增加积分商品热度值
   *
   * @param productId 商品ID
   * @param quantity  增加的数量
   * @return 更新的行数
   */
  int increaseHotness(@Param("productId") Long productId, @Param("quantity") Integer quantity);

  /**
   * 查询轮播图积分商品列表
   *
   * @param limit 限制数量
   * @return 轮播图积分商品集合
   */
  List<PointsProduct> selectBannerProducts(@Param("limit") Integer limit);

  /**
   * 增加兑换数量
   *
   * @param productId 商品ID
   * @param quantity  增加的数量
   * @return 更新的行数
   */
  int increaseExchangedCount(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
