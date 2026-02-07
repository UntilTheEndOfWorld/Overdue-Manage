package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.SharedItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.time.LocalDate;

/**
 * 共享物品Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface SharedItemMapper extends BaseMapper<SharedItem> {
    /**
     * 查询共享物品列表
     * 
     * @param sharedItem 共享物品
     * @return 共享物品列表
     */
    List<SharedItem> selectSharedItemList(SharedItem sharedItem);

    /**
     * 根据空间ID查询物品列表
     * 
     * @param spaceId 空间ID
     * @return 物品列表
     */
    List<SharedItem> selectBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 查询即将过期的物品（7天内）
     * 
     * @param spaceId 空间ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 物品列表
     */
    List<SharedItem> selectExpiringItems(@Param("spaceId") Long spaceId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    /**
     * 查询已过期的物品
     * 
     * @param spaceId 空间ID
     * @param currentDate 当前日期
     * @return 物品列表
     */
    List<SharedItem> selectExpiredItems(@Param("spaceId") Long spaceId, 
                                       @Param("currentDate") LocalDate currentDate);
}
