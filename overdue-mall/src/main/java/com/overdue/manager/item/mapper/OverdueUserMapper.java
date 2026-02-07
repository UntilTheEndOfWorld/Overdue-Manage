package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.OverdueUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface OverdueUserMapper extends BaseMapper<OverdueUser> {
    /**
     * 查询用户列表
     * 
     * @param overdueUser 用户
     * @return 用户列表
     */
    List<OverdueUser> selectOverdueUserList(OverdueUser overdueUser);

    /**
     * 根据OpenID查询用户
     * 
     * @param openid 微信OpenID
     * @return 用户
     */
    OverdueUser selectByOpenid(@Param("openid") String openid);
}
