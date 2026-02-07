package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.OverdueMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 会员信息Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface OverdueMemberMapper extends BaseMapper<OverdueMember> {
    /**
     * 查询会员信息列表
     * 
     * @param overdueMember 会员信息
     * @return 会员信息列表
     */
    List<OverdueMember> selectOverdueMemberList(OverdueMember overdueMember);

    /**
     * 根据用户ID查询会员信息
     * 
     * @param userId 用户ID
     * @return 会员信息
     */
    OverdueMember selectByUserId(@Param("userId") Long userId);
}
