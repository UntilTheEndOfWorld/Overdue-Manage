package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.AdWatchRecord;

import java.util.List;
import java.util.Map;

/**
 * 广告观看记录 Service接口
 *
 * @author overdue
 */
public interface AdWatchRecordService {

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
    AdWatchRecord selectByUserId(Long userId);

    /**
     * 根据ID查询记录
     *
     * @param id 记录ID
     * @return 记录
     */
    AdWatchRecord selectById(Long id);

    /**
     * 新增广告观看记录
     *
     * @param adWatchRecord 记录
     * @return 影响行数
     */
    int insertAdWatchRecord(AdWatchRecord adWatchRecord);

    /**
     * 修改广告观看记录
     *
     * @param adWatchRecord 记录
     * @return 影响行数
     */
    int updateAdWatchRecord(AdWatchRecord adWatchRecord);

    /**
     * 删除广告观看记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteAdWatchRecordById(Long id);

    /**
     * 用户观看广告（增加观看次数）
     *
     * @param userId 用户ID
     * @return 更新后的记录
     */
    AdWatchRecord watchAd(Long userId);

    /**
     * 用户使用额度（增加使用次数）
     *
     * @param userId 用户ID
     * @return 是否成功（false表示额度不足）
     */
    boolean useQuota(Long userId);

    /**
     * 获取用户剩余额度
     *
     * @param userId 用户ID
     * @return 剩余额度
     */
    int getRemainingQuota(Long userId);

    /**
     * 统计信息
     *
     * @return 统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 按日期统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    List<Map<String, Object>> getStatsByDate(String startDate, String endDate);
}
