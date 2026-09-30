# Contrato interno: repositorios y fuente de datos

**Fecha**: 2026-09-30 | Complementa a [api.yaml](api.yaml); no lo reemplaza.

`api.yaml` es el contrato con el backend. Este documento fija el contrato **dentro de la app**:
qué ofrecen los repositorios a la UI y cómo se relacionan con las operaciones de `api.yaml`.
Los tipos de dominio están en [../data-model.md](../data-model.md).

## Capas

```text
UI / ViewModels  →  Repositorios (dominio)  →  DevFestApi (DTOs de api.yaml)
                                            →  Almacenamiento local (DataStore)
                                            →  AuthGateway (Firebase, por plataforma)
```

- La UI solo conoce las interfaces de repositorio y los modelos de dominio.
- `DevFestApi` es el único punto que cambia al pasar de fake a backend.

## DevFestApi

Una operación por `operationId` de `api.yaml`; devuelve DTOs o lanza un error de transporte,
de `401` o de estado inesperado.

| Operación | `operationId` | Endpoint |
|---|---|---|
| `getEvent()` | `getEvent` | `GET /event` |
| `getAgenda()` | `getAgenda` | `GET /agenda` |
| `getMe()` | `getMe` | `GET /me` |
| `getGemsInfo()` | `getGemsInfo` | `GET /gems/info` |
| `postScan(code)` | `postScan` | `POST /scans` (200 / 409 / 422) |
| `getRanking(limit = 10)` | `getRanking` | `GET /ranking` |

Implementaciones: `FakeDevFestApi` (esta versión) y `KtorDevFestApi` (con backend; envía
`Authorization: Bearer <Firebase ID token>`). Si el backend real difiere de `api.yaml`, la
adaptación se hace dentro de `KtorDevFestApi`.

## Repositorios

Todas las operaciones son `suspend` o `Flow`. Las que pueden fallar devuelven `Outcome<T>`.

### AuthRepository

| Miembro | Comportamiento |
|---|---|
| `session: StateFlow<AuthSession?>` | `null` = sin sesión; persiste entre aperturas |
| `signIn(provider): Outcome<AuthSession>` | abre el flujo del proveedor; fallo o cancelación → `Failure` |
| `signOut()` | cierra la sesión de Firebase |
| `idToken(): String?` | token para el backend (no se usa con el fake) |

### EventRepository

| Miembro | Comportamiento |
|---|---|
| `event: Flow<Event?>` | último evento guardado |
| `refresh(): Outcome<Unit>` | pide `getEvent` y guarda |

### AgendaRepository

| Miembro | Comportamiento |
|---|---|
| `agenda: Flow<Agenda?>` | última agenda guardada; `null` si nunca se cargó |
| `refresh(): Outcome<Unit>` | pide `getAgenda`, guarda y depura ids guardados que ya no existen |
| `savedTalkIds: Flow<Set<String>>` | charlas guardadas (local) |
| `toggleSaved(talkId)` | guarda o quita; funciona sin conexión |
| `clearSaved()` | borra todas (cierre de sesión confirmado) |

Un fallo de `refresh()` nunca borra la agenda guardada.

### UserRepository

| Miembro | Comportamiento |
|---|---|
| `user: StateFlow<User?>` | usuario con saldo y posición |
| `refresh(): Outcome<Unit>` | pide `getMe` |
| `setBalance(gems)` | fija el saldo con el valor exacto devuelto por un escaneo |

### GemsRepository

| Miembro | Comportamiento |
|---|---|
| `info(): Outcome<GemsInfo>` | pide `getGemsInfo` |
| `scan(code): Outcome<ScanResult>` | pide `postScan`; en `Awarded` fija el saldo con `newBalance` y refresca al usuario |

`409` y `422` son resultados (`AlreadyUsed`, `Invalid`), no `Failure`.

### RankingRepository

| Miembro | Comportamiento |
|---|---|
| `ranking(): Outcome<Ranking>` | pide `getRanking`; conserva el orden del backend |

## Reglas comunes

- Error de transporte → `DataError.Offline`. `401` → `DataError.SessionExpired`. Resto →
  `DataError.Unknown`.
- `SessionExpired` en cualquier repositorio se publica en `SessionEvents`; la app muestra el
  modal, cierra la sesión y vuelve a Login sin llamar a `clearSaved()`.
- Ningún repositorio calcula gemas, saldos ni posiciones.
- Ningún repositorio expone DTOs.

## Abstracciones de plataforma

Código específico de plataforma permitido, siempre detrás de una interfaz o `expect/actual`:

| Abstracción | Android | iOS | Motivo |
|---|---|---|---|
| `AuthGateway` | Firebase Auth + Credential Manager | Swift en `iosApp` (Firebase iOS) | flujos de login nativos |
| `QrScannerView` y permiso de cámara | CameraX + ML Kit | AVFoundation | acceso a cámara |
| `openAppSettings()` | Intent de ajustes | URL de ajustes | modal "Activa la cámara" |
| `reduceMotionFlow()` | escala de animaciones | `UIAccessibility` | FR-060 |
| Ruta del archivo de DataStore | `Context.filesDir` | `NSDocumentDirectory` | persistencia |
