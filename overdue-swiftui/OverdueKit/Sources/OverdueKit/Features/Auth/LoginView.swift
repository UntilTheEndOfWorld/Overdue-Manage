import AuthenticationServices
import SwiftUI

/// 使用 **Sign in with Apple**（对应系统能力，非微信登录）。
public struct LoginView: View {
    @Environment(SessionStore.self) private var session
    @State private var signInError: String?

    public init() {}

    public var body: some View {
        VStack(spacing: 24) {
            Text("过期了么")
                .font(.largeTitle.bold())
            Text("使用 Apple 账号登录")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            SignInWithAppleButton(.signIn) { request in
                request.requestedScopes = [.fullName, .email]
            } onCompletion: { result in
                switch result {
                case .success(let authorization):
                    guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
                        signInError = "无法读取 Apple 凭证"
                        return
                    }
                    do {
                        let payload = try AppleSignInPayloadBuilder.build(from: credential)
                        Task { @MainActor in
                            await session.signInWithApple(payload)
                        }
                    } catch {
                        signInError = error.localizedDescription
                    }
                case .failure(let error):
                    if let authError = error as? ASAuthorizationError, authError.code == .canceled {
                        signInError = nil
                        return
                    }
                    signInError = error.localizedDescription
                }
            }
            .signInWithAppleButtonStyle(.black)
            .frame(height: 50)
            .padding(.horizontal, 32)

            if let signInError, !signInError.isEmpty {
                Text(signInError)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }

            Text("首次登录时可选提供姓名与邮箱；服务端校验请使用 identityToken。")
                .font(.caption2)
                .foregroundStyle(.tertiary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTheme.light.background)
    }
}
