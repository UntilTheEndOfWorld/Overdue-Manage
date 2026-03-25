package com.overdue.manager.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.item.domain.entity.OverdueReminderSent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OverdueReminderSentMapper extends BaseMapper<OverdueReminderSent> {
}
