package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.domain.vo.PersonalItemStatsVO;

import java.util.List;
import java.time.LocalDate;

/**
 * 个人物品Service接口
 * 
 * @author overdue
 */
public interface PersonalItemService {
    /**
     * 查询个人物品列表
     * 
     * @param personalItem 个人物品
     * @return 个人物品列表
     */
    List<PersonalItem> selectPersonalItemList(PersonalItem personalItem);

    /**
     * 新增个人物品
     * 
     * @param personalItem 个人物品
     * @return 结果
     */
    int insertPersonalItem(PersonalItem personalItem);

    /**
     * 新增个人物品（含默认字段与操作日志）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param personalItem 个人物品
     * @return 结果
     */
    int insertPersonalItemWithLog(Long userId, String userName, PersonalItem personalItem);

    /**
     * 修改个人物品
     * 
     * @param personalItem 个人物品
     * @return 结果
     */
    int updatePersonalItem(PersonalItem personalItem);

    /**
     * 修改个人物品（含操作日志）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param personalItem 个人物品
     * @return 结果
     */
    int updatePersonalItemWithLog(Long userId, String userName, PersonalItem personalItem);

    /**
     * 删除个人物品信息
     * 
     * @param id 个人物品ID
     * @return 结果
     */
    int deletePersonalItemById(Long id);

    /**
     * 删除个人物品（逻辑删除并记录日志）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param id 物品ID
     * @param opType 操作类型
     * @return 结果
     */
    int deletePersonalItemByIdWithLog(Long userId, String userName, Long id, String opType);

    /**
     * 根据用户ID查询物品列表
     * 
     * @param userId 用户ID
     * @return 物品列表
     */
    List<PersonalItem> selectByUserId(Long userId);

    /**
     * 查询用户物品并补充过期日志
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @return 物品列表
     */
    List<PersonalItem> selectByUserIdWithExpireLog(Long userId, String userName);

    /**
     * 按当前时间与过期日统计各状态数量（与前端 item.js 逻辑对齐）
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    PersonalItemStatsVO getItemStats(Long userId);

    /**
     * 查询即将过期的物品（7天内）
     * 
     * @param userId 用户ID
     * @return 物品列表
     */
    List<PersonalItem> selectExpiringItems(Long userId);

    /**
     * 查询已过期的物品
     * 
     * @param userId 用户ID
     * @return 物品列表
     */
    List<PersonalItem> selectExpiredItems(Long userId);

    /**
     * 批量删除个人物品信息
     * 
     * @param ids 个人物品ID数组
     * @return 结果
     */
    int deletePersonalItemByIds(Long[] ids);

    /**
     * 根据ID查询个人物品
     * 
     * @param id 个人物品ID
     * @return 个人物品
     */
    PersonalItem selectById(Long id);
}
