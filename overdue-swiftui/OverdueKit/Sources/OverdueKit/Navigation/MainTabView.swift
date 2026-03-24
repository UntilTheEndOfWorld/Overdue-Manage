import SwiftUI

/// 五 Tab 容器；各 Tab 内再用 `NavigationStack` 承载子页面（对应 uni-app 非 tab 页）。
public struct MainTabView: View {
    @State private var selected: MainTab = .home

    public init() {}

    public var body: some View {
        TabView(selection: $selected) {
            NavigationStack {
                HomeView()
            }
            .tabItem { Label(MainTab.home.title, systemImage: MainTab.home.systemImage) }
            .tag(MainTab.home)

            NavigationStack {
                PersonalRootView()
            }
            .tabItem { Label(MainTab.personal.title, systemImage: MainTab.personal.systemImage) }
            .tag(MainTab.personal)

            NavigationStack {
                SharedRootView()
            }
            .tabItem { Label(MainTab.shared.title, systemImage: MainTab.shared.systemImage) }
            .tag(MainTab.shared)

            NavigationStack {
                CalendarRootView()
            }
            .tabItem { Label(MainTab.calendar.title, systemImage: MainTab.calendar.systemImage) }
            .tag(MainTab.calendar)

            NavigationStack {
                ProfileRootView()
            }
            .tabItem { Label(MainTab.profile.title, systemImage: MainTab.profile.systemImage) }
            .tag(MainTab.profile)
        }
    }
}
