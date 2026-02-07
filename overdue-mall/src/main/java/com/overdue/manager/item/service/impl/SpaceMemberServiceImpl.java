package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.SpaceMember;
import com.overdue.manager.item.mapper.SpaceMemberMapper;
import com.overdue.manager.item.service.SpaceMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 共享空间成员 Service 实现
 *
 * @author overdue
 */
@Service
public class SpaceMemberServiceImpl implements SpaceMemberService {

    @Autowired
    private SpaceMemberMapper spaceMemberMapper;

    @Override
    public List<SpaceMember> selectBySpaceId(Long spaceId) {
        if (spaceId == null) {
            return java.util.Collections.emptyList();
        }
        return spaceMemberMapper.selectBySpaceId(spaceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addMember(Long spaceId, Long userId, String role) {
        if (spaceId == null || userId == null) {
            return 0;
        }
        SpaceMember existing = spaceMemberMapper.selectBySpaceIdAndUserId(spaceId, userId);
        if (existing != null) {
            if ("1".equals(existing.getStatus())) {
                existing.setStatus("0");
                existing.setRole(role != null ? role : "member");
                existing.setJoinTime(LocalDateTime.now());
                return spaceMemberMapper.updateById(existing);
            }
            return 1;
        }
        SpaceMember m = new SpaceMember();
        m.setSpaceId(spaceId);
        m.setUserId(userId);
        m.setRole(role != null ? role : "member");
        m.setJoinTime(LocalDateTime.now());
        m.setStatus("0");
        return spaceMemberMapper.insert(m);
    }

    @Override
    public boolean isMember(Long spaceId, Long userId) {
        if (spaceId == null || userId == null) {
            return false;
        }
        return spaceMemberMapper.selectBySpaceIdAndUserId(spaceId, userId) != null;
    }

    @Override
    public int leaveSpace(Long spaceId, Long userId) {
        if (spaceId == null || userId == null) {
            return 0;
        }
        SpaceMember m = spaceMemberMapper.selectBySpaceIdAndUserId(spaceId, userId);
        if (m == null) {
            return 0;
        }
        m.setStatus("1");
        return spaceMemberMapper.updateById(m);
    }

    @Override
    public List<SpaceMember> selectSpaceMemberList(SpaceMember spaceMember) {
        return spaceMemberMapper.selectSpaceMemberList(spaceMember);
    }

    @Override
    public SpaceMember selectById(Long id) {
        if (id == null) {
            return null;
        }
        return spaceMemberMapper.selectById(id);
    }

    @Override
    public int updateSpaceMember(SpaceMember spaceMember) {
        if (spaceMember == null || spaceMember.getId() == null) {
            return 0;
        }
        return spaceMemberMapper.updateById(spaceMember);
    }

    @Override
    public int deleteById(Long id) {
        if (id == null) {
            return 0;
        }
        return spaceMemberMapper.deleteById(id);
    }
}
