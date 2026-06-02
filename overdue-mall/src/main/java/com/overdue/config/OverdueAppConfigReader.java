package com.overdue.config;

import com.overdue.common.utils.StringUtils;
import com.overdue.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 从 sys_config 读取过期了吗小程序应用配置。
 * 在后台「参数设置」中修改后会更新 Redis 缓存，无需重启服务。
 */
@Service
public class OverdueAppConfigReader {

    private static final boolean DEFAULT_CHARGE_ENABLED = false;
    private static final int DEFAULT_PERSONAL_LIMIT = 10;
    private static final int DEFAULT_SHARED_SPACE_LIMIT = 1;
    private static final int DEFAULT_SHARED_ITEM_LIMIT = 10;

    @Autowired
    private ISysConfigService sysConfigService;

    public boolean isChargeEnabled() {
        String raw = getRaw(OverdueAppConfigKeys.CHARGE_ENABLED);
        if (StringUtils.isEmpty(raw)) {
            return DEFAULT_CHARGE_ENABLED;
        }
        return parseBoolean(raw);
    }

    public int getFreePersonalItemLimit() {
        Integer configured = parsePositiveInt(getRaw(OverdueAppConfigKeys.FREE_PERSONAL_ITEM_LIMIT));
        return configured != null ? configured : DEFAULT_PERSONAL_LIMIT;
    }

    public int getFreeSharedSpaceLimit() {
        Integer configured = parsePositiveInt(getRaw(OverdueAppConfigKeys.FREE_SHARED_SPACE_LIMIT));
        return configured != null ? configured : DEFAULT_SHARED_SPACE_LIMIT;
    }

    public int getFreeSharedItemLimit() {
        Integer configured = parsePositiveInt(getRaw(OverdueAppConfigKeys.FREE_SHARED_ITEM_LIMIT));
        return configured != null ? configured : DEFAULT_SHARED_ITEM_LIMIT;
    }

    private String getRaw(String key) {
        try {
            String value = sysConfigService.selectConfigByKey(key);
            return StringUtils.isEmpty(value) ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean parseBoolean(String value) {
        if (value == null) {
            return DEFAULT_CHARGE_ENABLED;
        }
        String normalized = value.trim().toLowerCase();
        return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized);
    }

    private static Integer parsePositiveInt(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
