import SwiftUI

/// 对应 `pages/profile/profile`；会员入口对齐 `pages/member/*`。
public struct ProfileRootView: View {
    @Environment(SessionStore.self) private var session

    public init() {}

    public var body: some View {
        List {
            Section("会员") {
                NavigationLink("会员中心") {
                    Text("member/member · 占位").navigationTitle("会员中心")
                }
                NavigationLink("升级会员") {
                    Text("member/upgrade · 占位").navigationTitle("升级会员")
                }
            }
            Section("账户") {
                Button("退出登录", role: .destructive) {
                    session.signOut()
                }
            }
        }
        .navigationTitle("我的")
        .toolbarBackground(AppTheme.light.navigationBarColor, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
