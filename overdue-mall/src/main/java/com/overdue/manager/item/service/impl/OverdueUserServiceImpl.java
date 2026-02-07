package com.overdue.manager.item.service.impl;

import com.overdue.common.utils.StringUtils;
import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.mapper.OverdueUserMapper;
import com.overdue.manager.item.service.OverdueUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ???Service??????
 * 
 * @author overdue
 */
@Service
public class OverdueUserServiceImpl implements OverdueUserService {
    @Autowired
    private OverdueUserMapper overdueUserMapper;

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
        overdueUserMapper.insert(user);
        return user;
    }
}
