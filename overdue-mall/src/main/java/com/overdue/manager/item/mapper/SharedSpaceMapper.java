package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.SharedSpace;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 共享空间Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface SharedSpaceMapper extends BaseMapper<SharedSpace> {
    /**
     * 查询共享空间列表
     * 
     * @param sharedSpace 共享空间
     * @return 共享空间列表
     */
    List<SharedSpace> selectSharedSpaceList(SharedSpace sharedSpace);

    /**
     * 根据邀请码查询共享空间
     * 
     * @param inviteCode 邀请码
     * @return 共享空间
     */
    SharedSpace selectByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 根据用户ID查询用户参与的所有空间
     * 
     * @param userId 用户ID
     * @return 共享空间列表
     */
    List<SharedSpace> selectByUserId(@Param("userId") Long userId);
}
