import SwiftUI

/// 应用根视图：登录态与主 Tab（对齐 `App.vue` 中 `checkAuth` + `tabBar`）。
public struct AppRootView: View {
    @Environment(SessionStore.self) private var session

    public init() {}

    public var body: some View {
        Group {
            if session.isLoggedIn {
                MainTabView()
            } else {
                LoginView()
            }
        }
        .tint(AppTheme.light.tabSelected)
    }
}
