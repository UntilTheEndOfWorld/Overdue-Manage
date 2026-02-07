package com.overdue.manager.pms.convert;

import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.domain.vo.PointsProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 积分商品信息 DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface PointsProductConvert {

  List<PointsProductVO> dos2vos(List<PointsProduct> list);

  @Mapping(source = "isBanner", target = "isBanner")
  @Mapping(source = "bannerTitle", target = "bannerTitle")
  @Mapping(source = "supportExpress", target = "supportExpress")
  @Mapping(source = "productType", target = "productType")
  @Mapping(source = "exchangeLimit", target = "exchangeLimit")
  @Mapping(source = "totalStock", target = "totalStock")
  @Mapping(source = "exchangedCount", target = "exchangedCount")
  @Mapping(source = "activityStartTime", target = "activityStartTime")
  @Mapping(source = "activityEndTime", target = "activityEndTime")
  @Mapping(source = "isLimitedTime", target = "isLimitedTime")
  @Mapping(source = "description", target = "description")
  PointsProduct vo2do(PointsProductVO pointsProductVO);

  @Mapping(source = "isBanner", target = "isBanner")
  @Mapping(source = "bannerTitle", target = "bannerTitle")
  @Mapping(source = "supportExpress", target = "supportExpress")
  @Mapping(source = "productType", target = "productType")
  @Mapping(source = "exchangeLimit", target = "exchangeLimit")
  @Mapping(source = "totalStock", target = "totalStock")
  @Mapping(source = "exchangedCount", target = "exchangedCount")
  @Mapping(source = "activityStartTime", target = "activityStartTime")
  @Mapping(source = "activityEndTime", target = "activityEndTime")
  @Mapping(source = "isLimitedTime", target = "isLimitedTime")
  @Mapping(source = "description", target = "description")
  PointsProductVO do2vo(PointsProduct pointsProduct);
}
