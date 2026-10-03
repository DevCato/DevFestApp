# DevFest Lima 2026 — app de asistentes

App para Android e iOS del DevFest Lima 2026 (Community Edition): agenda del día, agenda
personal, gemas por escanear QR y ranking. Hecha con Kotlin Multiplatform y Compose
Multiplatform; la UI y la lógica viven en el módulo compartido.

- Especificación, plan y tareas: [`specs/001-devfest-lima-app/`](specs/001-devfest-lima-app)
- Contrato con el backend: [`contracts/api.yaml`](specs/001-devfest-lima-app/contracts/api.yaml)
- Principios del proyecto: [`.specify/memory/constitution.md`](.specify/memory/constitution.md)

## Requisitos

- JDK 17 o superior y Android SDK (la versión de `compileSdk` está en `gradle/libs.versions.toml`).
- Android 8.0+ (API 26) para ejecutar la app.
- Para iOS: macOS con Xcode y iOS 16+. Desde Windows o Linux solo se puede compilar el código
  Kotlin de iOS (`compileKotlinIos*`), no enlazar la app ni correr sus tests.

## Estructura

| Carpeta | Contenido |
|---|---|
| `shared/src/commonMain` | Todo lo común: dominio, datos, ViewModels, UI, textos (`composeResources/values/strings.xml`), tipografías e íconos |
| `shared/src/commonTest` | Tests de la lógica compartida |
| `shared/src/androidMain`, `iosMain` | Código de plataforma: login con Firebase (Android), cámara/QR, "reducir movimiento", rutas de archivos |
| `androidApp/` | Punto de entrada Android |
| `iosApp/` | Punto de entrada iOS y el login con Firebase en Swift (`FirebaseAuthGateway.swift`) |

## Compilar y probar

```bash
# Tests de la lógica compartida
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test        # solo macOS

# App Android (APK de depuración)
./gradlew :androidApp:assembleDebug

# Verificar que el código Kotlin de iOS compila (también desde Windows)
./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosArm64
```

iOS: abrir `iosApp/` en Xcode y ejecutar el esquema `iosApp`.

## Datos falsos

En esta versión todos los datos del backend (agenda, usuario, gemas, ranking) son falsos y
vienen de `shared/src/commonMain/kotlin/pe/gdg/open/devfest/app/data/api/FakeData.kt`. El día
del evento se reemplaza `FakeDevFestApi` por un cliente real de `contracts/api.yaml`; las
pantallas no cambian.

Códigos QR de prueba:

| Código | Resultado |
|---|---|
| `DFL26-TALK-t5-7f3a` | +20 gemas (charla) la primera vez; "ya registrado" después |
| `DFL26-STAND-s1-2b9c` | +40 gemas (stand) |
| `DFL26-CHALLENGE-c1-9d41` | +80 gemas (reto) |
| cualquier otro | "Este QR no es del DevFest" |

### Simular escenarios

Para probar los estados de error, cambia el `FakeScenario` en
`shared/src/commonMain/kotlin/pe/gdg/open/devfest/app/di/AppModule.kt` y vuelve a compilar:

```kotlin
single { FakeScenario(latency = AppConfig.fakeLatency, networkFailure = true) }
```

| Parámetro | Qué simula |
|---|---|
| `latency` | Espera de cada respuesta (por defecto 600 ms, para ver los esqueletos) |
| `networkFailure = true` | Sin conexión: estado "Sin conexión" (sin caché) o modal "No se pudo actualizar" (con caché) |
| `emptyAgenda = true` | "Aún no hay charlas" |
| `sessionExpired = true` | Modal "Tu sesión expiró" y vuelta a Login |

Para ver el estado "Sin conexión" de la primera apertura hay que borrar los datos de la app
(o reinstalarla) antes, porque la agenda queda guardada en el dispositivo.

## Login con Firebase

Sin la configuración de Firebase la app usa un **login simulado**: cualquier botón entra como
"Asistente DevFest" y la sesión dura mientras la app esté abierta.

**Android**: copiar `google-services.json` en `androidApp/` y compilar. El build aplica el
plugin google-services y la app usa el login real automáticamente.

**iOS** (en Xcode):

1. Agregar por Swift Package Manager `firebase-ios-sdk` (FirebaseCore, FirebaseAuth) y
   `GoogleSignIn-iOS`.
2. Copiar `GoogleService-Info.plist` en `iosApp/iosApp/`.
3. En `Info.plist`, agregar un URL Type con el `REVERSED_CLIENT_ID` de ese plist.
4. En Signing & Capabilities, agregar "Sign in with Apple".

**Consola de Firebase**: habilitar los proveedores Google, Apple y GitHub.

## Pendientes del equipo

- URLs de Términos y Política de privacidad: `AppConfig.TERMS_URL` y `AppConfig.PRIVACY_URL`
  (mientras estén vacías, los enlaces no abren nada).
- Archivos de configuración de Firebase.
- URL del backend real.

## Licencias

Las tipografías Outfit y JetBrains Mono se distribuyen bajo la SIL Open Font License; los textos
están en [`licenses/fonts/`](licenses/fonts).


## Pendientes
2. skeleton animado