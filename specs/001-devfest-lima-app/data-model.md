# Data Model: App móvil DevFest Lima 2026

**Fecha**: 2026-09-30 | **Spec**: [spec.md](spec.md) | **Contrato**: [contracts/api.yaml](contracts/api.yaml)

Tres capas de tipos, todas en `shared/src/commonMain`:

1. **DTO** (`data/api/dto`): copia exacta de los esquemas de `contracts/api.yaml`. No se
   documentan aquí; el contrato es la fuente de verdad.
2. **Dominio** (`domain/model`): lo que consumen repositorios, lógica y ViewModels.
3. **Derivados** (`domain/logic`): resultados de funciones puras que usa la UI.

## Modelos de dominio

### Event

| Campo | Tipo | Origen (DTO) |
|---|---|---|
| id | String | `Event.id` |
| name | String | `Event.name` |
| edition | String | `Event.edition` |
| date | LocalDate | `Event.date` |
| city | String | `Event.city` |
| timeZone | TimeZone | `Event.timezone` |
| hashtag | String | `Event.hashtag` |

### Track

| Campo | Tipo | Notas |
|---|---|---|
| id | TrackId (`IA`, `WEB`, `MOBILE`, `CLOUD`, `GENERAL`) | `Track.id` |
| name | String | listo para mostrar |
| color | Long (ARGB) | de `Track.color` (`#RRGGBB`) |

Los filtros de la Agenda son fijos: Todo, IA, Web, Mobile, Cloud. `GENERAL` no tiene filtro.

### Session (sellada)

Campos comunes: `id`, `title`, `startsAt: Instant`, `endsAt: Instant`.

- **Talk** (`type = talk`): `room`, `track: Track`, `level: Level`, `description`,
  `topics: List<String>`, `speakers: List<Speaker>`, `isLive: Boolean`.
- **Break** (`type = break`): sin campos extra; no es tocable.

`Level`: `BASICO`, `INTERMEDIO`, `AVANZADO`, `TODOS`.

Reglas de mapeo y validación:

- `endsAt` DEBE ser posterior a `startsAt`; una sesión que no lo cumple se descarta.
- Una `talk` sin `trackId` o con un track desconocido se asigna a `GENERAL`.
- Una `talk` sin `level` usa `TODOS`; sin `room` muestra la sala vacía.
- `speakers` vacío significa "Ponente por confirmar".

### Speaker

`name: String`, `photoUrl: String?`, `role: String?`, `company: String?`.

### Agenda

`updatedAt: Instant`, `tracks: List<Track>`, `sessions: List<Session>` ordenadas por
`startsAt` (a igual hora se conserva el orden del backend).

### User

| Campo | Tipo | Origen |
|---|---|---|
| id | String | `Me.id` |
| fullName | String | `Me.fullName` |
| photoUrl | String? | `Me.photoUrl` |
| provider | AuthProvider (`GOOGLE`, `APPLE`, `GITHUB`) | `Me.provider` |
| gems | Int | `Me.gems` |
| rank | Int | `Me.rank` |
| totalParticipants | Int | `Me.totalParticipants` |

### AuthSession

Identidad que entrega Firebase tras el login: `userId`, `fullName`, `photoUrl?`, `provider`.
Se usa para decidir Login vs. Agenda al arrancar y para pedir el ID token.

### SavedTalks

`Set<String>` de ids de sesión. Solo local (no existe endpoint).

- Un id que ya no está en la agenda publicada se ignora al mostrar y se elimina en la
  siguiente recarga correcta.
- Se borra solo con el cierre de sesión confirmado; la sesión vencida lo conserva.

### GemsInfo, EarnWay, Prize

- `EarnWay`: `type` (`TALK`, `STAND`, `CHALLENGE`), `title`, `description`, `gems`.
- `Prize`: `id`, `title`, `description?`, `forPositions?`, `imageUrl?`. Sin precio.

### ScanResult (sellado)

| Variante | Campos | Origen |
|---|---|---|
| Awarded | `gemsAwarded`, `newBalance`, `source: ScanSource` | `200 ScanAwarded` |
| AlreadyUsed | `balance`, `source: ScanSource?` | `409 ScanAlreadyUsed` |
| Invalid | — | `422 invalid_code` |

`ScanSource`: `type` (`TALK`, `STAND`, `CHALLENGE`), `name`.

La app nunca suma: tras `Awarded`, el saldo mostrado pasa a ser `newBalance`.

### Ranking

`updatedAt: Instant`, `totalParticipants: Int`, `top: List<RankingEntry>`, `me: RankingEntry`.

`RankingEntry`: `position`, `userId`, `fullName`, `photoUrl?`, `gems`, `isMe`.

El orden de `top` es el del backend y no se reordena.

### DataError y Outcome

`Outcome<T>` = `Success(value)` | `Failure(error)`, con `DataError`:

| Error | Causa | Efecto en la UI |
|---|---|---|
| Offline | fallo de transporte | "Sin conexión" / "No se pudo actualizar" |
| SessionExpired | `401` | modal "Tu sesión expiró" y vuelta a Login |
| Unknown | cualquier otro | "No se pudo actualizar" |

## Persistencia local (DataStore)

| Clave | Contenido | Se borra cuando |
|---|---|---|
| `agenda_cache` | JSON del DTO `Agenda` | nunca (se reemplaza en cada recarga correcta) |
| `event_cache` | JSON del DTO `Event` | nunca |
| `saved_talk_ids` | conjunto de ids | cierre de sesión confirmado |

## Lógica derivada (funciones puras, con `Clock` inyectado)

Todas viven en `domain/logic` y tienen tests en `commonTest` (SC-008).

### Estado temporal de una sesión

- `PAST` si `now >= endsAt`; en otro caso `UPCOMING_OR_CURRENT`.
- "LIVE NOW" es exclusivamente `Talk.isLive`; no depende de `now`.
- `now` se compara como instante, por lo que la zona horaria del teléfono no afecta.

### Línea AHORA

Se muestra solo si se cumplen todas:

1. El filtro es "Todo".
2. La fecha de `now` en America/Lima es la fecha del evento.
3. `now` está entre el inicio de la primera sesión y el fin de la última.

Posición: justo antes de la primera sesión con `startsAt > now`. Si ninguna sesión está por
empezar (transcurre la última), la línea va al final de la lista.

### Lista de la Agenda

- Filtro "Todo": charlas y pausas, con la línea AHORA.
- Filtro de track: solo charlas de ese track, sin pausas ni línea AHORA.
- La hora se muestra solo en la primera sesión de cada grupo con el mismo `startsAt`.

### "Charla N / total"

Charlas (sin pausas) ordenadas por `startsAt`; N es la posición (desde 1) y total su cantidad.

### Cruce de horario

Dos charlas guardadas A y B se cruzan si `A.startsAt < B.endsAt` y `B.startsAt < A.endsAt`.
Toda charla que se cruza con al menos otra guardada lleva la etiqueta.

### Tarjeta destacada de Mi agenda

1. Si alguna charla guardada tiene `isLive`: la primera por `startsAt`, "EN CURSO AHORA".
2. Si no, la primera guardada con `startsAt > now`: "TU PRÓXIMA CHARLA".
3. Si no hay ninguna, no se muestra.

### Lista del Ranking

- Podio: entradas de `top` con posición 1, 2 y 3.
- Lista: entradas de `top` con posición 4 a 10.
- Si `me.position > 10`: se añade la fila propia al final; si `me.position > 11`, va precedida
  de la fila "• • •".
- La entrada con `isMe` se resalta donde aparezca (podio, lista o fila propia).
- Barra fija: `me.position`, `totalParticipants` y `me.gems`.

## Transiciones de estado

### Sesión de usuario

```text
SinSesion --login correcto--> ConSesion
SinSesion --login falla/cancela--> SinSesion (modal "No pudimos iniciar sesión")
ConSesion --cerrar sesión confirmado--> SinSesion (borra saved_talk_ids)
ConSesion --401 del backend--> SinSesion (conserva saved_talk_ids)
```

### Carga de una pantalla con datos (agenda, saldo, ranking)

```text
Inicial --sin caché--> Esqueleto --ok--> Contenido
                                --falla--> SinConexion (solo agenda; con "Intentar de nuevo")
Inicial --con caché--> Contenido(caché) --recarga ok--> Contenido
                                         --recarga falla--> Contenido(caché) + modal "No se pudo actualizar"
```

### Escaneo

```text
Escaneando --código leído--> Validando --Awarded--> modal "¡+N gemas!"
                                       --AlreadyUsed--> modal "Ya registraste este código"
                                       --Invalid--> modal "Este QR no es del DevFest"
                                       --Offline--> modal "Sin conexión"
```

Mientras el estado es `Validando` se ignoran nuevas lecturas (evita envíos duplicados).

## Datos falsos

`FakeDevFestApi` sirve los datos del Appendix B de la spec en forma de DTOs del contrato:

- Agenda: 12 sesiones (9 charlas y 3 pausas), con `isLive = true` en las dos charlas de
  las 11:30.
- Usuario: 120 gemas, puesto 12 de 14; nombre, foto y proveedor tomados de la sesión.
- Ranking: los 13 participantes del Appendix B más el usuario.
- Formas de ganar: charla +20, stand +40, reto +80 (valores de ejemplo).

Códigos QR de prueba:

| Código | Resultado |
|---|---|
| `DFL26-TALK-t5-7f3a` | Awarded +20 (charla) la primera vez; AlreadyUsed después |
| `DFL26-STAND-s1-2b9c` | Awarded +40 (stand) la primera vez; AlreadyUsed después |
| `DFL26-CHALLENGE-c1-9d41` | Awarded +80 (reto) la primera vez; AlreadyUsed después |
| cualquier otro | Invalid |

Escenarios configurables del fake (`FakeScenario`): latencia, fallo de red, agenda vacía y
sesión vencida. Los usan los tests y la validación manual de [quickstart.md](quickstart.md).
