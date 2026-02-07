package com.overdue.manager.oms.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.domain.vo.OrderVO;
import java.util.List;
/**
 * 订单表  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface OrderConvert  {

    List<OrderVO> dos2vos(List<Order> list);

    OrderVO do2vo(Order order);
}
