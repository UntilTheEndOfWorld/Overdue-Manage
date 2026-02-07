package com.overdue.manager.ums.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.ums.domain.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 意见反馈Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
    /**
     * 查询意见反馈列表
     *
     * @param feedback 意见反馈
     * @return 意见反馈集合
     */
    List<Feedback> selectByEntity(Feedback feedback);
}
