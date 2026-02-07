package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.SharedItem;
import java.util.List;
import java.time.LocalDate;

/**
 * 共享物品Service接口
 * 
 * @author overdue
 */
public interface SharedItemService {
    /**
     * 查询共享物品列表
     * 
     * @param sharedItem 共享物品
     * @return 共享物品集合
     */
    List<SharedItem> selectSharedItemList(SharedItem sharedItem);

    /**
     * 新增共享物品
     * 
     * @param sharedItem 共享物品
     * @return 结果
     */
    int insertSharedItem(SharedItem sharedItem);

    /**
     * 修改共享物品
     * 
     * @param sharedItem 共享物品
     * @return 结果
     */
    int updateSharedItem(SharedItem sharedItem);

    /**
     * 删除共享物品信息
     * 
     * @param id 共享物品ID
     * @return 结果
     */
    int deleteSharedItemById(Long id);

    /**
     * 根据空间ID查询物品列表
     * 
     * @param spaceId 空间ID
     * @return 物品集合
     */
    List<SharedItem> selectBySpaceId(Long spaceId);

    /**
     * 查询即将过期的物品（7天内）
     * 
     * @param spaceId 空间ID
     * @return 物品集合
     */
    List<SharedItem> selectExpiringItems(Long spaceId);

    /**
     * ????????
     *
     * @param spaceId ??ID
     * @return ????
     */
    List<SharedItem> selectExpiredItems(Long spaceId);

    /**
     * ??ID??????
     */
    SharedItem selectById(Long id);
}
