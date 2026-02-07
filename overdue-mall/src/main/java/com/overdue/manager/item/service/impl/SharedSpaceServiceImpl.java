package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.SharedSpace;
import com.overdue.manager.item.mapper.SharedSpaceMapper;
import com.overdue.manager.item.service.SharedSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ???????Service??????
 * 
 * @author overdue
 */
@Service
public class SharedSpaceServiceImpl implements SharedSpaceService {
    @Autowired
    private SharedSpaceMapper sharedSpaceMapper;

    @Override
    public List<SharedSpace> selectSharedSpaceList(SharedSpace sharedSpace) {
        return sharedSpaceMapper.selectSharedSpaceList(sharedSpace);
    }

    @Override
    public int insertSharedSpace(SharedSpace sharedSpace) {
        return sharedSpaceMapper.insert(sharedSpace);
    }

    @Override
    public int updateSharedSpace(SharedSpace sharedSpace) {
        return sharedSpaceMapper.updateById(sharedSpace);
    }

    @Override
    public int deleteSharedSpaceById(Long id) {
        return sharedSpaceMapper.deleteById(id);
    }

    @Override
    public SharedSpace selectByInviteCode(String inviteCode) {
        return sharedSpaceMapper.selectByInviteCode(inviteCode);
    }

    @Override
    public int deleteSharedSpaceByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += sharedSpaceMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public SharedSpace selectById(Long id) {
        return sharedSpaceMapper.selectById(id);
    }

    @Override
    public List<SharedSpace> selectByUserId(Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return sharedSpaceMapper.selectByUserId(userId);
    }
}
