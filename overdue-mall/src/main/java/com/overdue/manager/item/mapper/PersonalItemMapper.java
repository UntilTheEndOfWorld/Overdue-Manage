package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.PersonalItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;

/**
 * 个人物品Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface PersonalItemMapper extends BaseMapper<PersonalItem> {
    /**
     * 查询个人物品列表
     * 
     * @param personalItem 个人物品
     * @return 个人物品列表
     */
    List<PersonalItem> selectPersonalItemList(PersonalItem personalItem);

    /**
     * 根据用户ID查询物品列表
     * 
     * @param userId 用户ID
     * @return 物品列表
     */
    List<PersonalItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询即将过期的物品（7天内）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 物品列表
     */
    List<PersonalItem> selectExpiringItems(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 查询已过期的物品
     * 
     * @param userId 用户ID
     * @param currentDate 当前日期
     * @return 物品列表
     */
    List<PersonalItem> selectExpiredItems(@Param("userId") Long userId, 
                                         @Param("currentDate") LocalDate currentDate);

    /**
     * 按过期日集合查询未删除物品（到期提醒扫描）
     */
    List<PersonalItem> selectByExpiryDatesIn(@Param("dates") Collection<LocalDate> dates,
                                            @Param("status") String status);
}
