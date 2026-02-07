package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.SharedSpace;
import java.util.List;

/**
 * 共享空间Service接口
 * 
 * @author overdue
 */
public interface SharedSpaceService {
    /**
     * 查询共享空间列表
     * 
     * @param sharedSpace 共享空间
     * @return 共享空间列表
     */
    List<SharedSpace> selectSharedSpaceList(SharedSpace sharedSpace);

    /**
     * 新增共享空间
     * 
     * @param sharedSpace 共享空间
     * @return 结果
     */
    int insertSharedSpace(SharedSpace sharedSpace);

    /**
     * 修改共享空间
     * 
     * @param sharedSpace 共享空间
     * @return 结果
     */
    int updateSharedSpace(SharedSpace sharedSpace);

    /**
     * 删除共享空间信息
     * 
     * @param id 共享空间ID
     * @return 结果
     */
    int deleteSharedSpaceById(Long id);

    /**
     * 根据邀请码查询共享空间
     * 
     * @param inviteCode 邀请码
     * @return 共享空间
     */
    SharedSpace selectByInviteCode(String inviteCode);

    /**
     * 批量删除共享空间信息
     * 
     * @param ids 共享空间ID数组
     * @return 结果
     */
    int deleteSharedSpaceByIds(Long[] ids);

    /**
     * 根据ID查询
     */
    SharedSpace selectById(Long id);

    /**
     * 根据用户ID查询其参与的空间列表
     */
    List<SharedSpace> selectByUserId(Long userId);
}
