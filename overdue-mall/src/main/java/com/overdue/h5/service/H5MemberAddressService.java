package com.overdue.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberAddress;
import com.overdue.manager.ums.mapper.MemberAddressMapper;
import com.overdue.manager.ums.domain.form.MemberAddressForm;
import com.overdue.manager.ums.domain.vo.MemberAddressVO;
import com.overdue.common.constant.Constants;
import com.overdue.common.utils.AesCryptoUtils;
import com.overdue.common.utils.PhoneUtils;
import com.overdue.framework.config.LocalDataUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员收货地址Service业务层处理
 * 
 * @author sjm
 */
@Service
@Transactional
public class H5MemberAddressService {
    @Autowired
    private MemberAddressMapper memberAddressMapper;

    @Value("${aes.key}")
    private String aesKey;

    /**
     * 查询会员收货地址
     * 
     * @param id 会员收货地址主键
     * @return 会员收货地址
     */

    public MemberAddressVO selectById(Long id) {
        MemberAddress memberAddress = memberAddressMapper.selectById(id);
        MemberAddressVO memberAddressVO = new MemberAddressVO();
        BeanUtils.copyProperties(memberAddress, memberAddressVO);
        memberAddressVO.setPhone(AesCryptoUtils.decrypt(aesKey, memberAddress.getPhoneEncrypted()));
        return memberAddressVO;
    }

    public List<MemberAddressVO> selectList() {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        if (member == null) {
            throw new RuntimeException("用户未登录");
        }
        MemberAddress memberAddress = new MemberAddress();
        memberAddress.setMemberId(member.getId());
        List<MemberAddress> memberAddressesList = memberAddressMapper.selectByEntity(memberAddress);
        return memberAddressesList.stream().map(it -> {
            MemberAddressVO vo = new MemberAddressVO();
            BeanUtils.copyProperties(it, vo);
            vo.setPhone(AesCryptoUtils.decrypt(aesKey, it.getPhoneEncrypted()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 新增会员收货地址
     * 
     * @param memberAddressForm 会员收货地址
     * @return 结果
     */
    public int insert(MemberAddressForm memberAddressForm) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        if (memberAddressForm.getIsDefault() == 1) {
            // 将别的设置为0
            memberAddressMapper.updateDefault(0, member.getId());
        }

        // 添加调试日志
        System.out.println("[H5MemberAddressService] 新增地址，接收到的表单数据: " + memberAddressForm);
        System.out.println("[H5MemberAddressService] detailAddress字段值: " + memberAddressForm.getDetailAddress());

        MemberAddress memberAddress = new MemberAddress();
        BeanUtils.copyProperties(memberAddressForm, memberAddress);

        // 添加调试日志
        System.out.println("[H5MemberAddressService] 复制属性后的实体对象: " + memberAddress);
        System.out.println("[H5MemberAddressService] 实体对象的detailAddress字段值: " + memberAddress.getDetailAddress());

        memberAddress.setPhoneHidden(PhoneUtils.hidePhone(memberAddressForm.getPhone()));
        memberAddress.setPhoneEncrypted(AesCryptoUtils.encrypt(aesKey, memberAddressForm.getPhone()));
        memberAddress.setMemberId(member.getId());
        memberAddress.setCreateTime(LocalDateTime.now());

        int result = memberAddressMapper.insert(memberAddress);
        System.out.println("[H5MemberAddressService] 插入结果: " + result);
        return result;
    }

    /**
     * 修改会员收货地址
     * 
     * @param memberAddressForm 会员收货地址
     * @return 结果
     */

    public int update(MemberAddressForm memberAddressForm) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        if (memberAddressForm.getIsDefault() == 1) {
            // 将别的设置为0
            memberAddressMapper.updateDefault(0, member.getId());
        }

        // 添加调试日志
        System.out.println("[H5MemberAddressService] 更新地址，接收到的表单数据: " + memberAddressForm);
        System.out.println("[H5MemberAddressService] detailAddress字段值: " + memberAddressForm.getDetailAddress());

        MemberAddress memberAddress = new MemberAddress();
        BeanUtils.copyProperties(memberAddressForm, memberAddress);

        // 添加调试日志
        System.out.println("[H5MemberAddressService] 复制属性后的实体对象: " + memberAddress);
        System.out.println("[H5MemberAddressService] 实体对象的detailAddress字段值: " + memberAddress.getDetailAddress());

        memberAddress.setPhoneHidden(PhoneUtils.hidePhone(memberAddressForm.getPhone()));
        memberAddress.setPhoneEncrypted(AesCryptoUtils.encrypt(aesKey, memberAddressForm.getPhone()));
        memberAddress.setUpdateTime(LocalDateTime.now());
        memberAddress.setUpdateBy(member.getId());

        int result = memberAddressMapper.updateById(memberAddress);
        System.out.println("[H5MemberAddressService] 更新结果: " + result);
        return result;
    }

    /**
     * 批量删除会员收货地址
     * 
     * @param ids 需要删除的会员收货地址主键
     * @return 结果
     */

    public int deleteByIds(Long[] ids) {
        return memberAddressMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除会员收货地址信息
     * 
     * @param id 会员收货地址主键
     * @return 结果
     */

    public int deleteById(Long id) {
        return memberAddressMapper.deleteById(id);
    }

    public MemberAddressVO getDefault() {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        QueryWrapper<MemberAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", member.getId());
        queryWrapper.eq("is_default", 1);
        List<MemberAddress> list = memberAddressMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        MemberAddressVO memberAddressVO = new MemberAddressVO();
        BeanUtils.copyProperties(list.get(0), memberAddressVO);
        memberAddressVO.setPhone(AesCryptoUtils.decrypt(aesKey, list.get(0).getPhoneEncrypted()));
        return memberAddressVO;
    }

    /**
     * 设置默认地址
     * 
     * @param id 地址ID
     * @return 结果
     */
    public int setDefault(Long id) {
        try {
            Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
            System.out.println("[H5MemberAddressService] 设置默认地址，用户ID: " + member.getId() + ", 地址ID: " + id);

            // 首先验证地址是否属于当前用户
            MemberAddress address = memberAddressMapper.selectById(id);
            if (address == null) {
                System.out.println("[H5MemberAddressService] 地址不存在，ID: " + id);
                throw new RuntimeException("地址不存在");
            }

            if (!address.getMemberId().equals(member.getId())) {
                System.out.println("[H5MemberAddressService] 地址不属于当前用户，地址用户ID: " + address.getMemberId() + ", 当前用户ID: "
                        + member.getId());
                throw new RuntimeException("地址不存在或无权操作");
            }

            System.out.println("[H5MemberAddressService] 开始取消当前用户的所有默认地址");
            // 取消当前用户的所有默认地址
            int updateResult = memberAddressMapper.updateDefault(0, member.getId());
            System.out.println("[H5MemberAddressService] 取消默认地址结果: " + (updateResult > 0 ? "成功" : "失败"));

            // 设置指定地址为默认
            address.setIsDefault(1);
            address.setUpdateTime(LocalDateTime.now());
            address.setUpdateBy(member.getId());

            System.out.println("[H5MemberAddressService] 开始设置指定地址为默认");
            int result = memberAddressMapper.updateById(address);
            System.out.println("[H5MemberAddressService] 设置默认地址结果: " + result);

            return result;
        } catch (Exception e) {
            System.err.println("[H5MemberAddressService] 设置默认地址异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
