package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.OperationLog;
import com.overdue.manager.item.mapper.OperationLogMapper;
import com.overdue.manager.item.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 操作日志Service业务层处理
 * 
 * @author overdue
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLog> selectOperationLogList(OperationLog operationLog) {
        return operationLogMapper.selectOperationLogList(operationLog);
    }

    @Override
    public int insertOperationLog(OperationLog operationLog) {
        return operationLogMapper.insert(operationLog);
    }

    @Override
    public List<OperationLog> selectBySpaceId(Long spaceId) {
        return operationLogMapper.selectBySpaceId(spaceId);
    }

    @Override
    public List<OperationLog> selectByItemId(Long itemId, String itemType) {
        return operationLogMapper.selectByItemId(itemId, itemType);
    }

    @Override
    public List<OperationLog> selectPersonalLogs(Long operatorId, Long itemId, String operationType) {
        return operationLogMapper.selectPersonalLogs(operatorId, itemId, operationType);
    }

    @Override
    public boolean hasExpireLog(Long itemId, String itemType, String expiryDate) {
        if (itemId == null || itemType == null || expiryDate == null || expiryDate.trim().isEmpty()) {
            return false;
        }
        return operationLogMapper.countExpireLogByItemAndDate(itemId, itemType, expiryDate) > 0;
    }
}
