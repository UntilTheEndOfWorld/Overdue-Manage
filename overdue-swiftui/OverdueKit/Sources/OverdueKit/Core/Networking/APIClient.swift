import Foundation

/// 统一 HTTP 客户端：Bearer Token、`X-User-Id`（对齐 `api.js`）。
/// 具体业务 DTO 解码在各 `Services` 中实现，此处仅提供原始请求。
public actor APIClient {
    private let session: URLSession
    private let tokenProvider: @Sendable () -> String?
    private let userIdProvider: @Sendable () -> String?

    public init(
        session: URLSession = .shared,
        tokenProvider: @escaping @Sendable () -> String?,
        userIdProvider: @escaping @Sendable () -> String?
    ) {
        self.session = session
        self.tokenProvider = tokenProvider
        self.userIdProvider = userIdProvider
    }

    public enum APIError: Error, Sendable {
        case invalidResponse
        case httpStatus(Int)
        case business(code: Int, message: String)
        case unauthorized
    }

    /// 返回原始 JSON Data，由上层按 `{ code, msg, data }` 解析（与 uni-app 一致）。
    public func data(path: String, method: String = "GET", jsonBody: Data? = nil) async throws -> Data {
        let base = APIConfig.baseURL.absoluteString.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let p = path.hasPrefix("/") ? path : "/" + path
        guard let url = URL(string: base + p) else { throw APIError.invalidResponse }

        var req = URLRequest(url: url)
        req.httpMethod = method
        req.timeoutInterval = APIConfig.defaultTimeout
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = tokenProvider(), !token.isEmpty {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let uid = userIdProvider(), !uid.isEmpty {
            req.setValue(uid, forHTTPHeaderField: "X-User-Id")
        }
        req.httpBody = jsonBody

        let (data, response) = try await session.data(for: req)
        guard let http = response as? HTTPURLResponse else { throw APIError.invalidResponse }
        if http.statusCode == 401 { throw APIError.unauthorized }
        guard (200 ... 299).contains(http.statusCode) else { throw APIError.httpStatus(http.statusCode) }
        return data
    }
}
