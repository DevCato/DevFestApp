---

description: "Lista de tareas para implementar la app móvil DevFest Lima 2026"
---

# Tasks: App móvil DevFest Lima 2026

**Input**: Design documents from `/specs/001-devfest-lima-app/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/api.yaml,
contracts/repositories.md, quickstart.md, notas-para-plan.md

**Tests**: INCLUIDOS. La spec pide tests de la lógica compartida (SC-008) y la constitución
(Principio II) exige tests en `commonTest` para toda lógica de `commonMain`. No hay tests de UI.

**Organization**: las tareas se agrupan por historia de usuario para implementar y validar cada
una por separado.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: se puede hacer en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: historia de usuario a la que pertenece (US1 … US8)
- Cada tarea indica la ruta exacta de sus archivos

## Path Conventions

Proyecto Kotlin Multiplatform con un módulo `shared`. Para acortar las rutas se usan estos
alias; cada uno equivale a la ruta completa indicada:

| Alias | Ruta real |
|---|---|
| `C/` | `shared/src/commonMain/kotlin/pe/gdg/open/devfest/app/` |
| `T/` | `shared/src/commonTest/kotlin/pe/gdg/open/devfest/app/` |
| `A/` | `shared/src/androidMain/kotlin/pe/gdg/open/devfest/app/` |
| `I/` | `shared/src/iosMain/kotlin/pe/gdg/open/devfest/app/` |
| `R/` | `shared/src/commonMain/composeResources/` |

Reglas que aplican a todas las tareas (constitución):

- Todo texto visible va en `R/values/strings.xml`, en español; ningún literal en composables.
- Toda animación define su variante con "reducir movimiento" activo (`LocalReduceMotion`).
- La UI y los ViewModels solo acceden a datos por las interfaces de `C/domain/repository/`.
- Fuente de verdad visual: https://claude.ai/artifact/H9uMTBqaSt4rxrX6XVxxyV y los tokens de
  `notas-para-plan.md`. Textos exactos: `spec.md`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: dejar el proyecto compilando con las dependencias y la configuración de plataforma.

- [X] T001 Cambiar `android-minSdk` de `"24"` a `"26"` en `gradle/libs.versions.toml`
- [X] T002 Añadir a `gradle/libs.versions.toml` (última versión estable compatible con Kotlin 2.4.20 y Compose Multiplatform 1.12.1): navigation-compose multiplatform, koin (core, compose, compose-viewmodel), kotlinx-coroutines (core, test), kotlinx-serialization-json y su plugin, kotlinx-datetime, ktor-client (core, okhttp, darwin), coil3 (compose, network-ktor3), androidx-datastore-preferences, CameraX (camera2, lifecycle, view), ML Kit barcode-scanning (modelo incluido), firebase-bom y firebase-auth, androidx-credentials y googleid, plugin google-services
- [X] T003 Aplicar el plugin de serialización y declarar las dependencias por source set en `shared/build.gradle.kts`: comunes en `commonMain`, coroutines-test en `commonTest`, ktor-okhttp, CameraX, ML Kit, Firebase y Credentials en `androidMain`, ktor-darwin en `iosMain`
- [X] T004 Verificar `applicationId = "pe.gdg.open.devfest.app"` y aplicar el plugin google-services solo si existe `androidApp/google-services.json`, en `androidApp/build.gradle.kts`
- [X] T005 [P] Añadir las tipografías Outfit (400, 500, 600, 700, 800) y JetBrains Mono (500, 600) como archivos `.ttf` en `R/font/`
- [X] T006 [P] Forzar tema claro en Android: tema claro de la app y `android:forceDarkAllowed="false"` en `androidApp/src/main/res/values/themes.xml` y `androidApp/src/main/AndroidManifest.xml`
- [X] T007 [P] Configurar iOS: `UIUserInterfaceStyle = Light` en `iosApp/iosApp/Info.plist`; destino de despliegue 16.0 y bundle id `pe.gdg.open.devfest.app` en `iosApp/iosApp.xcodeproj/project.pbxproj`
- [X] T008 Eliminar el código de ejemplo de la plantilla: `C/Greeting.kt`, `C/GreetingUtil.kt`, `C/Platform.kt`, `A/Platform.android.kt`, `I/Platform.ios.kt`, `T/SharedCommonTest.kt`, `shared/src/androidHostTest/kotlin/pe/gdg/open/devfest/app/SharedLogicAndroidHostTest.kt`, `shared/src/iosTest/kotlin/pe/gdg/open/devfest/app/SharedLogicIOSTest.kt`, `R/drawable/compose-multiplatform.xml`; dejar `C/App.kt` como composable vacío
- [X] T009 Verificar que compila: `./gradlew :androidApp:assembleDebug` y `./gradlew :shared:testAndroidHostTest`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: dominio, contrato, datos falsos, almacenamiento, tema, modal y navegación que usan
todas las historias.

**⚠️ CRITICAL**: ninguna historia puede empezar antes de terminar esta fase.

### Dominio

- [X] T010 [P] Crear `Outcome<T>` (`Success(value)` | `Failure(error)`) y `DataError` (`Offline`, `SessionExpired`, `Unknown`) en `C/domain/repository/Outcome.kt`
- [X] T011 [P] Crear `Event` (id, name, edition, date: LocalDate, city, timeZone: TimeZone, hashtag), `TrackId` (`IA`, `WEB`, `MOBILE`, `CLOUD`, `GENERAL`) y `Track` (id, name, color: Long ARGB) en `C/domain/model/Event.kt` y `C/domain/model/Track.kt`
- [X] T012 [P] Crear `Session` sellada con campos comunes `id`, `title`, `startsAt: Instant`, `endsAt: Instant`; `Talk` (room, track, level, description, topics, speakers, isLive) y `Break`; `Level` (`BASICO`, `INTERMEDIO`, `AVANZADO`, `TODOS`); `Speaker` (name, photoUrl?, role?, company?); `Agenda` (updatedAt, tracks, sessions) en `C/domain/model/Session.kt`
- [X] T013 [P] Crear `AuthProvider` (`GOOGLE`, `APPLE`, `GITHUB`), `AuthSession` (userId, fullName, photoUrl?, provider) y `User` (id, fullName, photoUrl?, provider, gems, rank, totalParticipants) en `C/domain/model/User.kt`
- [X] T014 [P] Crear `EarnWay` (type: `TALK`/`STAND`/`CHALLENGE`, title, description, gems), `Prize` (id, title, description?, forPositions?, imageUrl?; sin precio), `GemsInfo`, `ScanSource` (type, name) y `ScanResult` sellado (`Awarded(gemsAwarded, newBalance, source)`, `AlreadyUsed(balance, source?)`, `Invalid`) en `C/domain/model/Gems.kt`
- [X] T015 [P] Crear `RankingEntry` (position, userId, fullName, photoUrl?, gems, isMe) y `Ranking` (updatedAt, totalParticipants, top, me) en `C/domain/model/Ranking.kt`
- [X] T016 Crear las seis interfaces con los miembros exactos de `contracts/repositories.md`: `C/domain/repository/AuthRepository.kt`, `EventRepository.kt`, `AgendaRepository.kt`, `UserRepository.kt`, `GemsRepository.kt`, `RankingRepository.kt` (operaciones `suspend` o `Flow`; las que fallan devuelven `Outcome`)

### Contrato y datos falsos

- [X] T017 [P] Crear los DTOs `@Serializable` que replican exactamente los esquemas de `contracts/api.yaml` (Error, Event, Track, Speaker, Session, Agenda, Me, EarnWay, Prize, GemsInfo, ScanSource, ScanAwarded, ScanAlreadyUsed, RankingEntry, Ranking), respetando campos requeridos y anulables, en `C/data/api/dto/`
- [X] T018 Crear la interfaz `DevFestApi` con una operación por `operationId` (`getEvent`, `getAgenda`, `getMe`, `getGemsInfo`, `postScan(code)`, `getRanking(limit = 10)`) y las excepciones de transporte, `401` y estado inesperado en `C/data/api/DevFestApi.kt`
- [X] T019 Implementar los mappers DTO → dominio en `C/data/mapper/Mappers.kt` con las reglas de `data-model.md`: "`endsAt` DEBE ser posterior a `startsAt`; una sesión que no lo cumple se descarta"; "una `talk` sin `trackId` o con un track desconocido se asigna a `GENERAL`"; "una `talk` sin `level` usa `TODOS`; sin `room` muestra la sala vacía"; "`speakers` vacío significa 'Ponente por confirmar'"; sesiones ordenadas por `startsAt` conservando el orden del backend a igual hora; color `#RRGGBB` → ARGB; `top` del ranking sin reordenar
- [X] T020 [P] Tests de los mappers (cada regla de T019 y el mapeo de 200/409/422 de escaneo a `ScanResult`) en `T/data/mapper/MappersTest.kt`
- [X] T021 Crear `FakeScenario` (latencia, fallo de red, agenda vacía, sesión vencida) en `C/data/api/FakeScenario.kt` y los datos del Appendix B de `spec.md` como DTOs en `C/data/api/FakeData.kt`: 12 sesiones (9 charlas y 3 pausas) con `isLive = true` en las dos de las 11:30, ranking de 13 participantes más el usuario (120 gemas, puesto 12 de 14), formas de ganar +20/+40/+80, premios y los códigos `DFL26-TALK-t5-7f3a` (+20), `DFL26-STAND-s1-2b9c` (+40), `DFL26-CHALLENGE-c1-9d41` (+80)
- [X] T022 Implementar `FakeDevFestApi` en `C/data/api/FakeDevFestApi.kt`: aplica `FakeScenario`; `getMe` toma nombre, foto y proveedor de la sesión de autenticación; `postScan` devuelve Awarded la primera vez, AlreadyUsed después y Invalid para cualquier otro código; saldo y puesto se actualizan dentro del fake
- [X] T023 [P] Tests de `FakeDevFestApi` (cada código de prueba, repetición, código inválido y cada escenario) en `T/data/api/FakeDevFestApiTest.kt`

### Almacenamiento local

- [X] T024 Crear la interfaz `KeyValueStore` y su implementación con DataStore Preferences en `C/data/local/KeyValueStore.kt`; `expect` de la ruta del archivo en `C/platform/DataStorePath.kt` con `actual` en `A/platform/DataStorePath.android.kt` (`Context.filesDir`) e `I/platform/DataStorePath.ios.kt` (`NSDocumentDirectory`); implementación en memoria para tests en `T/data/local/InMemoryKeyValueStore.kt`
- [X] T025 Implementar `AgendaLocalStore` en `C/data/local/AgendaLocalStore.kt` con las claves `agenda_cache` (JSON del DTO Agenda), `event_cache` (JSON del DTO Event) y `saved_talk_ids` (conjunto de ids)
- [X] T026 [P] Tests de `AgendaLocalStore` (guardar y leer agenda, evento e ids; borrar ids) en `T/data/local/AgendaLocalStoreTest.kt`

### Utilidades y sesión

- [X] T027 [P] Crear los formateadores de hora y fecha en hora de Lima y formato 24 h (`HH:mm`, duración en minutos, "Actualizado a las HH:MM") en `C/domain/logic/TimeFormat.kt` con tests en `T/domain/logic/TimeFormatTest.kt` (incluye teléfono en otra zona horaria)
- [X] T028 [P] Crear la interfaz `AuthGateway` (`currentSession`, `signIn(provider)`, `signOut()`, `idToken()`) en `C/platform/AuthGateway.kt` y `FakeAuthGateway` (éxito, fallo y cancelación configurables) en `C/platform/FakeAuthGateway.kt`
- [X] T029 [P] Crear `SessionEvents` (flujo compartido que publica la sesión vencida) en `C/data/SessionEvents.kt`
- [X] T030 [P] Crear `AppConfig` (URL de Términos, URL de Privacidad, uso de autenticación falsa) en `C/AppConfig.kt`

### Tema, componentes base y modal

- [X] T031 [P] Crear `R/values/strings.xml` con los textos comunes (nombre de la app, "Community Edition", "2026", "LIVE NOW", "Conectando…", botones genéricos y descripciones de accesibilidad de íconos)
- [X] T032 [P] Crear el tema en `C/ui/theme/`: `DevFestColors.kt` (fondo `#F1F1F1`, tarjeta `#FFFFFF`, tinta `#1E1E1E`, secundario `#5F6368`, suave `#ECECEC`, celeste `#57CAFF`/`#C6ECFF`, amarillo `#FBBC04`, rojo en vivo `#D93025`, tracks IA `#4285F4`, Web `#A142F4`, Mobile `#34A853`, Cloud `#FBBC04`, General `#9AA0A6`), `DevFestTypography.kt` (Outfit y JetBrains Mono), `DevFestDimens.kt` (bordes 2–2.5 dp, radios 14–28 dp, sombras 3–7 dp) y `DevFestTheme.kt` (sin leer el modo oscuro del sistema)
- [X] T033 Crear `reduceMotionFlow()` como `expect` en `C/platform/ReduceMotion.kt` con `actual` en `A/platform/ReduceMotion.android.kt` (`Settings.Global.ANIMATOR_DURATION_SCALE == 0`) e `I/platform/ReduceMotion.ios.kt` (`UIAccessibilityIsReduceMotionEnabled` y su notificación), y `LocalReduceMotion` en `C/ui/theme/ReduceMotion.kt`
- [X] T034 Crear los componentes base en `C/ui/components/`: `SolidShadow.kt` (sombra sólida desplazada sin blur), `AppButton.kt` (borde de tinta, sombra, presión 100–150 ms con `CubicBezierEasing(0.23f, 1f, 0.32f, 1f)`, estado de carga y deshabilitado, mínimo 44×44) y `DotBackground.kt` (fondo con patrón de puntos)
- [X] T035 [P] Crear `Avatar` (foto con Coil o inicial del nombre; avatar genérico) en `C/ui/components/Avatar.kt`
- [X] T036 [P] Crear `FloatingSticker` (`rememberInfiniteTransition`, ida y vuelta, ritmo por sticker; estático con reducir movimiento) en `C/ui/components/FloatingSticker.kt` y los stickers en `R/drawable/`
- [X] T037 [P] Crear `SkeletonBlock` (bloque gris con radios del diseño, sin bordes ni sombras, brillo suave que se apaga con reducir movimiento; FR-018) en `C/ui/components/Skeleton.kt`
- [X] T038 [P] Crear `AppIcons` y los íconos vectoriales (marcador, volver, refresh, gema, wifi tachado, cámara, cerrar, logos de Google, Apple y GitHub) en `C/ui/components/AppIcons.kt` y `R/drawable/`
- [X] T039 Crear `AppModal` inmutable, `AppModal.Builder` (title, message, icon, iconBackground, iconTint, shadowColor, primaryButton, secondaryButton opcional, dismissOnOutsideTap; valida que haya título y botón principal) y `ModalRequest` en `C/ui/modal/AppModal.kt`
- [X] T040 [P] Tests del builder (falla sin título, falla sin botón principal, uno y dos botones, valores por defecto) en `T/ui/modal/AppModalBuilderTest.kt`
- [X] T041 Crear `ModalHost` en `C/ui/modal/ModalHost.kt`: uno a la vez, fundido del fondo y escala 0.95 → 1 en ≈220 ms (solo fundido con reducir movimiento), ícono girado -6°, semántica de diálogo y foco en el título (FR-067)
- [X] T042 Crear los nueve presets de FR-066 con el builder y sus textos en `strings.xml`, en `C/ui/modal/Modals.kt`: no se pudo iniciar sesión, sesión vencida, confirmar cierre de sesión, gemas sumadas, QR ya usado, QR no válido, sin conexión, permiso de cámara, no se pudo actualizar

### Navegación, DI y raíz

- [X] T043 Crear las rutas tipadas (Login, Agenda, MyAgenda, TalkDetail(id), Profile, Gems, Scanner, Ranking) en `C/ui/navigation/Routes.kt` y el grafo en `C/ui/navigation/AppNavHost.kt`: push/pop de 220–280 ms (solo fundido con reducir movimiento) y pestañas Agenda/Mi agenda sin animación
- [X] T044 Crear los módulos de Koin en `C/di/AppModule.kt` (`Clock.System`, `FakeDevFestApi`, `AgendaLocalStore`, `SessionEvents`, `AuthGateway` según `AppConfig`) e iniciar Koin en `androidApp/src/main/kotlin/pe/gdg/open/devfest/app/MainActivity.kt` y en `I/MainViewController.kt`; montar `DevFestTheme`, `ModalHost` y `AppNavHost` en `C/App.kt`

**Checkpoint**: la app compila en ambas plataformas, muestra una raíz vacía con el tema, y los
tests de mappers, fake, almacenamiento, formateadores y builder pasan.

---

## Phase 3: User Story 1 — Iniciar sesión (Priority: P1) 🎯 MVP

**Goal**: entrar con Google, Apple o GitHub y llegar a la Agenda; entrar directo si ya hay sesión.

**Independent Test**: abrir la app sin sesión, tocar cualquiera de los 3 botones, completar el
flujo del proveedor y llegar a la Agenda.

### Tests for User Story 1

> Escribir primero y comprobar que fallan antes de implementar.

- [X] T045 [P] [US1] Tests de `AuthRepositoryImpl` con `FakeAuthGateway` (éxito, fallo, cancelación, sesión que persiste, cierre de sesión) en `T/data/repository/AuthRepositoryImplTest.kt`
- [X] T046 [P] [US1] Tests de `LoginViewModel` (estado "Conectando…" en el botón tocado, demás botones deshabilitados, modal "No pudimos iniciar sesión" al fallar o cancelar, botones rehabilitados, segundo toque ignorado) en `T/ui/screens/login/LoginViewModelTest.kt`

### Implementation for User Story 1

- [X] T047 [US1] Implementar `AuthRepositoryImpl` sobre `AuthGateway` en `C/data/repository/AuthRepositoryImpl.kt` y registrarlo en `C/di/AppModule.kt`
- [X] T048 [US1] Implementar `LoginViewModel` (`StateFlow<LoginUiState>`, emite `ModalRequest` y evento de navegación) en `C/ui/screens/login/LoginViewModel.kt`
- [X] T049 [US1] Implementar `LoginScreen` en `C/ui/screens/login/LoginScreen.kt` según el escenario 1 de US1 y FR-005: lockup "{ DevFest Lima }", etiquetas, "Sáb 21 nov · Lima", tarjeta "Inicia sesión" con Google (blanco), Apple (negro) y GitHub (blanco), aviso de Términos y Privacidad debajo de la tarjeta con enlaces a las URLs de `AppConfig`, "#DevFestLima26" y 6 stickers flotantes; textos en `R/values/strings.xml`
- [X] T050 [US1] Elegir el destino inicial según `AuthRepository.session` (Login o Agenda) y navegar a Agenda tras el login sin dejar Login en la pila, en `C/ui/navigation/AppNavHost.kt`
- [X] T051 [P] [US1] Implementar el `AuthGateway` de Android con Firebase Auth en `A/platform/FirebaseAuthGateway.kt`: Credential Manager para Google y `OAuthProvider` para Apple y GitHub; pasar la Activity desde `androidApp/src/main/kotlin/pe/gdg/open/devfest/app/MainActivity.kt`
- [X] T052 [P] [US1] Implementar el `AuthGateway` de iOS en `iosApp/iosApp/FirebaseAuthGateway.swift` (Firebase Auth y GoogleSignIn por SPM, `ASAuthorization` para Apple, `OAuthProvider` para GitHub), configurar Firebase en `iosApp/iosApp/iOSApp.swift` y recibirlo como parámetro en `I/MainViewController.kt`
- [X] T053 [US1] Usar el `AuthGateway` real cuando existan los archivos de configuración de Firebase y `FakeAuthGateway` en caso contrario, en `C/di/AppModule.kt` y `C/AppConfig.kt`

**Checkpoint**: se puede iniciar sesión y la app recuerda la sesión al reabrir.

---

## Phase 4: User Story 2 — Consultar la agenda del día (Priority: P1)

**Goal**: ver las sesiones por horario, filtrar por track, ver qué está en vivo y usar la agenda
sin conexión.

**Independent Test**: con datos falsos, abrir la Agenda, filtrar por cada track, verificar
"LIVE NOW" en las charlas de las 11:30, y cambiar la hora del teléfono para verificar la línea
AHORA.

### Tests for User Story 2

- [X] T054 [P] [US2] Tests del estado temporal (`PAST` si `now >= endsAt`; antes del evento ninguna pasada; después todas; teléfono en otra zona horaria) en `T/domain/logic/SessionTimeStatusTest.kt`
- [X] T055 [P] [US2] Tests de la línea AHORA (solo con filtro "Todo", solo en la fecha del evento en America/Lima, solo entre el inicio de la primera sesión y el fin de la última; va antes de la primera sesión con `startsAt > now`; al final si ninguna está por empezar) en `T/domain/logic/NowLineTest.kt`
- [X] T056 [P] [US2] Tests de la lista (filtro "Todo" incluye pausas; filtro de track solo charlas de ese track sin pausas ni AHORA; hora solo en la primera sesión de cada grupo con el mismo `startsAt`; lista vacía por track) en `T/domain/logic/AgendaListTest.kt`
- [X] T057 [P] [US2] Tests de `AgendaRepositoryImpl` (recarga guarda en caché; fallo no borra la agenda guardada; `Offline` y `SessionExpired`; sin caché devuelve `null`) en `T/data/repository/AgendaRepositoryImplTest.kt`
- [X] T058 [P] [US2] Tests de `UserRepositoryImpl` y `EventRepositoryImpl` (recarga, `setBalance` fija el valor exacto, errores) en `T/data/repository/UserRepositoryImplTest.kt` y `T/data/repository/EventRepositoryImplTest.kt`
- [X] T059 [P] [US2] Tests de `AgendaViewModel` (esqueleto sin caché, contenido con caché mientras recarga, estado "Sin conexión" sin caché, modal "No se pudo actualizar" con caché, agenda vacía sin filtros, cambio de filtro, recarga al entrar y con pull-to-refresh) en `T/ui/screens/agenda/AgendaViewModelTest.kt`

### Implementation for User Story 2

- [X] T060 [P] [US2] Implementar el estado temporal de una sesión con `Clock` inyectado en `C/domain/logic/SessionTimeStatus.kt`
- [X] T061 [P] [US2] Implementar el cálculo de la línea AHORA en `C/domain/logic/NowLine.kt`
- [X] T062 [US2] Implementar el armado de la lista de la Agenda (filtro, pausas, agrupación por hora, posición de AHORA) en `C/domain/logic/AgendaList.kt`
- [X] T063 [US2] Implementar `AgendaRepositoryImpl` (`agenda`, `refresh()`; charlas guardadas en US3) en `C/data/repository/AgendaRepositoryImpl.kt` y registrarlo en `C/di/AppModule.kt`
- [X] T064 [P] [US2] Implementar `EventRepositoryImpl` y `UserRepositoryImpl` en `C/data/repository/EventRepositoryImpl.kt` y `C/data/repository/UserRepositoryImpl.kt`, y registrarlos en `C/di/AppModule.kt`
- [X] T065 [US2] Implementar `AgendaViewModel` (`StateFlow<AgendaUiState>`: Skeleton, Content, Empty, Offline; filtro; `refresh()`; emite `ModalRequest`) en `C/ui/screens/agenda/AgendaViewModel.kt`
- [X] T066 [P] [US2] Crear `AppHeader` (logo tipográfico, contador de gemas, foto del usuario; ambos tocables con descripción) en `C/ui/components/AppHeader.kt`
- [X] T067 [P] [US2] Crear `TrackChip` y `LiveBadge` (punto que late en bucle de 1.4 s; fijo con reducir movimiento) en `C/ui/components/TrackChip.kt` y `C/ui/components/LiveBadge.kt`
- [X] T068 [US2] Crear `TalkCard` (track, título, sala, duración, nivel, sombra del color del track, atenuada si pasó), `BreakRow` (fila punteada no tocable con duración) y `NowLineRow` (línea roja "AHORA" con hora) en `C/ui/components/TalkCard.kt`, `C/ui/components/BreakRow.kt` y `C/ui/components/NowLineRow.kt`
- [X] T069 [P] [US2] Crear `FilterRow` (Todo, IA, Web, Mobile, Cloud; desplazamiento horizontal con borde derecho desvanecido) en `C/ui/components/FilterRow.kt`
- [X] T070 [P] [US2] Crear `BottomTabBar` (Agenda, Mi agenda con contador) en `C/ui/components/BottomTabBar.kt`
- [X] T071 [P] [US2] Crear `EmptyState` (ícono, título, texto, botón opcional) y `AgendaSkeleton` en `C/ui/components/EmptyState.kt` y `C/ui/screens/agenda/AgendaSkeleton.kt`
- [X] T072 [US2] Implementar `AgendaScreen` en `C/ui/screens/agenda/AgendaScreen.kt` según los escenarios 1 a 14 de US2: cabecera con 2 stickers, etiquetas, título, filtros, lista, pull-to-refresh, estados "Aún no hay charlas" (sin filtros), "Nada en este track" con "Ver todas las charlas" y "Sin conexión" con "Intentar de nuevo"; textos en `R/values/strings.xml`
- [X] T073 [US2] Conectar la Agenda en `C/ui/navigation/AppNavHost.kt`: pestañas con `BottomTabBar`, recarga al entrar a la pantalla y un destino provisional para Mi agenda

**Checkpoint**: la Agenda funciona con datos falsos, con y sin conexión simulada.

---

## Phase 5: User Story 3 — Armar mi agenda (Priority: P1)

**Goal**: guardar y quitar charlas, verlas juntas con su tarjeta destacada y los cruces de horario.

**Independent Test**: guardar 2 charlas del mismo horario y verificar que aparecen en Mi agenda
con aviso de cruce.

### Tests for User Story 3

- [X] T074 [P] [US3] Tests de cruces (A y B se cruzan si `A.startsAt < B.endsAt` y `B.startsAt < A.endsAt`; charlas contiguas no se cruzan; tres charlas) en `T/domain/logic/ScheduleConflictsTest.kt`
- [X] T075 [P] [US3] Tests de la tarjeta destacada (en vivo → "EN CURSO AHORA" con la primera por `startsAt`; si no, primera con `startsAt > now` → "TU PRÓXIMA CHARLA"; ninguna → no se muestra) en `T/domain/logic/FeaturedTalkTest.kt`
- [X] T076 [P] [US3] Tests de charlas guardadas en `AgendaRepositoryImpl` (guardar, quitar, persistencia, depuración de ids que ya no existen tras una recarga correcta, `clearSaved`) y de `MyAgendaViewModel` (conteo, orden por hora, estado vacío, segundo toque ignorado) en `T/data/repository/SavedTalksTest.kt` y `T/ui/screens/myagenda/MyAgendaViewModelTest.kt`

### Implementation for User Story 3

- [X] T077 [P] [US3] Implementar la detección de cruces en `C/domain/logic/ScheduleConflicts.kt`
- [X] T078 [P] [US3] Implementar la selección de la tarjeta destacada con `Clock` inyectado en `C/domain/logic/FeaturedTalk.kt`
- [X] T079 [US3] Añadir `savedTalkIds`, `toggleSaved(talkId)` y `clearSaved()` a `C/data/repository/AgendaRepositoryImpl.kt`, con la depuración de ids en `refresh()`
- [X] T080 [US3] Añadir el marcador a `C/ui/components/TalkCard.kt` (relleno con el color del track, animación breve de confirmación que se omite con reducir movimiento, descripción accesible) y el guardado a `C/ui/screens/agenda/AgendaViewModel.kt`
- [X] T081 [US3] Implementar `MyAgendaViewModel` en `C/ui/screens/myagenda/MyAgendaViewModel.kt`
- [X] T082 [P] [US3] Crear `FeaturedTalkCard` ("EN CURSO AHORA" con "LIVE NOW" o "TU PRÓXIMA CHARLA") y la etiqueta amarilla "Cruce de horario" en `C/ui/screens/myagenda/FeaturedTalkCard.kt` y `C/ui/components/ConflictBadge.kt`
- [X] T083 [US3] Implementar `MyAgendaScreen` en `C/ui/screens/myagenda/MyAgendaScreen.kt` según los escenarios 3 a 6 de US3 ("N charlas guardadas", tarjeta destacada, "Todas tus charlas", estado "Aún no guardas charlas" con "Explorar la agenda") y conectarla con el contador de la pestaña en `C/ui/navigation/AppNavHost.kt`

**Checkpoint**: las charlas guardadas persisten al reabrir la app y sin conexión.

---

## Phase 6: User Story 4 — Ver el detalle de una charla (Priority: P1)

**Goal**: ver toda la información de una charla y guardarla desde ahí.

**Independent Test**: abrir el detalle desde la Agenda y desde Mi agenda, guardar/quitar y volver.

### Tests for User Story 4

- [X] T084 [P] [US4] Tests de "Charla N / total" (charlas sin pausas ordenadas por `startsAt`; N desde 1) en `T/domain/logic/TalkIndexTest.kt`
- [X] T085 [P] [US4] Tests de `TalkDetailViewModel` (datos de la charla, ponente por confirmar con `speakers` vacío, guardar y quitar, charla inexistente) en `T/ui/screens/talk/TalkDetailViewModelTest.kt`

### Implementation for User Story 4

- [X] T086 [P] [US4] Implementar el cálculo de posición y total en `C/domain/logic/TalkIndex.kt`
- [X] T087 [US4] Implementar `TalkDetailViewModel` en `C/ui/screens/talk/TalkDetailViewModel.kt`
- [X] T088 [US4] Implementar `TalkDetailScreen` en `C/ui/screens/talk/TalkDetailScreen.kt` según los escenarios 1 a 3 de US4: volver, "Charla N / total", marcador, tarjeta con track, "LIVE NOW", título y los cuatro datos (Hora, Sala, Duración, Nivel), "Sobre la charla", "Ponente" o "Ponente por confirmar · Se anunciará pronto", "Temas" y botón fijo "Agregar a mi agenda" / "Guardada en tu agenda"
- [X] T089 [US4] Navegar al detalle desde `C/ui/screens/agenda/AgendaScreen.kt` y `C/ui/screens/myagenda/MyAgendaScreen.kt`, y volver a la pantalla de origen, en `C/ui/navigation/AppNavHost.kt`

**Checkpoint**: las cuatro historias P1 funcionan de punta a punta.

---

## Phase 7: User Story 6 — Ver mis gemas y los premios (Priority: P2)

**Goal**: ver el saldo, cómo ganar gemas y los premios de los primeros puestos.

**Independent Test**: abrir Mis gemas y verificar el saldo animado, los botones y las secciones
informativas.

> Va antes de US5 porque el botón "Escanear QR" vive en esta pantalla.

### Tests for User Story 6

- [X] T090 [P] [US6] Tests de `GemsRepositoryImpl.info()` (éxito, `Offline`, `SessionExpired`) en `T/data/repository/GemsRepositoryInfoTest.kt`
- [X] T091 [P] [US6] Tests de `GemsViewModel` (esqueleto, saldo y puesto del usuario, formas de ganar y premios del backend, modal "No se pudo actualizar") en `T/ui/screens/gems/GemsViewModelTest.kt`

### Implementation for User Story 6

- [X] T092 [US6] Implementar `GemsRepositoryImpl` con `info()` en `C/data/repository/GemsRepositoryImpl.kt` y registrarlo en `C/di/AppModule.kt`
- [X] T093 [US6] Implementar `GemsViewModel` en `C/ui/screens/gems/GemsViewModel.kt`
- [X] T094 [P] [US6] Crear `AnimatedCount` (de 0 al valor en ~900 ms con easeOutExpo tras la entrada; valor final directo con reducir movimiento) en `C/ui/components/AnimatedCount.kt`
- [X] T095 [US6] Implementar `GemsScreen` en `C/ui/screens/gems/GemsScreen.kt` según el escenario 2 de US6: tarjeta "TU SALDO" con contador animado y "gemas recolectadas", botón "Escanear QR" (Charlas, stands y retos), botón "Ver ranking · Vas en el puesto #N de M", "Cómo ganar gemas" y premios sin precios ni botón de canje; esqueleto de la tarjeta de saldo
- [X] T096 [US6] Abrir Mis gemas desde el contador de `C/ui/components/AppHeader.kt` en `C/ui/navigation/AppNavHost.kt`

**Checkpoint**: Mis gemas muestra el saldo y el contenido informativo del fake.

---

## Phase 8: User Story 5 — Ganar gemas escaneando QR (Priority: P2)

**Goal**: escanear un QR, validarlo y mostrar el resultado con los valores exactos del backend.

**Independent Test**: con datos falsos, escanear un QR válido, uno repetido y uno inválido;
verificar que el saldo mostrado es el que devuelve la fuente de datos.

### Tests for User Story 5

- [X] T097 [P] [US5] Tests de `GemsRepositoryImpl.scan()` (Awarded fija el saldo con `newBalance` sin sumar y refresca al usuario; AlreadyUsed e Invalid son resultados, no fallos; `Offline` no cambia el saldo) en `T/data/repository/GemsRepositoryScanTest.kt`
- [X] T098 [P] [US5] Tests de `ScannerViewModel` (un modal por cada resultado, lecturas ignoradas mientras valida, "Seguir escaneando" reanuda, permiso denegado) en `T/ui/screens/scanner/ScannerViewModelTest.kt`

### Implementation for User Story 5

- [X] T099 [US5] Implementar `scan(code)` en `C/data/repository/GemsRepositoryImpl.kt`
- [X] T100 [US5] Declarar `QrScannerView`, el estado del permiso de cámara y `openAppSettings()` como `expect` en `C/platform/QrScanner.kt`
- [X] T101 [P] [US5] Implementar el `actual` de Android con CameraX y ML Kit en `A/platform/QrScanner.android.kt` y declarar el permiso de cámara en `androidApp/src/main/AndroidManifest.xml`
- [X] T102 [P] [US5] Implementar el `actual` de iOS con AVFoundation en `I/platform/QrScanner.ios.kt` y añadir `NSCameraUsageDescription` en español en `iosApp/iosApp/Info.plist`
- [X] T103 [US5] Implementar `ScannerViewModel` en `C/ui/screens/scanner/ScannerViewModel.kt`
- [X] T104 [US5] Implementar `ScannerScreen` a pantalla completa en `C/ui/screens/scanner/ScannerScreen.kt`: visor, "Apunta al código de la charla, el stand o el reto." y botón cerrar; modales "¡+N gemas!" (Listo / Seguir escaneando), "Ya registraste este código", "Este QR no es del DevFest" (Intentar de nuevo / Cancelar), "Sin conexión" (Reintentar) y "Activa la cámara" (Abrir configuración / Ahora no)
- [X] T105 [US5] Abrir el escáner desde "Escanear QR" de `C/ui/screens/gems/GemsScreen.kt` en `C/ui/navigation/AppNavHost.kt` y comprobar que cabecera, saldo y puesto se actualizan tras sumar

**Checkpoint**: los tres códigos de prueba dan los resultados de `data-model.md`.

---

## Phase 9: User Story 7 — Consultar el ranking (Priority: P2)

**Goal**: ver el podio, la lista, la posición propia y actualizar con refresh.

**Independent Test**: abrir el Ranking con datos falsos donde el usuario está fuera del top 10 y
verificar la fila separadora, la barra fija y el refresh.

### Tests for User Story 7

- [X] T106 [P] [US7] Tests del armado de la lista (podio 1–3, lista 4–10, fila propia si `me.position > 10`, "• • •" solo si `me.position > 11`, resaltado de `isMe` en podio o lista, menos de 3 participantes) en `T/domain/logic/RankingListTest.kt`
- [X] T107 [P] [US7] Tests de `RankingRepositoryImpl` (conserva el orden del backend, errores) en `T/data/repository/RankingRepositoryImplTest.kt`
- [X] T108 [P] [US7] Tests de `RankingViewModel` (esqueleto, "Actualizado a las HH:MM", refresh con "Actualizando…" y botón deshabilitado, "Actualizado hace un momento", segundo toque ignorado, modal al fallar) en `T/ui/screens/ranking/RankingViewModelTest.kt`

### Implementation for User Story 7

- [X] T109 [P] [US7] Implementar el armado de la lista del ranking en `C/domain/logic/RankingList.kt`
- [X] T110 [P] [US7] Implementar `RankingRepositoryImpl` en `C/data/repository/RankingRepositoryImpl.kt` y registrarlo en `C/di/AppModule.kt`
- [X] T111 [US7] Implementar `RankingViewModel` en `C/ui/screens/ranking/RankingViewModel.kt`
- [X] T112 [P] [US7] Crear `Podium` (1.º al centro más alto en amarillo, 2.º a la izquierda en celeste, 3.º a la derecha en verde; conteo animado) y `RankingRow` (posición, avatar, nombre recortado con "…", gemas, resaltado "Tú") en `C/ui/screens/ranking/Podium.kt` y `C/ui/screens/ranking/RankingRow.kt`
- [X] T113 [US7] Implementar `RankingScreen` en `C/ui/screens/ranking/RankingScreen.kt` según los escenarios 1 a 6 de US7: título, subtítulo, hora de actualización, botón refresh con ícono que gira (sin giro con reducir movimiento), podio, lista, fila "• • •", fila propia, barra fija "Tu posición #N de M" y esqueleto de filas
- [X] T114 [US7] Abrir el Ranking desde "Ver ranking" de `C/ui/screens/gems/GemsScreen.kt` en `C/ui/navigation/AppNavHost.kt`

**Checkpoint**: el Ranking muestra el puesto 12 de 14 con la fila "• • •".

---

## Phase 10: User Story 8 — Perfil y menú (Priority: P2)

**Goal**: ver la cuenta, navegar a las secciones y cerrar sesión con confirmación.

**Independent Test**: abrir Perfil desde la foto, recorrer Perfil → Mis gemas → Ranking y volver
dos veces, y cerrar sesión cancelando y confirmando.

### Tests for User Story 8

- [X] T115 [P] [US8] Tests de `ProfileViewModel` (datos y subtítulos de las opciones, modal con "Perderás las N charlas guardadas…" o el texto sin guardadas, confirmar borra las guardadas y cierra sesión, cancelar no cambia nada) en `T/ui/screens/profile/ProfileViewModelTest.kt`
- [X] T116 [P] [US8] Tests de sesión vencida (`SessionExpired` de cualquier repositorio → modal "Tu sesión expiró" → cierre de sesión y Login, sin llamar a `clearSaved()`) en `T/ui/AppViewModelTest.kt`

### Implementation for User Story 8

- [X] T117 [US8] Implementar `ProfileViewModel` en `C/ui/screens/profile/ProfileViewModel.kt`
- [X] T118 [US8] Implementar `ProfileScreen` en `C/ui/screens/profile/ProfileScreen.kt` según el escenario 1 de US8: foto grande o inicial, nombre, "Conectado con {proveedor}" con su logo, opciones Agenda, Mi agenda, Mis gemas y Ranking con sus subtítulos, "Cerrar sesión", línea "Términos · Política de privacidad" y stickers flotantes (sin hashtag)
- [X] T119 [US8] Abrir Perfil desde la foto de `C/ui/components/AppHeader.kt` y conectar las cuatro opciones y la vuelta a Login tras cerrar sesión en `C/ui/navigation/AppNavHost.kt`, conservando la pila real (FR-057)
- [X] T120 [US8] Implementar `AppViewModel` que escucha `SessionEvents` en `C/ui/AppViewModel.kt` y conectarlo en `C/App.kt`

**Checkpoint**: todas las historias funcionan de forma independiente.

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: requisitos transversales y validación final.

- [X] T121 Revisar accesibilidad en `C/ui/`: todo elemento tocable mide al menos 44×44 y tiene descripción (marcador, volver, refresh, foto, gemas, cerrar escáner); contraste 4.5:1 (FR-061, FR-063)
- [X] T122 Revisar en `C/ui/` el tamaño de letra grande del sistema y los textos largos: todo se puede desplazar, títulos en varias líneas y nombres del ranking con "…" (FR-062)
- [X] T123 Revisar en `C/ui/` cada animación con "reducir movimiento" activo: sin stickers flotantes, contadores, brillo de esqueletos, punto que late ni desplazamientos; solo fundidos (FR-060)
- [X] T124 Revisar en `C/ui/screens/` que el doble toque no duplica acciones en login, escaneo, refresh y marcador
- [X] T125 Revisar que no quedan textos literales en `C/ui/` y que todos están en `R/values/strings.xml` en español
- [X] T126 [P] Actualizar `README.md`: requisitos, archivos de Firebase, comandos de compilación y tests, y cómo activar cada `FakeScenario`
- [ ] T127 Ejecutar `./gradlew :shared:testAndroidHostTest`, `./gradlew :shared:iosSimulatorArm64Test` (macOS), `./gradlew :androidApp:assembleDebug` y compilar iOS en Xcode — Avance 2026-09-30: Android y compilación Kotlin de iOS en verde (188 tests); falta `iosSimulatorArm64Test` y Xcode en macOS
- [ ] T128 Ejecutar los siete escenarios de `specs/001-devfest-lima-app/quickstart.md` en Android e iOS y marcar los controles de la constitución — Avance 2026-09-30: en Android se validaron login, agenda, mi agenda, detalle, gemas, ranking, perfil, letra grande y doble toque; faltan escenarios sin conexión/sesión vencida en dispositivo, escáner con cámara real, TalkBack/VoiceOver y todo iOS

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias.
- **Foundational (Phase 2)**: depende de Setup; bloquea todas las historias.
- **Historias (Phase 3–10)**: dependen de Foundational.
- **Polish (Phase 11)**: depende de las historias que se quieran entregar.

### User Story Dependencies

- **US1 (P1)**: solo Foundational.
- **US2 (P1)**: Foundational. Se llega a ella desde US1; con `FakeAuthGateway` se prueba sola.
- **US3 (P1)**: depende de US2 (`AgendaRepositoryImpl`, `TalkCard`, pestañas).
- **US4 (P1)**: depende de US2; el guardado desde el detalle usa US3.
- **US6 (P2)**: depende de US2 (`UserRepositoryImpl`, `AppHeader`).
- **US5 (P2)**: depende de US6 (el botón "Escanear QR" y `GemsRepositoryImpl`).
- **US7 (P2)**: depende de US6 para su punto de entrada ("Ver ranking").
- **US8 (P2)**: depende de US2 (cabecera) y US3 (`clearSaved`); sus opciones de menú enlazan a
  US6 y US7.

```text
Setup → Foundational → US1
                     → US2 → US3 → US4
                           → US6 → US5
                                 → US7
                           → US8 (tras US3; enlaza a US6 y US7)
```

### Within Each User Story

- Los tests se escriben primero y deben fallar antes de implementar.
- Lógica de dominio → repositorios → ViewModel → componentes → pantalla → navegación.

### Parallel Opportunities

- Setup: T005, T006 y T007.
- Foundational: los modelos T010–T015; T017; los tests T020, T023, T026 y T040; T027–T032;
  los componentes T035–T038.
- En cada historia, todos sus tests marcados [P] y las piezas de lógica y componentes [P].
- Entre historias: tras US2, una persona puede seguir con US3 → US4 y otra con US6 → US5 / US7.
- T051 (Android) y T052 (iOS), y T101 (Android) y T102 (iOS), son paralelas entre sí; las de
  iOS requieren macOS.

---

## Parallel Example: User Story 2

```bash
# Tests de US2 en paralelo:
Task: "Tests del estado temporal en T/domain/logic/SessionTimeStatusTest.kt"
Task: "Tests de la línea AHORA en T/domain/logic/NowLineTest.kt"
Task: "Tests de la lista en T/domain/logic/AgendaListTest.kt"
Task: "Tests de AgendaRepositoryImpl en T/data/repository/AgendaRepositoryImplTest.kt"

# Componentes de US2 en paralelo:
Task: "Crear AppHeader en C/ui/components/AppHeader.kt"
Task: "Crear FilterRow en C/ui/components/FilterRow.kt"
Task: "Crear BottomTabBar en C/ui/components/BottomTabBar.kt"
```

---

## Implementation Strategy

### MVP First

1. Phase 1: Setup.
2. Phase 2: Foundational.
3. Phase 3: US1 (login). **Validar** con el escenario 1 de `quickstart.md`.
4. Phase 4: US2 (agenda). Con US1 + US2 la app ya es útil el día del evento: es el MVP real.

### Incremental Delivery

1. Setup + Foundational → base lista.
2. US1 + US2 → MVP: entrar y consultar la agenda.
3. US3 + US4 → agenda personal y detalle (completa las P1).
4. US6 + US5 → gemas y escáner.
5. US7 → ranking.
6. US8 → perfil y cierre de sesión.
7. Polish → accesibilidad y validación final.

### Parallel Team Strategy

1. Todo el equipo: Setup + Foundational.
2. Persona A: US1 (incluye los puentes nativos de Firebase).
3. Persona B: US2 → US3 → US4.
4. Persona C (tras US2): US6 → US5 → US7 → US8.

---

## Notes

- Las tareas que tocan `R/values/strings.xml`, `C/di/AppModule.kt` y
  `C/ui/navigation/AppNavHost.kt` no son [P] entre sí: son archivos compartidos.
- Las tareas de iOS (T007, T052, T102 y parte de T127 y T128) requieren macOS con Xcode.
- T051 y T052 necesitan los archivos de configuración de Firebase; sin ellos la app sigue
  funcionando con `FakeAuthGateway` (T053).
- `contracts/api.yaml` no se modifica en ninguna tarea.
- Commit después de cada tarea o grupo lógico; validar en cada checkpoint.
