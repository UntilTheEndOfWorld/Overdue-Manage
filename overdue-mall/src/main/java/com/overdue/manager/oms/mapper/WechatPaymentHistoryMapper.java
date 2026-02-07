package com.overdue.manager.oms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.oms.domain.entity.WechatPaymentHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信订单表Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface WechatPaymentHistoryMapper extends BaseMapper<WechatPaymentHistory> {
    /**
     * 查询微信订单表列表
     *
     * @param wechatPaymentHistory 微信订单表
     * @return 微信订单表集合
     */
    List<WechatPaymentHistory> selectByEntity(WechatPaymentHistory wechatPaymentHistory);
}
