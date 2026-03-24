import SwiftUI

/// 对应 `pages/shared/list` 及详情、创建、邀请、加入、日志。
public struct SharedRootView: View {
    public init() {}

    public var body: some View {
        List {
            NavigationLink("创建共享空间") {
                Text("shared/create · 占位").navigationTitle("创建共享空间")
            }
            NavigationLink("加入共享空间") {
                Text("shared/join · 占位").navigationTitle("加入共享空间")
            }
        }
        .navigationTitle("共享空间")
        .toolbarBackground(AppTheme.light.navigationBarColor, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
