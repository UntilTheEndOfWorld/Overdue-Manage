import AuthenticationServices
import Foundation

/// 从 `ASAuthorizationAppleIDCredential` 解析出的、可序列化并发往后端的字段。
public struct AppleSignInPayload: Sendable {
    public let userIdentifier: String
    /// JWT 字符串，供服务端向 Apple 校验并签发你们自己的 session / JWT。
    public let identityToken: String
    /// 首次授权可用于服务端换 token；可能为 nil。
    public let authorizationCode: String?
    public let email: String?
    /// 仅首次登录时 Apple 可能返回姓名。
    public let fullNameFormatted: String?

    public init(
        userIdentifier: String,
        identityToken: String,
        authorizationCode: String?,
        email: String?,
        fullNameFormatted: String?
    ) {
        self.userIdentifier = userIdentifier
        self.identityToken = identityToken
        self.authorizationCode = authorizationCode
        self.email = email
        self.fullNameFormatted = fullNameFormatted
    }
}

public enum AppleSignInPayloadError: Error, Sendable {
    case missingIdentityToken
}

public enum AppleSignInPayloadBuilder {
    /// 从系统回调的凭证构建 payload；失败时抛出（如无 identityToken）。
    public static func build(from credential: ASAuthorizationAppleIDCredential) throws -> AppleSignInPayload {
        guard let tokenData = credential.identityToken,
              let identityToken = String(data: tokenData, encoding: .utf8) else {
            throw AppleSignInPayloadError.missingIdentityToken
        }

        let authCode: String?
        if let codeData = credential.authorizationCode {
            authCode = String(data: codeData, encoding: .utf8)
        } else {
            authCode = nil
        }

        let fullName: String?
        if let name = credential.fullName {
            let formatter = PersonNameComponentsFormatter()
            fullName = formatter.string(from: name)
        } else {
            fullName = nil
        }

        return AppleSignInPayload(
            userIdentifier: credential.user,
            identityToken: identityToken,
            authorizationCode: authCode,
            email: credential.email,
            fullNameFormatted: fullName
        )
    }
}
