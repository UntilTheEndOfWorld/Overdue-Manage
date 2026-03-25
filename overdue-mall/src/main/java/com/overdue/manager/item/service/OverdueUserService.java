package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.domain.form.OverdueProfileForm;
import com.overdue.manager.item.domain.form.ReminderSettingsForm;
import com.overdue.manager.item.domain.vo.OverdueProfileVO;
import com.overdue.manager.item.domain.vo.ReminderSettingsVO;

import java.util.List;

/**
 * 用户Service接口
 * 
 * @author overdue
 */
public interface OverdueUserService {
    /**
     * 查询用户列表
     * 
     * @param overdueUser 用户
     * @return 用户列表
     */
    List<OverdueUser> selectOverdueUserList(OverdueUser overdueUser);

    /**
     * 新增用户
     * 
     * @param overdueUser 用户
     * @return 结果
     */
    int insertOverdueUser(OverdueUser overdueUser);

    /**
     * 修改用户
     * 
     * @param overdueUser 用户
     * @return 结果
     */
    int updateOverdueUser(OverdueUser overdueUser);

    /**
     * 删除用户信息
     * 
     * @param id 用户ID
     * @return 结果
     */
    int deleteOverdueUserById(Long id);

    /**
     * 根据OpenID查询用户
     * 
     * @param openid 微信OpenID
     * @return 用户
     */
    OverdueUser selectByOpenid(String openid);

    /**
     * 批量删除用户信息
     * 
     * @param ids 用户ID数组
     * @return 结果
     */
    int deleteOverdueUserByIds(Long[] ids);

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户
     */
    OverdueUser selectById(Long id);

    /**
     * 根据 OpenID 创建或获取用户（登录时同步 OverdueUser）
     *
     * @param openid   微信 OpenID
     * @param nickname 昵称（可选）
     * @param avatarUrl 头像（可选）
     * @param gender   性别（可选）
     * @param country  国家（可选）
     * @param province 省份（可选）
     * @param city     城市（可选）
     * @return 用户实体，不会为 null
     */
    OverdueUser createOrGetByOpenid(String openid, String nickname, String avatarUrl,
                                    Integer gender, String country, String province, String city);

    /**
     * 查询用户到期提醒设置（脱敏）
     */
    ReminderSettingsVO getReminderSettings(Long userId);

    /**
     * 更新到期提醒设置
     */
    void updateReminderSettings(Long userId, ReminderSettingsForm form);

    /**
     * 小程序个人资料
     */
    OverdueProfileVO getProfile(Long userId);

    void updateProfile(Long userId, OverdueProfileForm form);
}
