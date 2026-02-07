package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.OverdueMember;
import com.overdue.manager.item.mapper.OverdueMemberMapper;
import com.overdue.manager.item.service.OverdueMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ??????Service??????
 * 
 * @author overdue
 */
@Service
public class OverdueMemberServiceImpl implements OverdueMemberService {
    @Autowired
    private OverdueMemberMapper overdueMemberMapper;

    @Override
    public List<OverdueMember> selectOverdueMemberList(OverdueMember overdueMember) {
        return overdueMemberMapper.selectOverdueMemberList(overdueMember);
    }

    @Override
    public int insertOverdueMember(OverdueMember overdueMember) {
        return overdueMemberMapper.insert(overdueMember);
    }

    @Override
    public int updateOverdueMember(OverdueMember overdueMember) {
        return overdueMemberMapper.updateById(overdueMember);
    }

    @Override
    public OverdueMember selectByUserId(Long userId) {
        return overdueMemberMapper.selectByUserId(userId);
    }

    @Override
    public OverdueMember selectById(Long id) {
        return overdueMemberMapper.selectById(id);
    }
}
