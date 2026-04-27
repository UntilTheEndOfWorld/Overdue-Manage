package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.domain.vo.PersonalItemStatsVO;
import com.overdue.manager.item.domain.entity.OperationLog;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.overdue.manager.item.mapper.PersonalItemMapper;
import com.overdue.manager.item.service.OperationLogService;
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
    private static final String ITEM_TYPE_PERSONAL = "personal";
    private static final String STATUS_NORMAL = "0";
    private static final String STATUS_DELETED = "1";

    @Autowired
    private PersonalItemMapper personalItemMapper;
    @Autowired
    private OperationLogService operationLogService;

    @Override
    public List<PersonalItem> selectPersonalItemList(PersonalItem personalItem) {
        return personalItemMapper.selectPersonalItemList(personalItem);
    }

    @Override
    public int insertPersonalItem(PersonalItem personalItem) {
        if (personalItem == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (personalItem.getStatus() == null || personalItem.getStatus().trim().isEmpty()) {
            personalItem.setStatus(STATUS_NORMAL);
        }
        if (personalItem.getCreateTime() == null) {
            personalItem.setCreateTime(now);
        }
        personalItem.setUpdateTime(now);
        return personalItemMapper.insert(personalItem);
    }

    @Override
    public int insertPersonalItemWithLog(Long userId, String userName, PersonalItem personalItem) {
        if (userId == null || personalItem == null) {
            return 0;
        }
        personalItem.setUserId(userId);
        int rows = insertPersonalItem(personalItem);
        if (rows > 0) {
            saveOperationLog(null, userId, safeUserName(userName), "add", personalItem,
                    "添加了物品\"" + safeText(personalItem.getName()) + "\"", buildAddOperationData(personalItem));
        }
        return rows;
    }

    @Override
    public int updatePersonalItem(PersonalItem personalItem) {
        if (personalItem == null || personalItem.getId() == null) {
            return 0;
        }
        PersonalItem existing = personalItemMapper.selectById(personalItem.getId());
        if (existing == null) {
            return 0;
        }
        // 防止更新请求未携带关键字段时把状态/创建时间置空，导致列表被过滤。
        if (personalItem.getUserId() == null) {
            personalItem.setUserId(existing.getUserId());
        }
        if (personalItem.getStatus() == null || personalItem.getStatus().trim().isEmpty()) {
            personalItem.setStatus(existing.getStatus() == null || existing.getStatus().trim().isEmpty()
                    ? STATUS_NORMAL
                    : existing.getStatus());
        }
        if (personalItem.getCreateTime() == null) {
            personalItem.setCreateTime(existing.getCreateTime() == null ? LocalDateTime.now() : existing.getCreateTime());
        }
        personalItem.setUpdateTime(LocalDateTime.now());
        return personalItemMapper.updateById(personalItem);
    }

    @Override
    public int updatePersonalItemWithLog(Long userId, String userName, PersonalItem personalItem) {
        if (userId == null || personalItem == null || personalItem.getId() == null) {
            return 0;
        }
        PersonalItem before = selectById(personalItem.getId());
        int rows = updatePersonalItem(personalItem);
        if (rows > 0) {
            saveOperationLog(null, userId, safeUserName(userName), "update", personalItem,
                    "更新了物品\"" + safeText(personalItem.getName()) + "\"", buildUpdateOperationData(personalItem, before));
        }
        return rows;
    }

    @Override
    public int deletePersonalItemById(Long id) {
        if (id == null) {
            return 0;
        }
        UpdateWrapper<PersonalItem> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("status", STATUS_DELETED)
                .set("update_time", LocalDateTime.now());
        return personalItemMapper.update(null, updateWrapper);
    }

    @Override
    public int deletePersonalItemByIdWithLog(Long userId, String userName, Long id, String opType) {
        if (userId == null || id == null) {
            return 0;
        }
        PersonalItem existing = selectById(id);
        int rows = deletePersonalItemById(id);
        if (rows > 0 && existing != null) {
            String operationType = "process".equalsIgnoreCase(opType) ? "process" : "delete";
            String operationDesc = "process".equalsIgnoreCase(opType)
                    ? "处理了过期物品\"" + safeText(existing.getName()) + "\""
                    : "删除了物品\"" + safeText(existing.getName()) + "\"";
            saveOperationLog(null, userId, safeUserName(userName), operationType, existing, operationDesc,
                    "{\"source\":\"" + operationType + "\",\"status\":\"" + STATUS_DELETED + "\"}");
        }
        return rows;
    }

    @Override
    public List<PersonalItem> selectByUserId(Long userId) {
        return personalItemMapper.selectByUserId(userId);
    }

    @Override
    public List<PersonalItem> selectByUserIdWithExpireLog(Long userId, String userName) {
        List<PersonalItem> list = selectByUserId(userId);
        if (userId == null || list == null || list.isEmpty()) {
            return list;
        }
        LocalDate today = LocalDate.now();
        String operatorName = safeUserName(userName);
        for (PersonalItem item : list) {
            if (item == null || item.getId() == null || item.getExpiryDate() == null) {
                continue;
            }
            if (!item.getExpiryDate().isAfter(today)) {
                String expiryDate = String.valueOf(item.getExpiryDate());
                if (!operationLogService.hasExpireLog(item.getId(), ITEM_TYPE_PERSONAL, expiryDate)) {
                    saveOperationLog(null, userId, operatorName, "expire", item,
                            "物品已过期\"" + safeText(item.getName()) + "\"", "{\"expiryDate\":\"" + expiryDate + "\"}");
                }
            }
        }
        return list;
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
            count += deletePersonalItemById(id);
        }
        return count;
    }

    @Override
    public PersonalItem selectById(Long id) {
        return personalItemMapper.selectById(id);
    }

    private void saveOperationLog(Long spaceId, Long userId, String userName, String operationType, PersonalItem item,
                                  String operationDesc, String operationData) {
        if (userId == null || item == null || item.getId() == null) {
            return;
        }
        OperationLog log = new OperationLog();
        log.setSpaceId(spaceId);
        log.setItemId(item.getId());
        log.setItemType(ITEM_TYPE_PERSONAL);
        log.setOperatorId(userId);
        log.setOperatorName(userName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setOperationData(operationData);
        log.setOperationTime(LocalDateTime.now());
        operationLogService.insertOperationLog(log);
    }

    private String buildAddOperationData(PersonalItem item) {
        return "{\"category\":\"" + safeText(item.getCategory())
                + "\",\"expiryDate\":\"" + safeText(item.getExpiryDate()) + "\"}";
    }

    private String buildUpdateOperationData(PersonalItem item, PersonalItem before) {
        return "{\"name\":\"" + safeText(item.getName())
                + "\",\"category\":\"" + safeText(item.getCategory())
                + "\",\"expiryDate\":\"" + safeText(item.getExpiryDate())
                + "\",\"beforeExpiryDate\":\"" + safeText(before == null ? null : before.getExpiryDate()) + "\"}";
    }

    private String safeUserName(String userName) {
        String value = safeText(userName).trim();
        return value.isEmpty() ? "我" : value;
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

}
