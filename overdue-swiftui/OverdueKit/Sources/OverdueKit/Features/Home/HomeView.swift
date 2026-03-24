import SwiftUI

/// 对应 `pages/index/index`。
public struct HomeView: View {
    public init() {}

    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("首页")
                    .font(.title2.bold())
                Text("占位 · 后续迁移 index.vue 业务")
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
        }
        .navigationTitle("过期了么")
        .toolbarBackground(AppTheme.light.navigationBarColor, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .background(AppTheme.light.background)
    }
}
