import SwiftUI
import ComposeApp

@main
struct BeThereCentralApp: App {
    @State private var lightMap = false

    var body: some Scene {
        WindowGroup {
            SharedContent(isLight: $lightMap)
                .ignoresSafeArea()
                .background((lightMap
                    ? Color(red: 255 / 255, green: 248 / 255, blue: 244 / 255)
                    : Color(red: 12 / 255, green: 17 / 255, blue: 20 / 255)).ignoresSafeArea())
                .preferredColorScheme(lightMap ? .light : .dark)
        }
    }
}

private struct SharedContent: UIViewControllerRepresentable {
    @Binding var isLight: Bool

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(onAppearanceChanged: { light in
            DispatchQueue.main.async { isLight = light.boolValue }
        })
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
