import Foundation

/// 与 `overdue-miniUI/pages.json` 中页面一一对应，用于 `NavigationStack` 深链与程序化跳转。
public enum AppRoute: Hashable, Sendable {
    // MARK: - Tab 根（与 tabBar 一致）
    case home
    case personalRoot
    case sharedRoot
    case calendarRoot
    case profileRoot

    // MARK: - 个人空间
    case personalAddItem
    case personalEditItem(itemId: String)
    case personalLogs

    // MARK: - 共享空间
    case sharedDetail(spaceId: String)
    case sharedCreate
    case sharedInvite(spaceId: String?)
    case sharedLogs(spaceId: String?)
    case sharedJoin(code: String?)

    // MARK: - 会员
    case memberCenter
    case memberUpgrade
    case memberAd
    case memberPurchase

    // MARK: - 认证
    case login
}
