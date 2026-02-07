package com.overdue.manager.pms.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.domain.vo.SkuVO;
import java.util.List;
/**
 * sku信息  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface SkuConvert  {

    List<SkuVO> dos2vos(List<Sku> list);
}
