import SwiftUI

/// 对应 `pages/calendar/calendar`。
public struct CalendarRootView: View {
    public init() {}

    public var body: some View {
        ContentUnavailableView(
            "过期日历",
            systemImage: "calendar",
            description: Text("占位 · 后续接入日历与到期数据")
        )
        .navigationTitle("过期日历")
        .toolbarBackground(AppTheme.light.navigationBarColor, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
