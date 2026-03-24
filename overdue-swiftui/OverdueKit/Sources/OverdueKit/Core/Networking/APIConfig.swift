import Foundation

/// 与 `api.js` 中 `BASE_URL` 对齐；Debug 可改为本地或代理。
public enum APIConfig: Sendable {
    public static var baseURL: URL {
        URL(string: "https://www.lycc.ltd/jx_slr_api")!
    }

    public static var defaultTimeout: TimeInterval { 30 }
}
