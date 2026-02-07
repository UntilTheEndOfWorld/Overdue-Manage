package com.overdue.manager.statistics.mapper;

import com.overdue.manager.statistics.domain.query.GoodsStatisticsQuery;
import com.overdue.manager.statistics.domain.query.OrderStatisticsQuery;
import com.overdue.manager.statistics.domain.vo.OrderStatisticsVO;
import com.overdue.manager.statistics.domain.vo.ProductTopVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IndexStatisticsMapper {
    List<ProductTopVO> goodsSkuStatistics(GoodsStatisticsQuery goodsStatisticsQuery);

    List<ProductTopVO> goodsStatistics(GoodsStatisticsQuery goodsStatisticsQuery);

    List<OrderStatisticsVO> orderStatistics(OrderStatisticsQuery param);

}
