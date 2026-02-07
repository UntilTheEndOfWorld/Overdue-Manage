package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.AdWatchRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 广告观看记录 Mapper
 *
 * @author overdue
 */
@Mapper
public interface AdWatchRecordMapper extends BaseMapper<AdWatchRecord> {

    /**
     * 查询广告观看记录列表
     *
     * @param adWatchRecord 查询条件
     * @return 记录列表
     */
    List<AdWatchRecord> selectAdWatchRecordList(AdWatchRecord adWatchRecord);

    /**
     * 按用户ID查询记录
     *
     * @param userId 用户ID
     * @return 记录
     */
    AdWatchRecord selectByUserId(@Param("userId") Long userId);

    /**
     * 统计总观看次数
     *
     * @return 总次数
     */
    Integer sumWatchCount();

    /**
     * 统计总使用次数
     *
     * @return 总次数
     */
    Integer sumUsedCount();

    /**
     * 按日期统计观看记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    List<Map<String, Object>> selectStatsByDate(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
