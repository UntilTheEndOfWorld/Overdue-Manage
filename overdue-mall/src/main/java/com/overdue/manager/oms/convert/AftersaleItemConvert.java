package com.overdue.manager.oms.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.oms.domain.entity.AftersaleItem;
import com.overdue.manager.oms.domain.vo.AftersaleItemVO;
import java.util.List;
/**
 * 订单售后  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface AftersaleItemConvert  {

    List<AftersaleItemVO> dos2vos(List<AftersaleItem> list);
}
