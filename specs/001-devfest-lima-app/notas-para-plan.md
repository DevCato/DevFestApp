# Notas técnicas para `/speckit.plan`

> Pega o referencia este archivo al ejecutar `/speckit.plan`. El `spec.md` describe **qué** hace la app; aquí van las decisiones de **cómo** ya tomadas.

## Identidad

- Paquete / applicationId / bundle ID: **`pe.gdg.open.devfest.app`**

## Stack

- **Kotlin Multiplatform** (proyecto creado con kmp.jetbrains.com), targets **Android** e **iOS**.
- **Compose Multiplatform** para la UI compartida (el diseño es propio, no nativo de cada plataforma).
- Mínimos: **Android 8.0 (API 26)**, **iOS 16**.
- Solo español, sin modo oscuro.

## Autenticación

- **Firebase Auth** con Google, Sign in with Apple y GitHub.
- Los flujos de login son específicos de cada plataforma → `expect/actual` (o un SDK de Firebase para KMP evaluado en el plan).
- Cuando exista el backend propio, la app le envía el **Firebase ID token** en cada request; el backend lo verifica.

## Datos

- **Fase actual: fake data local** (ver `spec.md`, Appendix B) detrás de interfaces de repositorio (`EventRepository`, `AgendaRepository`, `UserRepository`, `GemsRepository`, `RankingRepository`, `AuthRepository`).
- **Después: backend propio** según [`contracts/api.yaml`](contracts/api.yaml). Los DTOs del contrato se mapean a modelos de dominio; si el backend real difiere, se escribe un **adapter** (DTO → dominio) sin tocar la UI ni los ViewModels.
- Persistencia local para agenda cacheada y charlas guardadas (offline-first). Las charlas guardadas son solo locales (no hay endpoint).
- Fechas/horas con zona horaria **America/Lima**.
- **LIVE NOW** = `Session.isLive` del backend. **Línea AHORA, sesiones pasadas y "próxima charla"** = hora del teléfono convertida a America/Lima (inyectar un `Clock` para poder testear).
- **Gemas**: la app nunca calcula cantidades; muestra `gemsAwarded` / `newBalance` que devuelve `POST /scans`. No hay canjes.

## Librerías sugeridas (confirmar versiones actuales al planear)

- Red: Ktor Client + kotlinx.serialization.
- Fechas: kotlinx-datetime.
- Persistencia: SQLDelight o Room KMP; DataStore para preferencias.
- DI: Koin.
- Estado / navegación: ViewModel y Navigation Compose multiplatform.
- Imágenes: Coil 3.
- Escaneo QR: CameraX + ML Kit (Android) / AVFoundation (iOS) vía `expect/actual` o librería KMP evaluada en el plan.

## Sistema de diseño (del prototipo)

- Fuente de verdad visual: https://claude.ai/artifact/H9uMTBqaSt4rxrX6XVxxyV
- Tipografías: **Outfit** (400–800) y **JetBrains Mono** (500–600) como Compose resources.
- Colores: fondo `#F1F1F1` con patrón de puntos, tarjeta `#FFFFFF`, tinta `#1E1E1E`, texto secundario `#5F6368`, suave `#ECECEC`, celeste `#57CAFF` / `#C6ECFF`, amarillo `#FBBC04`, rojo en vivo `#D93025`, tracks: IA `#4285F4`, Web `#A142F4`, Mobile `#34A853`, Cloud `#FBBC04`, General `#9AA0A6`.
- Bordes 2–2.5 dp en tinta; radios 14–28 dp; sombras **sólidas desplazadas** (sin blur) 3–7 dp.
- Centralizar tokens en un tema propio (`MaterialTheme` extendido o `CompositionLocal`) y componentes reutilizables: tarjeta de charla, chip de track, botón con sombra, barra inferior, cabecera con gemas, sticker decorativo.

## Modal genérico (patrón Builder)

Un solo composable de modal + un builder para configurarlo donde se necesite (FR-065). Referencia visual: pantalla 08 del prototipo.

```kotlin
val modal = AppModal.Builder()
    .title("No se pudo actualizar")
    .message("Revisa tu conexión e inténtalo de nuevo.")
    .icon(AppIcons.Refresh)
    .iconBackground(DevFestColors.PurpleTint)   // fondo del ícono girado -6°
    .iconTint(DevFestColors.Ink)
    .shadowColor(DevFestColors.Purple)           // sombra sólida de la tarjeta
    .primaryButton("Intentar de nuevo") { viewModel.retry() }
    .secondaryButton("Cerrar")                   // opcional → 1 o 2 botones
    .dismissOnOutsideTap(true)
    .build()

modalHost.show(modal)   // un único host en la raíz de la app
```

- `AppModal` es inmutable; el builder valida que haya título y botón principal.
- `ModalHost` (en la raíz) muestra uno a la vez; los ViewModels emiten `ModalRequest` como evento y no conocen la UI.
- Presets opcionales para los casos frecuentes (`Modals.sessionExpired()`, `Modals.noConnection(onRetry)`, `Modals.confirmLogout(savedCount, onConfirm)`), construidos con el mismo builder.
- Si el botón principal usa sombra de tinta, el botón usa sombra celeste `#57CAFF` (como en el prototipo).

## Carga, recarga y sin conexión

- Agenda y Mi agenda: `refresh()` al entrar a la pantalla y con pull-to-refresh (`PullToRefreshBox` o equivalente).
- Primera carga: esqueletos (placeholder shimmer, se apaga con reducir movimiento).
- Sin agenda cacheada + sin red → estado "Sin conexión" con "Intentar de nuevo" (pantalla 02c).
- Recarga fallida con caché → modal "No se pudo actualizar" construido con el builder.
- Cerrar sesión: modal de confirmación → al confirmar se borran las charlas guardadas y se cierra la sesión de Firebase.

## Movimiento (equivalentes en Compose)

- Presión de botones: escala/offset 100–150 ms con ease-out fuerte (`CubicBezierEasing(0.23f, 1f, 0.32f, 1f)`).
- Transiciones: push/pop ~220–280 ms; pestañas Agenda/Mi agenda **sin animación**.
- Stickers flotantes (Login, Perfil, cabecera de Agenda): `rememberInfiniteTransition`, ida y vuelta, ritmos distintos por sticker.
- Contador de gemas 0 → saldo: `animateIntAsState` ~900 ms, easeOutExpo, tras la entrada de la pantalla.
- "LIVE NOW": punto que late (loop 1.4 s).
- **Reducir movimiento**: `expect/actual` (Android: escala de animaciones del sistema; iOS: `UIAccessibility.isReduceMotionEnabled`).

## Tests

- Tests de la lógica compartida (`commonTest`): cálculo de estados en vivo/pasada/AHORA, cruces de horario, tarjeta destacada de Mi agenda, filtros, armado de la lista del ranking (podio, 4–10, "• • •", fila propia).
