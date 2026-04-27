package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.SharedItem;
import com.overdue.manager.item.mapper.SharedItemMapper;
import com.overdue.manager.item.service.SharedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 共享物品Service业务层处理
 * 
 * @author overdue
 */
@Service
public class SharedItemServiceImpl implements SharedItemService {
    private static final String STATUS_NORMAL = "0";

    @Autowired
    private SharedItemMapper sharedItemMapper;

    @Override
    public List<SharedItem> selectSharedItemList(SharedItem sharedItem) {
        return sharedItemMapper.selectSharedItemList(sharedItem);
    }

    @Override
    public int insertSharedItem(SharedItem sharedItem) {
        if (sharedItem == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (sharedItem.getStatus() == null || sharedItem.getStatus().trim().isEmpty()) {
            sharedItem.setStatus(STATUS_NORMAL);
        }
        if (sharedItem.getCreateTime() == null) {
            sharedItem.setCreateTime(now);
        }
        sharedItem.setUpdateTime(now);
        return sharedItemMapper.insert(sharedItem);
    }

    @Override
    public int updateSharedItem(SharedItem sharedItem) {
        if (sharedItem == null || sharedItem.getId() == null) {
            return 0;
        }
        SharedItem existing = sharedItemMapper.selectById(sharedItem.getId());
        if (existing == null) {
            return 0;
        }
        if (sharedItem.getSpaceId() == null) {
            sharedItem.setSpaceId(existing.getSpaceId());
        }
        if (sharedItem.getCreatorId() == null) {
            sharedItem.setCreatorId(existing.getCreatorId());
        }
        if (sharedItem.getStatus() == null || sharedItem.getStatus().trim().isEmpty()) {
            sharedItem.setStatus(existing.getStatus() == null || existing.getStatus().trim().isEmpty()
                    ? STATUS_NORMAL
                    : existing.getStatus());
        }
        if (sharedItem.getCreateTime() == null) {
            sharedItem.setCreateTime(existing.getCreateTime() == null ? LocalDateTime.now() : existing.getCreateTime());
        }
        sharedItem.setUpdateTime(LocalDateTime.now());
        return sharedItemMapper.updateById(sharedItem);
    }

    @Override
    public int deleteSharedItemById(Long id) {
        return sharedItemMapper.deleteById(id);
    }

    @Override
    public List<SharedItem> selectBySpaceId(Long spaceId) {
        return sharedItemMapper.selectBySpaceId(spaceId);
    }

    @Override
    public List<SharedItem> selectExpiringItems(Long spaceId) {
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysLater = now.plusDays(7);
        return sharedItemMapper.selectExpiringItems(spaceId, now, sevenDaysLater);
    }

    @Override
    public List<SharedItem> selectExpiredItems(Long spaceId) {
        return sharedItemMapper.selectExpiredItems(spaceId, LocalDate.now());
    }

    @Override
    public SharedItem selectById(Long id) {
        return sharedItemMapper.selectById(id);
    }

}
