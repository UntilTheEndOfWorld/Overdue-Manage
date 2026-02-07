package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.OverdueMember;
import java.util.List;

/**
 * 会员信息Service接口
 * 
 * @author overdue
 */
public interface OverdueMemberService {
    /**
     * 查询会员信息列表
     * 
     * @param overdueMember 会员信息
     * @return 会员信息列表
     */
    List<OverdueMember> selectOverdueMemberList(OverdueMember overdueMember);

    /**
     * 新增会员信息
     * 
     * @param overdueMember 会员信息
     * @return 结果
     */
    int insertOverdueMember(OverdueMember overdueMember);

    /**
     * 修改会员信息
     * 
     * @param overdueMember 会员信息
     * @return 结果
     */
    int updateOverdueMember(OverdueMember overdueMember);

    /**
     * 根据用户ID查询会员信息
     * 
     * @param userId 用户ID
     * @return 会员信息
     */
    OverdueMember selectByUserId(Long userId);

    /**
     * 根据ID查询会员信息
     * 
     * @param id 会员ID
     * @return 会员信息
     */
    OverdueMember selectById(Long id);
}
