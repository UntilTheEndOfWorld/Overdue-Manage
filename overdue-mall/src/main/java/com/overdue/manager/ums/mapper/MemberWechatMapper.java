package com.overdue.manager.ums.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户微信信息Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MemberWechatMapper extends BaseMapper<MemberWechat> {
    /**
     * 查询用户微信信息列表
     *
     * @param memberWechat 用户微信信息
     * @return 用户微信信息集合
     */
    List<MemberWechat> selectByEntity(MemberWechat memberWechat);

    /**
     * 根据会员ID查询微信信息
     *
     * @param memberId 会员ID
     * @return 用户微信信息
     */
    MemberWechat selectByMemberId(Long memberId);
}
