# Research: App móvil DevFest Lima 2026

**Fecha**: 2026-09-30 | **Spec**: [spec.md](spec.md) | **Notas de entrada**: [notas-para-plan.md](notas-para-plan.md)

Las decisiones ya tomadas en `notas-para-plan.md` (stack, Firebase Auth, repositorios, Koin,
Navigation Compose, Coil 3, kotlinx-datetime, modal Builder, movimiento) se adoptan tal cual.
Este documento resuelve solo lo que las notas dejaron abierto ("evaluado en el plan").

**Versiones**: el catálogo actual fija Kotlin 2.4.20, Compose Multiplatform 1.12.1 y AGP 9.1.1.
Las versiones de las librerías nuevas NO se verificaron en este plan: se fija la última estable
compatible con ese trío al añadir cada una a `gradle/libs.versions.toml`.

---

## R1. Inicio de sesión con Firebase Auth en KMP

- **Decisión**: interfaz `AuthGateway` en `commonMain` con una implementación por plataforma
  sobre el SDK nativo de Firebase.
  - Android (`androidMain`): Firebase Auth Android + Credential Manager para Google;
    `OAuthProvider` de Firebase para Apple y GitHub.
  - iOS: clase Swift en `iosApp` que implementa `AuthGateway` (Firebase iOS por SPM,
    GoogleSignIn, `ASAuthorization` para Apple, `OAuthProvider` para GitHub) y se pasa a
    `MainViewController(...)` al arrancar.
  - `FakeAuthGateway` en `commonMain` para tests y para compilar sin archivos de Firebase.
- **Justificación**: los flujos de los tres proveedores necesitan UI y ciclo de vida nativos
  (Activity, `UIViewController`). Implementar la interfaz en Swift evita CocoaPods/cinterop de
  Firebase en el módulo `shared`.
- **Alternativas consideradas**:
  - SDK de Firebase para KMP (GitLive): cubre `signInWithCredential`, pero no los flujos de UI
    de los proveedores, así que el código nativo sería igualmente necesario y además añade el
    enlazado de Firebase iOS al framework compartido.
  - `expect/actual` puro en `iosMain`: obliga a exponer Firebase iOS a Kotlin/Native.

## R2. Alcance del "fake data" frente al login real

- **Decisión**: el inicio de sesión es real (Firebase Auth). Todo lo que en el contrato viene
  del backend (`/event`, `/agenda`, `/me`, `/gems/info`, `/scans`, `/ranking`) es fake. El
  nombre, la foto y el proveedor que devuelve el `/me` fake se toman de la sesión de Firebase.
- **Justificación**: la spec pide los tres proveedores con Firebase (FR-001) y a la vez datos
  falsos para todo lo del backend (FR-070); esta lectura cumple ambas.
- **Alternativas consideradas**: login también simulado — no permitiría validar US1 ni SC-001.

## R3. Punto de intercambio fake → backend

- **Decisión**: una interfaz `DevFestApi` en la capa de datos con una operación por cada
  `operationId` de `contracts/api.yaml`, que devuelve los DTOs del contrato. Hoy se usa
  `FakeDevFestApi`; después, `KtorDevFestApi`. Los repositorios (caché, mapeo DTO → dominio,
  errores) son reales desde ahora y no cambian.
- **Justificación**: la lógica offline-first y los mappers quedan probados hoy con el fake, y el
  cambio a backend es una sola clase nueva. Si el backend real difiere, el adapter vive en esa
  clase. Las interfaces de repositorio que consume la UI no se tocan (Principio III).
- **Alternativas consideradas**: un `Fake*Repository` por repositorio — duplicaría después la
  lógica de caché y dejaría sin probar los mappers hasta que exista el backend.

## R4. Red

- **Decisión**: Ktor Client (motor OkHttp en Android, Darwin en iOS) + kotlinx.serialization.
  En esta versión Ktor solo se usa como cargador de red de Coil; `KtorDevFestApi` se escribe
  cuando exista el backend. kotlinx.serialization se usa ya para los DTOs y la caché.
- **Justificación**: decisión de las notas; Coil 3 necesita un cliente de red en KMP.

## R5. Persistencia local

- **Decisión**: DataStore Preferences (KMP) para todo: ids de charlas guardadas, y la última
  respuesta de agenda y de evento como JSON serializado del DTO del contrato.
- **Justificación**: se guarda un documento de agenda (12 sesiones) y un conjunto de ids; no
  hay consultas ni relaciones. Las notas ya incluyen DataStore.
- **Alternativas consideradas**: SQLDelight o Room KMP (sugeridas en las notas) — esquema,
  migraciones y drivers por plataforma sin ninguna consulta que los aproveche. Se reconsidera
  si aparece contenido paginado o consultable.

## R6. Fechas, horas y reloj

- **Decisión**: `kotlin.time.Instant` y `kotlin.time.Clock` (stdlib) para instantes;
  kotlinx-datetime para `TimeZone("America/Lima")` y `LocalDateTime`. Toda la lógica recibe un
  `Clock` inyectado; en producción `Clock.System`.
- **Justificación**: permite probar AHORA, sesiones pasadas y "próxima charla" con horas fijas
  (SC-008). Las horas se muestran siempre en hora de Lima, en formato 24 h.

## R7. Escaneo de QR

- **Decisión**: composable `QrScannerView` con `expect/actual`.
  - Android: CameraX + ML Kit Barcode Scanning (modelo incluido en la app).
  - iOS: AVFoundation (`AVCaptureMetadataOutput`) desde `iosMain`.
  - El permiso de cámara y "Abrir configuración" van en la misma abstracción de plataforma.
- **Justificación**: decisión preferida en las notas; el modelo incluido no depende de una
  descarga de Google Play Services el día del evento.
- **Alternativas consideradas**: librería KMP de escaneo — menos código, pero depende de un
  tercero para la función central del sistema de gemas y su compatibilidad con Compose
  Multiplatform 1.12 no está verificada.

## R8. "Reducir movimiento"

- **Decisión**: `expect fun` observable por plataforma, expuesto a la UI como
  `LocalReduceMotion` (`CompositionLocal`).
  - Android: `Settings.Global.ANIMATOR_DURATION_SCALE == 0`.
  - iOS: `UIAccessibilityIsReduceMotionEnabled()` y su notificación de cambio.
- **Justificación**: decisión de las notas y Principio V. Un único punto de lectura permite
  que cada animación declare su variante reducida (FR-060).

## R9. Solo tema claro

- **Decisión**: tema propio `DevFestTheme` con tokens fijos, sin leer `isSystemInDarkTheme()`.
  Android: tema claro de la app y `forceDarkAllowed=false`. iOS: `UIUserInterfaceStyle = Light`
  en `Info.plist`.
- **Justificación**: Principio IV; evita que el sistema invierta colores por su cuenta.

## R10. Textos y tipografías

- **Decisión**: todos los textos en `composeResources/values/strings.xml` (único idioma, en
  español). Tipografías Outfit y JetBrains Mono en `composeResources/font`.
- **Justificación**: Principio IV. Los textos que entrega el backend llegan listos para mostrar.

## R11. Navegación, estado y DI

- **Decisión**: Navigation Compose multiplatform con rutas tipadas; un `ViewModel` por
  pantalla que expone `StateFlow<UiState>`; Koin para DI. Las pestañas Agenda / Mi agenda
  cambian sin animación; el resto usa push/pop.
- **Justificación**: decisión de las notas. La pila real de navegación cumple FR-057.

## R12. Modales y eventos de sesión

- **Decisión**: `AppModal` + `AppModal.Builder` + `ModalHost` en la raíz, como en las notas.
  Los ViewModels emiten `ModalRequest`. Un `SessionEvents` a nivel de app recibe el error
  `SessionExpired` de cualquier repositorio, muestra el modal y navega a Login sin borrar las
  charlas guardadas.
- **Justificación**: FR-065 a FR-067 y el caso "Sesión vencida" de la spec.

## R13. Tests

- **Decisión**: `kotlin-test` + `kotlinx-coroutines-test` en `commonTest`. Se prueban la lógica
  pura de dominio, los mappers DTO → dominio, los repositorios con `FakeDevFestApi` y los
  ViewModels. Sin tests de UI automatizados en esta versión.
- **Justificación**: Principio II y SC-008. La spec pide tests de la lógica compartida.

---

## Pendientes que no bloquean el plan

| Pendiente | Quién | Efecto mientras falta |
|---|---|---|
| `google-services.json` y `GoogleService-Info.plist` del proyecto Firebase | Equipo | La app arranca con `FakeAuthGateway` |
| URLs de Términos y Política de privacidad | Equipo | Constantes vacías en `AppConfig` |
| URL real del backend | Backend | No aplica a esta versión |
| Diseño de esqueletos, ícono y splash | Diseño | Esqueletos según FR-018; ícono por defecto |
