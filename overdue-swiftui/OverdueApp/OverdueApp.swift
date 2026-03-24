import SwiftUI
import OverdueKit

@main
struct OverdueApp: App {
    @State private var session = SessionStore()

    var body: some Scene {
        WindowGroup {
            AppRootView()
                .environment(session)
        }
    }
}
