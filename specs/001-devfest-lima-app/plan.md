# Implementation Plan: App móvil DevFest Lima 2026

**Branch**: `001-devfest-lima-app` | **Date**: 2026-09-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-devfest-lima-app/spec.md`, decisiones técnicas
de [notas-para-plan.md](notas-para-plan.md) y contrato existente [contracts/api.yaml](contracts/api.yaml)
(se respeta sin modificarlo).

## Summary

App de asistentes para Android e iOS con 8 pantallas: login, agenda, mi agenda, detalle de
charla, perfil, mis gemas (con escáner QR) y ranking, más un modal genérico.

Enfoque técnico: un único módulo `shared` en Kotlin Multiplatform con la UI en Compose
Multiplatform. La UI consume seis repositorios; estos leen de una interfaz `DevFestApi` que
replica las operaciones de `contracts/api.yaml`. En esta versión se usa `FakeDevFestApi`; el
paso a backend es una implementación nueva (`KtorDevFestApi`) sin tocar repositorios ni
pantallas. El login es real con Firebase Auth. Agenda y charlas guardadas se guardan en el
dispositivo para funcionar sin conexión. La lógica de tiempo recibe un `Clock` inyectado y se
prueba en `commonTest`.

## Technical Context

**Language/Version**: Kotlin 2.4.20 (Kotlin Multiplatform); Swift solo para el punto de entrada
y el puente de Firebase Auth en iOS.

**Primary Dependencies**: Compose Multiplatform 1.12.1 (ya en el catálogo), Navigation Compose
multiplatform, Lifecycle ViewModel, Koin, kotlinx-coroutines, kotlinx.serialization,
kotlinx-datetime, Ktor Client, Coil 3, DataStore Preferences, Firebase Auth (SDK nativos),
CameraX + ML Kit (Android), AVFoundation (iOS). Las versiones de las librerías nuevas se fijan
al añadirlas al catálogo (ver [research.md](research.md)).

**Storage**: DataStore Preferences en el dispositivo (caché de agenda y evento como JSON; ids
de charlas guardadas). Sin base de datos.

**Testing**: `kotlin-test` y `kotlinx-coroutines-test` en `shared/src/commonTest`, ejecutados
con `:shared:testAndroidHostTest` y `:shared:iosSimulatorArm64Test`.

**Target Platform**: Android 8.0+ (`minSdk` 26) e iOS 16+.

**Project Type**: mobile-app (Kotlin Multiplatform, UI compartida).

**Performance Goals**: login → Agenda en menos de 30 s (SC-001); resultado de escaneo en menos
de 3 s con conexión normal (SC-004); desplazamiento fluido de la agenda (60 fps).

**Constraints**: Agenda y Mi agenda utilizables sin conexión; solo español; solo tema claro;
respeto a "reducir movimiento"; la app nunca calcula gemas; elementos tocables de 44×44 pt como
mínimo; contraste 4.5:1.

**Scale/Scope**: 8 pantallas y 9 casos de modal; un evento de un día con 12 sesiones; 6
operaciones de API; 6 repositorios.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Cumplimiento en este plan | Estado |
|---|---|---|
| I. Lógica compartida primero | Dominio, datos, ViewModels y UI en `commonMain`. Código de plataforma solo en las cinco abstracciones listadas abajo. | PASA |
| II. Lógica probada en commonTest | Tests de lógica de dominio, mappers, repositorios y ViewModels; deterministas por `Clock` inyectado y `FakeDevFestApi`. | PASA |
| III. Datos detrás de repositorios | Seis interfaces de repositorio con modelos de dominio y `Outcome`; fake hoy, `KtorDevFestApi` después. | PASA |
| IV. UI solo en español y tema claro | Textos en `composeResources/values/strings.xml`; `DevFestTheme` fijo; modo oscuro bloqueado en ambas plataformas. | PASA |
| V. Respeto a "reducir movimiento" | `LocalReduceMotion` leído del sistema; cada animación define su variante reducida (FR-060). | PASA |
| Restricciones de plataforma | Paquete `pe.gdg.open.devfest.app`; `minSdk` 26; iOS 16. | PASA con una corrección pendiente (abajo) |

**Código específico de plataforma (justificación exigida por el Principio I)**:

| Abstracción | Por qué no puede ser común |
|---|---|
| `AuthGateway` | Los flujos de Google, Apple y GitHub requieren UI y ciclo de vida nativos |
| `QrScannerView` y permiso de cámara | Acceso a la cámara y al detector de códigos del sistema |
| `openAppSettings()` | Abrir los ajustes de la app es una API del sistema |
| `reduceMotionFlow()` | La preferencia de accesibilidad es una API del sistema |
| Ruta del archivo de DataStore | El directorio de archivos lo entrega cada sistema |

**Corrección pendiente**: `gradle/libs.versions.toml` tiene `android-minSdk = "24"`; la
constitución y la spec exigen 26. Es la primera tarea de la implementación.

**Re-evaluación tras el diseño (Phase 1)**: PASA. El diseño no añade código de plataforma fuera
de la tabla ni acceso a datos fuera de los repositorios. No hay violaciones que justificar.

## Project Structure

### Documentation (this feature)

```text
specs/001-devfest-lima-app/
├── plan.md              # Este archivo
├── spec.md              # Especificación
├── notas-para-plan.md   # Decisiones técnicas de entrada
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   ├── api.yaml         # Contrato con el backend (existente, sin cambios)
│   └── repositories.md  # Contrato interno de repositorios (Phase 1)
└── tasks.md             # Phase 2 (/speckit-tasks; no lo crea este comando)
```

### Source Code (repository root)

```text
shared/src/
├── commonMain/
│   ├── composeResources/
│   │   ├── values/strings.xml        # todos los textos, en español
│   │   ├── font/                     # Outfit, JetBrains Mono
│   │   └── drawable/                 # íconos, stickers, logos de proveedores
│   └── kotlin/pe/gdg/open/devfest/app/
│       ├── App.kt                    # raíz: tema, ModalHost, navegación
│       ├── AppConfig.kt              # URLs de Términos y Privacidad, fecha del evento
│       ├── di/                       # módulos de Koin
│       ├── domain/
│       │   ├── model/                # Event, Track, Session, User, Ranking, ScanResult…
│       │   ├── repository/           # las 6 interfaces de repositorio, Outcome, DataError
│       │   └── logic/                # funciones puras: AHORA, cruces, destacada, ranking…
│       ├── data/
│       │   ├── api/                  # DevFestApi, dto/, FakeDevFestApi, FakeScenario
│       │   ├── mapper/               # DTO → dominio
│       │   ├── local/                # caché de agenda y charlas guardadas (DataStore)
│       │   └── repository/           # implementaciones de los repositorios
│       ├── platform/                 # expect: AuthGateway, QrScannerView, reduceMotion…
│       └── ui/
│           ├── theme/                # DevFestTheme, colores, tipografía, LocalReduceMotion
│           ├── components/           # tarjeta de charla, chip, botón con sombra, cabecera…
│           ├── modal/                # AppModal, Builder, ModalHost, presets
│           ├── navigation/           # rutas y grafo
│           └── screens/              # login, agenda, myagenda, talk, profile, gems,
│                                     # scanner, ranking (pantalla + ViewModel cada una)
├── commonTest/kotlin/pe/gdg/open/devfest/app/
│   ├── domain/logic/                 # tests de la lógica de SC-008
│   ├── data/                         # mappers y repositorios con FakeDevFestApi
│   └── ui/                           # ViewModels
├── androidMain/kotlin/pe/gdg/open/devfest/app/platform/   # actual de Android
└── iosMain/kotlin/pe/gdg/open/devfest/app/platform/       # actual de iOS

androidApp/src/main/                  # MainActivity, manifiesto (cámara, tema claro)
iosApp/iosApp/                        # iOSApp.swift, puente Swift de Firebase Auth, Info.plist
gradle/libs.versions.toml             # catálogo de versiones
```

**Structure Decision**: se mantiene la estructura que ya existe en el repositorio: un módulo
`shared` con todo el código común y dos puntos de entrada (`androidApp`, `iosApp`). No se
crean módulos nuevos; la separación en capas se hace por paquetes dentro de `shared`, bajo
`pe.gdg.open.devfest.app`.

## Orden de implementación sugerido

Sirve de entrada a `/speckit-tasks`; sigue las prioridades de la spec.

1. **Base**: `minSdk` 26, dependencias, tema y tokens, tipografías, textos, componentes base,
   modal genérico, `LocalReduceMotion`, navegación y DI.
2. **Datos**: DTOs del contrato, mappers, `DevFestApi` y `FakeDevFestApi`, almacenamiento local,
   repositorios y lógica de dominio, todo con sus tests.
3. **P1**: Login (US1), Agenda (US2), Mi agenda (US3), Detalle (US4).
4. **P2**: Mis gemas y escáner (US5, US6), Ranking (US7), Perfil y cierre de sesión (US8).
5. **Cierre**: accesibilidad (FR-061 a FR-063), validación con [quickstart.md](quickstart.md).

## Riesgos y pendientes

| Riesgo o pendiente | Mitigación |
|---|---|
| Faltan los archivos de configuración de Firebase | `FakeAuthGateway` permite avanzar; el login real se activa al recibirlos |
| Compilar y probar iOS requiere macOS | Ejecutar los controles de iOS en una Mac o en CI con macOS |
| Versiones de librerías nuevas sin verificar | Fijarlas al añadirlas al catálogo y compilar ambos targets de inmediato |
| Faltan las URLs de Términos y Privacidad | Constantes en `AppConfig`; los enlaces quedan listos |
| Esqueletos, ícono y splash sin diseño | Esqueletos según FR-018; ícono y splash por defecto hasta tener diseño |

## Complexity Tracking

Sin violaciones de la constitución; no hay nada que justificar.
