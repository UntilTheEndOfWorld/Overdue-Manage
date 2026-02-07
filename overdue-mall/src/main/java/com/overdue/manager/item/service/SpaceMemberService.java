package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.SpaceMember;

import java.util.List;

/**
 * 共享空间成员 Service
 *
 * @author overdue
 */
public interface SpaceMemberService {

    /**
     * 按空间ID查询成员列表
     *
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<SpaceMember> selectBySpaceId(Long spaceId);

    /**
     * 加入空间（插入成员，状态正常）
     *
     * @param spaceId 空间ID
     * @param userId  用户ID
     * @param role    角色：creator / admin / member
     * @return 影响行数
     */
    int addMember(Long spaceId, Long userId, String role);

    /**
     * 检查用户是否已在空间中
     *
     * @param spaceId 空间ID
     * @param userId  用户ID
     * @return 已在则 true
     */
    boolean isMember(Long spaceId, Long userId);

    /**
     * 退出空间（软删：status=1）
     *
     * @param spaceId 空间ID
     * @param userId  用户ID
     * @return 影响行数
     */
    int leaveSpace(Long spaceId, Long userId);

    /**
     * 查询空间成员列表（带查询条件）
     *
     * @param spaceMember 查询条件
     * @return 成员列表
     */
    List<SpaceMember> selectSpaceMemberList(SpaceMember spaceMember);

    /**
     * 根据ID查询成员
     *
     * @param id 成员关系ID
     * @return 成员信息
     */
    SpaceMember selectById(Long id);

    /**
     * 更新成员信息
     *
     * @param spaceMember 成员信息
     * @return 影响行数
     */
    int updateSpaceMember(SpaceMember spaceMember);

    /**
     * 删除成员（物理删除）
     *
     * @param id 成员关系ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
