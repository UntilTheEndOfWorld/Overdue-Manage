import Foundation

/// 对应 uni-app `tabBar.list` 五个 Tab。
public enum MainTab: Int, CaseIterable, Identifiable, Sendable {
    case home = 0
    case personal = 1
    case shared = 2
    case calendar = 3
    case profile = 4

    public var id: Int { rawValue }

    public var title: String {
        switch self {
        case .home: return "首页"
        case .personal: return "个人"
        case .shared: return "共享"
        case .calendar: return "日历"
        case .profile: return "我的"
        }
    }

    public var systemImage: String {
        switch self {
        case .home: return "house.fill"
        case .personal: return "person.fill"
        case .shared: return "person.3.fill"
        case .calendar: return "calendar"
        case .profile: return "ellipsis.circle.fill"
        }
    }
}
