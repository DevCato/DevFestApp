import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController(authGateway: AuthSetup.gateway)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        // Edge to edge: Compose dibuja detrás de la barra de estado y del indicador de inicio.
        // Cada pantalla deja el espacio necesario con los WindowInsets de Compose.
        ComposeView()
            .ignoresSafeArea()
    }
}
