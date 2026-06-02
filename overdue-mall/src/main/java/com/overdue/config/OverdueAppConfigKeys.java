package com.overdue.config;

/**
 * 过期了吗小程序应用配置 sys_config 键名。
 * 在后台「参数设置」中增改；键名需与本类及 doc/overdue_app_sys_config.sql 一致。
 */
public final class OverdueAppConfigKeys {

    private OverdueAppConfigKeys() {
    }

    /** 是否开启收费/会员：true/false/1/0 */
    public static final String CHARGE_ENABLED = "overdue.app.charge.enabled";

    /** 免费用户个人物品上限 */
    public static final String FREE_PERSONAL_ITEM_LIMIT = "overdue.app.free.personal.item.limit";

    /** 免费用户共享空间上限 */
    public static final String FREE_SHARED_SPACE_LIMIT = "overdue.app.free.shared.space.limit";

    /** 每个共享空间免费物品上限 */
    public static final String FREE_SHARED_ITEM_LIMIT = "overdue.app.free.shared.item.limit";
}
