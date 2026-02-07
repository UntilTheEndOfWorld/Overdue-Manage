package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.AdWatchRecord;
import com.overdue.manager.item.mapper.AdWatchRecordMapper;
import com.overdue.manager.item.service.AdWatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告观看记录 Service实现
 *
 * @author overdue
 */
@Service
public class AdWatchRecordServiceImpl implements AdWatchRecordService {

    @Autowired
    private AdWatchRecordMapper adWatchRecordMapper;

    @Override
    public List<AdWatchRecord> selectAdWatchRecordList(AdWatchRecord adWatchRecord) {
        return adWatchRecordMapper.selectAdWatchRecordList(adWatchRecord);
    }

    @Override
    public AdWatchRecord selectByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return adWatchRecordMapper.selectByUserId(userId);
    }

    @Override
    public AdWatchRecord selectById(Long id) {
        if (id == null) {
            return null;
        }
        return adWatchRecordMapper.selectById(id);
    }

    @Override
    public int insertAdWatchRecord(AdWatchRecord adWatchRecord) {
        return adWatchRecordMapper.insert(adWatchRecord);
    }

    @Override
    public int updateAdWatchRecord(AdWatchRecord adWatchRecord) {
        if (adWatchRecord == null || adWatchRecord.getId() == null) {
            return 0;
        }
        return adWatchRecordMapper.updateById(adWatchRecord);
    }

    @Override
    public int deleteAdWatchRecordById(Long id) {
        if (id == null) {
            return 0;
        }
        return adWatchRecordMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdWatchRecord watchAd(Long userId) {
        if (userId == null) {
            return null;
        }
        AdWatchRecord record = adWatchRecordMapper.selectByUserId(userId);
        if (record == null) {
            record = new AdWatchRecord();
            record.setUserId(userId);
            record.setWatchCount(1);
            record.setUsedCount(0);
            record.setLastWatchTime(LocalDateTime.now());
            adWatchRecordMapper.insert(record);
        } else {
            record.setWatchCount(record.getWatchCount() + 1);
            record.setLastWatchTime(LocalDateTime.now());
            adWatchRecordMapper.updateById(record);
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useQuota(Long userId) {
        if (userId == null) {
            return false;
        }
        AdWatchRecord record = adWatchRecordMapper.selectByUserId(userId);
        if (record == null) {
            return false;
        }
        int remaining = record.getWatchCount() - record.getUsedCount();
        if (remaining <= 0) {
            return false;
        }
        record.setUsedCount(record.getUsedCount() + 1);
        adWatchRecordMapper.updateById(record);
        return true;
    }

    @Override
    public int getRemainingQuota(Long userId) {
        if (userId == null) {
            return 0;
        }
        AdWatchRecord record = adWatchRecordMapper.selectByUserId(userId);
        if (record == null) {
            return 0;
        }
        return Math.max(0, record.getWatchCount() - record.getUsedCount());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Integer totalWatch = adWatchRecordMapper.sumWatchCount();
        Integer totalUsed = adWatchRecordMapper.sumUsedCount();
        stats.put("totalWatchCount", totalWatch != null ? totalWatch : 0);
        stats.put("totalUsedCount", totalUsed != null ? totalUsed : 0);
        stats.put("totalRemaining", (totalWatch != null ? totalWatch : 0) - (totalUsed != null ? totalUsed : 0));
        return stats;
    }

    @Override
    public List<Map<String, Object>> getStatsByDate(String startDate, String endDate) {
        return adWatchRecordMapper.selectStatsByDate(startDate, endDate);
    }
}
