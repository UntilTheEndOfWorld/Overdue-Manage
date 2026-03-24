import SwiftUI

/// 对应 `pages/personal/personal` 及子路由（添加/编辑/日志）。
public struct PersonalRootView: View {
    public init() {}

    public var body: some View {
        List {
            Section("物品") {
                NavigationLink("添加物品") {
                    Text("add-item · 占位")
                        .navigationTitle("添加物品")
                }
                NavigationLink("操作日志") {
                    Text("personal/logs · 占位")
                        .navigationTitle("操作日志")
                }
            }
        }
        .navigationTitle("个人空间")
        .toolbarBackground(AppTheme.light.navigationBarColor, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
