package com.overdue.manager.pms.mapper;

import java.time.LocalDateTime;
import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.pms.domain.entity.PointsProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 积分商品SKU信息Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface PointsProductSkuMapper extends BaseMapper<PointsProductSku> {
  /**
   * 查询积分商品SKU信息列表
   *
   * @param pointsProductSku 积分商品SKU信息
   * @return 积分商品SKU信息集合
   */
  List<PointsProductSku> selectByEntity(PointsProductSku pointsProductSku);

  /**
   * 更新库存
   *
   * @param skuId    sku ID
   * @param optDate  操作时间
   * @param quantity 数量
   * @return 更新的行数
   */
  int updateStockById(@Param("skuId") Long skuId, @Param("optDate") LocalDateTime optDate,
      @Param("quantity") Integer quantity);

  /**
   * 增加兑换数量
   *
   * @param skuId    SKU ID
   * @param quantity 增加的数量
   * @return 更新的行数
   */
  int increaseExchangedCount(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
