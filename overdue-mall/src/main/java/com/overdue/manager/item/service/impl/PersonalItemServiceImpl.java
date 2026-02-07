package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.mapper.PersonalItemMapper;
import com.overdue.manager.item.service.PersonalItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
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
        return personalItemMapper.insert(personalItem);
    }

    @Override
    public int updatePersonalItem(PersonalItem personalItem) {
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
