package com.overdue.manager.oms.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.oms.domain.entity.OrderItem;
import com.overdue.manager.oms.domain.vo.OrderItemVO;
import java.util.List;
/**
 * 订单中所包含的商品  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface OrderItemConvert  {

    List<OrderItemVO> dos2vos(List<OrderItem> list);
}
