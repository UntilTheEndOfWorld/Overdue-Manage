package com.overdue.manager.ums.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.ums.domain.entity.MemberAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员收货地址Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MemberAddressMapper extends BaseMapper<MemberAddress> {
    /**
     * 查询会员收货地址列表
     *
     * @param memberAddress 会员收货地址
     * @return 会员收货地址集合
     */
    List<MemberAddress> selectByEntity(MemberAddress memberAddress);

    int updateByPrimaryKeySelective(MemberAddress address);

    int updateDefault(@Param("isDefault")int isDefault, @Param("memberId")Long memberId);
}
