import SwiftUI

/// 对齐小程序 `globalStyle` / 主题工具：主色 `#6a89cc`、背景 `#f8f9fa`。
public enum AppTheme: String, CaseIterable, Sendable {
    case light
    case dark

    public var navigationBarColor: Color {
        Color(red: 0x6a / 255, green: 0x89 / 255, blue: 0xcc / 255)
    }

    public var background: Color {
        switch self {
        case .light: return Color(red: 0xf8 / 255, green: 0xf9 / 255, blue: 0xfa / 255)
        case .dark: return Color(red: 0.07, green: 0.07, blue: 0.09)
        }
    }

    public var tabSelected: Color {
        Color(red: 0x3b / 255, green: 0x82 / 255, blue: 0xf6 / 255)
    }

    public var tabNormal: Color {
        Color(red: 0x94 / 255, green: 0xa3 / 255, blue: 0xb8 / 255)
    }
}
