// Login con Firebase Auth en iOS (research.md, R1). Implementa la interfaz Kotlin `AuthGateway`.
//
// Para activarlo en Xcode:
//  1. Agregar por Swift Package Manager:
//     - https://github.com/firebase/firebase-ios-sdk  → FirebaseCore, FirebaseAuth
//     - https://github.com/google/GoogleSignIn-iOS     → GoogleSignIn
//  2. Copiar GoogleService-Info.plist en iosApp/iosApp/.
//  3. En Info.plist, agregar un URL Type con el REVERSED_CLIENT_ID de ese plist (Google).
//  4. En Signing & Capabilities, agregar "Sign in with Apple".
//  5. En la consola de Firebase, habilitar Google, Apple y GitHub como proveedores.
// Mientras falte algo de esto, `makeIfConfigured()` devuelve nil y la app usa el login simulado.

import Foundation
import Shared

#if canImport(FirebaseCore) && canImport(FirebaseAuth) && canImport(GoogleSignIn)
import AuthenticationServices
import CryptoKit
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import UIKit

final class FirebaseAuthGateway: NSObject, AuthGateway {

    /// Configura Firebase si la app tiene GoogleService-Info.plist.
    static func makeIfConfigured() -> AuthGateway? {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return nil }
        if FirebaseApp.app() == nil { FirebaseApp.configure() }
        return FirebaseAuthGateway()
    }

    /// Error de Firebase cuando el usuario cierra el flujo web (FIRAuthErrorCodeWebContextCancelled).
    private static let webContextCancelledCode = 17058

    private var appleDelegate: AppleSignInDelegate?
    private var gitHubProvider: OAuthProvider?

    func currentSession() -> AuthSession? {
        Auth.auth().currentUser.map { makeSession($0, provider: nil, fallbackName: nil) }
    }

    func signIn(provider: AuthProvider, onResult: @escaping (AuthSignInResult) -> Void) {
        let done: (AuthSignInResult) -> Void = { result in
            DispatchQueue.main.async { onResult(result) }
        }
        if provider == AuthProvider.google {
            signInWithGoogle(done)
        } else if provider == AuthProvider.apple {
            signInWithApple(done)
        } else if provider == AuthProvider.github {
            signInWithGitHub(done)
        } else {
            done(AuthSignInResultFailed(message: "Proveedor no soportado"))
        }
    }

    func signOut() {
        try? Auth.auth().signOut()
        GIDSignIn.sharedInstance.signOut()
    }

    func idToken(onResult: @escaping (String?) -> Void) {
        guard let user = Auth.auth().currentUser else {
            onResult(nil)
            return
        }
        user.getIDToken { token, _ in
            DispatchQueue.main.async { onResult(token) }
        }
    }

    // MARK: - Google

    private func signInWithGoogle(_ done: @escaping (AuthSignInResult) -> Void) {
        guard let clientID = FirebaseApp.app()?.options.clientID,
              let presenter = Self.topViewController() else {
            done(AuthSignInResultFailed(message: "Google no está configurado"))
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as NSError? {
                let cancelled = error.code == GIDSignInError.Code.canceled.rawValue
                done(cancelled ? AuthSignInResultCancelled.shared : AuthSignInResultFailed(message: error.localizedDescription))
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                done(AuthSignInResultFailed(message: nil))
                return
            }
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )
            self.signInToFirebase(credential, provider: AuthProvider.google, fallbackName: user.profile?.name, done)
        }
    }

    // MARK: - Apple

    private func signInWithApple(_ done: @escaping (AuthSignInResult) -> Void) {
        let nonce = Self.randomNonce()
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = Self.sha256(nonce)

        let delegate = AppleSignInDelegate(rawNonce: nonce) { [weak self] result in
            guard let self else { return }
            self.appleDelegate = nil
            switch result {
            case .cancelled:
                done(AuthSignInResultCancelled.shared)
            case .failure(let message):
                done(AuthSignInResultFailed(message: message))
            case .success(let credential, let name):
                self.signInToFirebase(credential, provider: AuthProvider.apple, fallbackName: name, done)
            }
        }
        appleDelegate = delegate

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        controller.performRequests()
    }

    // MARK: - GitHub

    private func signInWithGitHub(_ done: @escaping (AuthSignInResult) -> Void) {
        let provider = OAuthProvider(providerID: "github.com")
        provider.scopes = ["read:user"]
        gitHubProvider = provider
        provider.getCredentialWith(nil) { [weak self] credential, error in
            guard let self else { return }
            self.gitHubProvider = nil
            if let error = error as NSError? {
                let cancelled = error.code == Self.webContextCancelledCode
                done(cancelled ? AuthSignInResultCancelled.shared : AuthSignInResultFailed(message: error.localizedDescription))
                return
            }
            guard let credential else {
                done(AuthSignInResultFailed(message: nil))
                return
            }
            self.signInToFirebase(credential, provider: AuthProvider.github, fallbackName: nil, done)
        }
    }

    // MARK: - Firebase

    private func signInToFirebase(
        _ credential: AuthCredential,
        provider: AuthProvider,
        fallbackName: String?,
        _ done: @escaping (AuthSignInResult) -> Void
    ) {
        Auth.auth().signIn(with: credential) { result, error in
            if let error {
                done(AuthSignInResultFailed(message: error.localizedDescription))
                return
            }
            guard let user = result?.user else {
                done(AuthSignInResultFailed(message: nil))
                return
            }
            done(AuthSignInResultSuccess(session: self.makeSession(user, provider: provider, fallbackName: fallbackName)))
        }
    }

    /// `FirebaseAuth.User` explícito: el framework Shared también exporta un `User`.
    private func makeSession(_ user: FirebaseAuth.User, provider: AuthProvider?, fallbackName: String?) -> AuthSession {
        let name = [user.displayName, fallbackName, user.email]
            .compactMap { $0 }
            .first { !$0.isEmpty } ?? ""
        return AuthSession(
            userId: user.uid,
            fullName: name,
            photoUrl: user.photoURL?.absoluteString,
            provider: provider ?? Self.provider(of: user)
        )
    }

    private static func provider(of user: FirebaseAuth.User) -> AuthProvider {
        let ids = user.providerData.map { $0.providerID }
        if ids.contains("apple.com") { return AuthProvider.apple }
        if ids.contains("github.com") { return AuthProvider.github }
        return AuthProvider.google
    }

    // MARK: - Utilidades

    static func keyWindow() -> UIWindow? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }
    }

    private static func topViewController() -> UIViewController? {
        var top = keyWindow()?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }

    private static func randomNonce(length: Int = 32) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var generator = SystemRandomNumberGenerator()
        return String((0..<length).map { _ in charset.randomElement(using: &generator)! })
    }

    private static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }
}

/// Recibe el resultado de Sign in with Apple y arma la credencial de Firebase.
private final class AppleSignInDelegate: NSObject,
    ASAuthorizationControllerDelegate,
    ASAuthorizationControllerPresentationContextProviding {

    enum AppleResult {
        case success(AuthCredential, String?)
        case cancelled
        case failure(String?)
    }

    private let rawNonce: String
    private let completion: (AppleResult) -> Void

    init(rawNonce: String, completion: @escaping (AppleResult) -> Void) {
        self.rawNonce = rawNonce
        self.completion = completion
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        FirebaseAuthGateway.keyWindow() ?? ASPresentationAnchor()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let apple = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = apple.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            completion(.failure(nil))
            return
        }
        let credential = OAuthProvider.appleCredential(
            withIDToken: idToken,
            rawNonce: rawNonce,
            fullName: apple.fullName
        )
        // Apple entrega el nombre solo la primera vez que el usuario autoriza la app.
        let name = apple.fullName
            .map { PersonNameComponentsFormatter.localizedString(from: $0, style: .default) }
            .flatMap { $0.isEmpty ? nil : $0 }
        completion(.success(credential, name))
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            completion(.cancelled)
        } else {
            completion(.failure(error.localizedDescription))
        }
    }
}

#else

/// Sin los paquetes de Firebase la app usa el login simulado.
enum FirebaseAuthGateway {
    static func makeIfConfigured() -> AuthGateway? { nil }
}

#endif
