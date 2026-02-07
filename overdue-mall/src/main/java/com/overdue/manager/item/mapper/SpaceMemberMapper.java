package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.SpaceMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 共享空间成员 Mapper
 *
 * @author overdue
 */
@Mapper
public interface SpaceMemberMapper extends BaseMapper<SpaceMember> {

    /**
     * 按空间ID查询成员列表
     *
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<SpaceMember> selectBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 按空间ID和用户ID查询成员（是否已在空间中）
     *
     * @param spaceId 空间ID
     * @param userId  用户ID
     * @return 成员记录，未加入则 null
     */
    SpaceMember selectBySpaceIdAndUserId(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

    /**
     * 查询空间成员列表（带查询条件）
     *
     * @param spaceMember 查询条件
     * @return 成员列表
     */
    List<SpaceMember> selectSpaceMemberList(SpaceMember spaceMember);
}
