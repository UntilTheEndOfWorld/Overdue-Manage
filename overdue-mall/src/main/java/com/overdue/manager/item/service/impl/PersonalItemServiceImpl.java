package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.domain.vo.PersonalItemStatsVO;
import com.overdue.manager.item.mapper.PersonalItemMapper;
import com.overdue.manager.item.service.PersonalItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * ???????Service??????
 * 
 * @author overdue
 */
@Service
public class PersonalItemServiceImpl implements PersonalItemService {
    @Autowired
    private PersonalItemMapper personalItemMapper;

    @Override
    public List<PersonalItem> selectPersonalItemList(PersonalItem personalItem) {
        return personalItemMapper.selectPersonalItemList(personalItem);
    }

    @Override
    public int insertPersonalItem(PersonalItem personalItem) {
        LocalDateTime now = LocalDateTime.now();
        if (personalItem.getCreateTime() == null) {
            personalItem.setCreateTime(now);
        }
        if (personalItem.getUpdateTime() == null) {
            personalItem.setUpdateTime(now);
        }
        return personalItemMapper.insert(personalItem);
    }

    @Override
    public int updatePersonalItem(PersonalItem personalItem) {
        personalItem.setUpdateTime(LocalDateTime.now());
        return personalItemMapper.updateById(personalItem);
    }

    @Override
    public int deletePersonalItemById(Long id) {
        return personalItemMapper.deleteById(id);
    }

    @Override
    public List<PersonalItem> selectByUserId(Long userId) {
        return personalItemMapper.selectByUserId(userId);
    }

    @Override
    public PersonalItemStatsVO getItemStats(Long userId) {
        PersonalItemStatsVO vo = new PersonalItemStatsVO();
        if (userId == null) {
            return vo;
        }
        List<PersonalItem> items = personalItemMapper.selectByUserId(userId);
        int total = items.size();
        long expired = 0;
        long near = 0;
        Instant now = Instant.now();
        Instant sevenDaysLater = now.plus(7, ChronoUnit.DAYS);
        ZoneId zone = ZoneId.systemDefault();
        for (PersonalItem item : items) {
            LocalDate d = item.getExpiryDate();
            if (d == null) {
                continue;
            }
            Instant expStart = d.atStartOfDay(zone).toInstant();
            if (!expStart.isAfter(now)) {
                expired++;
            } else if (!expStart.isAfter(sevenDaysLater)) {
                near++;
            }
        }
        long normal = (long) total - expired - near;
        if (normal < 0) {
            normal = 0;
        }
        vo.setTotal(total);
        vo.setExpired(expired);
        vo.setNear(near);
        vo.setNormal(normal);
        return vo;
    }

    @Override
    public List<PersonalItem> selectExpiringItems(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysLater = now.plusDays(7);
        return personalItemMapper.selectExpiringItems(userId, now, sevenDaysLater);
    }

    @Override
    public List<PersonalItem> selectExpiredItems(Long userId) {
        return personalItemMapper.selectExpiredItems(userId, LocalDate.now());
    }

    @Override
    public int deletePersonalItemByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += personalItemMapper.deleteById(id);
        }
        return count;
    }

    @Override
    public PersonalItem selectById(Long id) {
        return personalItemMapper.selectById(id);
    }
}
