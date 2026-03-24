import Foundation
import Observation

/// 会话与登录态；支持 **Sign in with Apple** 与后续后端 JWT（对齐原 `auth.js` Bearer）。
@MainActor
@Observable
public final class SessionStore {
    public var isLoggedIn: Bool
    /// 业务接口使用的 Bearer（应由服务端登录接口签发，**不要用 Apple 的 identityToken 长期直连业务 API**）。
    public var accessToken: String?
    public var userId: String?
    public var pendingInviteCode: String?

    /// Apple 用户稳定标识（`credential.user`），可与服务端账号绑定。
    public var appleUserIdentifier: String?
    /// Apple 返回的 JWT，用于调用你们后端的「苹果登录」接口换发自己的 token。
    public var appleIdentityToken: String?
    public var appleEmail: String?

    public init(
        isLoggedIn: Bool = false,
        accessToken: String? = nil,
        userId: String? = nil,
        pendingInviteCode: String? = nil,
        appleUserIdentifier: String? = nil,
        appleIdentityToken: String? = nil,
        appleEmail: String? = nil
    ) {
        self.isLoggedIn = isLoggedIn
        self.accessToken = accessToken
        self.userId = userId
        self.pendingInviteCode = pendingInviteCode
        self.appleUserIdentifier = appleUserIdentifier
        self.appleIdentityToken = appleIdentityToken
        self.appleEmail = appleEmail
    }

    /// 使用 Apple 登录结果更新会话。
    /// - Note: 生产环境应在 `exchangeAppleTokenWithBackend` 成功后再把服务端 JWT 写入 `accessToken`。
    public func signInWithApple(_ payload: AppleSignInPayload) async {
        appleUserIdentifier = payload.userIdentifier
        appleIdentityToken = payload.identityToken
        appleEmail = payload.email
        userId = payload.userIdentifier

        await exchangeAppleTokenWithBackend(payload)
    }

    /// 调用后端：用 `identityToken`（及可选 `authorizationCode`）换取业务 JWT。
    /// 当前为占位实现：未接后端时仅标记已登录，**不会**把 Apple JWT 当作长期 `accessToken`。
    public func exchangeAppleTokenWithBackend(_ payload: AppleSignInPayload) async {
        // TODO: 使用 APIClient POST，例如 /api/auth/apple，body: identityToken, userIdentifier, fullName, email
        // let jwt = try await AuthService.exchangeApple(payload)
        // accessToken = jwt

        // 开发阶段：无后端时可临时用占位 token，便于走通 UI；联调后删除。
        #if DEBUG
        accessToken = "debug-placeholder-use-backend-jwt"
        #else
        accessToken = nil
        #endif

        isLoggedIn = true
    }

    public func signOut() {
        isLoggedIn = false
        accessToken = nil
        userId = nil
        appleUserIdentifier = nil
        appleIdentityToken = nil
        appleEmail = nil
    }

    public func restoreFromStorage() {
        // TODO: Keychain 恢复 appleUserIdentifier + 服务端 JWT
    }
}
