package com.overdue.manager.item.service.impl;

import com.overdue.common.utils.OssUtils;
import com.overdue.common.utils.StringUtils;
import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.domain.form.OverdueProfileForm;
import com.overdue.manager.item.domain.form.ReminderSettingsForm;
import com.overdue.manager.item.domain.vo.OverdueProfileVO;
import com.overdue.manager.item.domain.vo.ReminderSettingsVO;
import com.overdue.manager.item.mapper.OverdueUserMapper;
import com.overdue.manager.item.service.OverdueUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ???Service??????
 * 
 * @author overdue
 */
@Slf4j
@Service
public class OverdueUserServiceImpl implements OverdueUserService {
    @Autowired
    private OverdueUserMapper overdueUserMapper;

    @Autowired
    private OssUtils ossUtils;

    @Override
    public List<OverdueUser> selectOverdueUserList(OverdueUser overdueUser) {
        return overdueUserMapper.selectOverdueUserList(overdueUser);
    }

    @Override
    public int insertOverdueUser(OverdueUser overdueUser) {
        return overdueUserMapper.insert(overdueUser);
    }

    @Override
    public int updateOverdueUser(OverdueUser overdueUser) {
        return overdueUserMapper.updateById(overdueUser);
    }

    @Override
    public int deleteOverdueUserById(Long id) {
        return overdueUserMapper.deleteById(id);
    }

    @Override
    public OverdueUser selectByOpenid(String openid) {
        return overdueUserMapper.selectByOpenid(openid);
    }

    @Override
    public int deleteOverdueUserByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += overdueUserMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public OverdueUser selectById(Long id) {
        return overdueUserMapper.selectById(id);
    }

    @Override
    public OverdueUser createOrGetByOpenid(String openid, String nickname, String avatarUrl,
                                           Integer gender, String country, String province, String city) {
        if (StringUtils.isEmpty(openid)) {
            return null;
        }
        OverdueUser user = overdueUserMapper.selectByOpenid(openid);
        if (user != null) {
            boolean needUpdate = false;
            if (StringUtils.isNotEmpty(nickname) && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                needUpdate = true;
            }
            if (StringUtils.isNotEmpty(avatarUrl) && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                needUpdate = true;
            }
            if (gender != null && !gender.equals(user.getGender())) {
                user.setGender(gender);
                needUpdate = true;
            }
            if (StringUtils.isNotEmpty(country)) {
                user.setCountry(country);
                needUpdate = true;
            }
            if (StringUtils.isNotEmpty(province)) {
                user.setProvince(province);
                needUpdate = true;
            }
            if (StringUtils.isNotEmpty(city)) {
                user.setCity(city);
                needUpdate = true;
            }
            if (needUpdate) {
                overdueUserMapper.updateById(user);
            }
            return user;
        }
        user = new OverdueUser();
        user.setOpenid(openid);
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);
        user.setGender(gender != null ? gender : 0);
        user.setCountry(country);
        user.setProvince(province);
        user.setCity(city);
        user.setStatus("0");
        user.setReminderEnabled("0");
        user.setReminderSubscribe("0");
        user.setReminderSms("0");
        user.setReminderEmail("0");
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        overdueUserMapper.insert(user);
        return user;
    }

    @Override
    public ReminderSettingsVO getReminderSettings(Long userId) {
        OverdueUser user = overdueUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return toReminderSettingsVO(user);
    }

    @Override
    public void updateReminderSettings(Long userId, ReminderSettingsForm form) {
        if (form == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        OverdueUser user = overdueUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.isNotEmpty(form.getContactPhone())) {
            String p = form.getContactPhone().trim();
            if (p.length() < 11 || !p.matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("请输入正确的11位手机号");
            }
            user.setPhone(p);
        }
        if (form.getContactEmail() != null) {
            String em = form.getContactEmail().trim();
            if (StringUtils.isNotEmpty(em) && !em.contains("@")) {
                throw new IllegalArgumentException("请输入正确的邮箱格式");
            }
            user.setEmail(StringUtils.isEmpty(em) ? null : em);
        }
        if (form.getReminderEnabled() != null) {
            user.setReminderEnabled(normalizeFlag(form.getReminderEnabled()));
        }
        if (form.getReminderSubscribe() != null) {
            user.setReminderSubscribe(normalizeFlag(form.getReminderSubscribe()));
        }
        if (form.getReminderSms() != null) {
            user.setReminderSms(normalizeFlag(form.getReminderSms()));
        }
        if (form.getReminderEmail() != null) {
            user.setReminderEmail(normalizeFlag(form.getReminderEmail()));
        }
        String phone = user.getPhone();
        String email = user.getEmail();
        if ("1".equals(user.getReminderSms()) && StringUtils.isEmpty(phone)) {
            throw new IllegalArgumentException("开启短信提醒前请先填写手机号");
        }
        if ("1".equals(user.getReminderEmail()) && StringUtils.isEmpty(email)) {
            throw new IllegalArgumentException("开启邮件提醒前请先填写邮箱");
        }
        overdueUserMapper.updateById(user);
    }

    private static String normalizeFlag(String v) {
        if (v == null) {
            return "0";
        }
        String t = v.trim();
        if ("1".equals(t) || "true".equalsIgnoreCase(t)) {
            return "1";
        }
        return "0";
    }

    private static ReminderSettingsVO toReminderSettingsVO(OverdueUser user) {
        ReminderSettingsVO vo = new ReminderSettingsVO();
        vo.setReminderEnabled(StringUtils.isEmpty(user.getReminderEnabled()) ? "0" : user.getReminderEnabled());
        vo.setReminderSubscribe(StringUtils.isEmpty(user.getReminderSubscribe()) ? "0" : user.getReminderSubscribe());
        vo.setReminderSms(StringUtils.isEmpty(user.getReminderSms()) ? "0" : user.getReminderSms());
        vo.setReminderEmail(StringUtils.isEmpty(user.getReminderEmail()) ? "0" : user.getReminderEmail());
        String phone = user.getPhone();
        vo.setPhoneBound(StringUtils.isNotEmpty(phone));
        vo.setPhoneMasked(maskPhone(phone));
        String email = user.getEmail();
        vo.setEmailBound(StringUtils.isNotEmpty(email));
        vo.setEmailMasked(maskEmail(email));
        return vo;
    }

    private static String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 7) {
            return "";
        }
        if (phone.length() >= 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone.charAt(0) + "***";
    }

    private static String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return "";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 1) {
            return "*" + domain;
        }
        return local.charAt(0) + "***" + domain;
    }

    @Override
    public OverdueProfileVO getProfile(Long userId) {
        OverdueUser user = overdueUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        OverdueProfileVO vo = new OverdueProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        return vo;
    }

    @Override
    public void updateProfile(Long userId, OverdueProfileForm form) {
        if (form == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        OverdueUser user = overdueUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.isNotEmpty(form.getNickname())) {
            user.setNickname(form.getNickname().trim());
        }
        if (form.getPhone() != null) {
            String p = form.getPhone().trim();
            if (StringUtils.isNotEmpty(p)) {
                if (p.length() != 11 || !p.matches("^1[3-9]\\d{9}$")) {
                    throw new IllegalArgumentException("请输入正确的11位手机号");
                }
                user.setPhone(p);
            } else {
                user.setPhone(null);
            }
        }
        if (form.getEmail() != null) {
            String em = form.getEmail().trim();
            if (StringUtils.isNotEmpty(em)) {
                if (!em.contains("@") || em.length() > 128) {
                    throw new IllegalArgumentException("请输入正确的邮箱");
                }
                user.setEmail(em);
            } else {
                user.setEmail(null);
            }
        }
        if (form.getAvatarUrl() != null) {
            String url = form.getAvatarUrl().trim();
            String newAvatar = StringUtils.isEmpty(url) ? null : url;
            String oldAvatar = user.getAvatarUrl();
            if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
                deleteOldAvatarFromOss(oldAvatar);
            } else if (newAvatar == null && StringUtils.isNotEmpty(oldAvatar)) {
                deleteOldAvatarFromOss(oldAvatar);
            }
            user.setAvatarUrl(newAvatar);
        }
        overdueUserMapper.updateById(user);
    }

    /**
     * 更换头像时删除 OSS 上旧文件；非本服务 OSS 地址或删除失败不阻断主流程。
     */
    private void deleteOldAvatarFromOss(String oldUrl) {
        if (StringUtils.isEmpty(oldUrl)) {
            return;
        }
        try {
            boolean ok = ossUtils.deleteFile(oldUrl);
            if (!ok) {
                log.warn("旧头像未删除（可能非本 OSS 或解析失败）: {}", oldUrl);
            }
        } catch (Exception e) {
            log.warn("删除旧头像异常: {}", oldUrl, e);
        }
    }
}
