import SwiftUI
import Shared
#if canImport(GoogleSignIn)
import GoogleSignIn
#endif

/// Login de la app: Firebase si está configurado; si no, `nil` y la app usa el simulado.
enum AuthSetup {
    static let gateway: AuthGateway? = FirebaseAuthGateway.makeIfConfigured()
}

@main
struct iOSApp: App {
    init() {
        // Configura Firebase antes de que se cree la primera pantalla.
        _ = AuthSetup.gateway
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    #if canImport(GoogleSignIn)
                    GIDSignIn.sharedInstance.handle(url)
                    #endif
                }
        }
    }
}
