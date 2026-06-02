package com.overdue.config;

import com.overdue.manager.item.domain.entity.OverdueMember;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.domain.entity.SharedItem;
import com.overdue.manager.item.domain.entity.SharedSpace;
import com.overdue.manager.item.mapper.OverdueMemberMapper;
import com.overdue.manager.item.service.PersonalItemService;
import com.overdue.manager.item.service.SharedItemService;
import com.overdue.manager.item.service.SharedSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 过期了吗小程序应用配置与额度校验。
 */
@Service
public class OverdueAppConfigService {

    @Autowired
    private OverdueAppConfigReader configReader;
    @Autowired
    private OverdueMemberMapper overdueMemberMapper;
    @Autowired
    private PersonalItemService personalItemService;
    @Autowired
    private SharedSpaceService sharedSpaceService;
    @Autowired
    private SharedItemService sharedItemService;

    public Map<String, Object> buildPublicConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("chargeEnabled", configReader.isChargeEnabled());
        config.put("freePersonalItemLimit", configReader.getFreePersonalItemLimit());
        config.put("freeSharedSpaceLimit", configReader.getFreeSharedSpaceLimit());
        config.put("freeSharedItemLimit", configReader.getFreeSharedItemLimit());
        return config;
    }

    public boolean isActiveMember(Long userId) {
        if (userId == null || !configReader.isChargeEnabled()) {
            return false;
        }
        OverdueMember member = overdueMemberMapper.selectByUserId(userId);
        if (member == null || member.getIsMember() == null || member.getIsMember() != 1) {
            return false;
        }
        if ("lifetime".equalsIgnoreCase(member.getPlanType())) {
            return true;
        }
        LocalDateTime expireTime = member.getExpireTime();
        return expireTime == null || expireTime.isAfter(LocalDateTime.now());
    }

    public String checkPersonalItemQuota(Long userId) {
        if (userId == null || isActiveMember(userId)) {
            return null;
        }
        List<PersonalItem> items = personalItemService.selectByUserId(userId);
        int current = items == null ? 0 : items.size();
        int limit = configReader.getFreePersonalItemLimit();
        if (current >= limit) {
            if (configReader.isChargeEnabled()) {
                return "免费额度已用完，请升级会员或观看广告后继续添加";
            }
            return "个人物品已达上限（" + limit + "个）";
        }
        return null;
    }

    public String checkSharedSpaceQuota(Long userId) {
        if (userId == null || isActiveMember(userId)) {
            return null;
        }
        List<SharedSpace> spaces = sharedSpaceService.selectByUserId(userId);
        int current = spaces == null ? 0 : spaces.size();
        int limit = configReader.getFreeSharedSpaceLimit();
        if (current >= limit) {
            if (configReader.isChargeEnabled()) {
                return "共享空间已达上限，请升级会员后创建更多空间";
            }
            return "共享空间已达上限（" + limit + "个）";
        }
        return null;
    }

    public String checkSharedItemQuota(Long spaceId) {
        if (spaceId == null) {
            return "空间不存在";
        }
        SharedSpace space = sharedSpaceService.selectById(spaceId);
        if (space == null) {
            return "空间不存在";
        }
        if (isActiveMember(space.getCreatorId())) {
            return null;
        }
        List<SharedItem> items = sharedItemService.selectBySpaceId(spaceId);
        int current = items == null ? 0 : items.size();
        int limit = configReader.getFreeSharedItemLimit();
        if (current >= limit) {
            if (configReader.isChargeEnabled()) {
                return "该空间物品已达上限，请升级会员后继续添加";
            }
            return "该空间物品已达上限（" + limit + "个）";
        }
        return null;
    }
}
