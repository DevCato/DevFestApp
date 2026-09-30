# Feature Specification: App móvil DevFest Lima 2026

**Feature Branch**: `001-devfest-lima-app`
**Created**: 2026-09-29
**Status**: Draft — sin puntos pendientes
**Input**: App para Android e iOS del evento DevFest Lima 2026 (Community Edition, organizado por las comunidades GDG de Lima con Google). Los asistentes inician sesión, consultan la agenda del día, arman su agenda personal, ganan gemas escaneando códigos QR y compiten en un ranking; los primeros puestos ganan premios.
**Diseño de referencia**: prototipo interactivo en Claude Design — https://claude.ai/artifact/H9uMTBqaSt4rxrX6XVxxyV (8 pantallas + propuestas de modales; es la fuente de verdad visual).
**Contrato con el backend**: [`contracts/api.yaml`](contracts/api.yaml).

---

## Clarifications

### Session 2026-09-29

- Q: ¿Qué pantallas entran en la primera versión? → A: Las 8 pantallas del prototipo (login, agenda, agenda vacía, mi agenda, detalle de charla, perfil/menú, mis gemas, ranking) más los modales de error.
- Q: ¿De dónde salen los datos? → A: Por ahora datos falsos (fake data) dentro de la app. Luego habrá un backend propio; la app debe poder cambiar de fuente sin rehacer pantallas.
- Q: ¿Qué métodos de inicio de sesión? → A: Google, Sign in with Apple y GitHub, con Firebase Auth (Facebook se retiró en la revisión del diseño). No hay usuario/contraseña.
- Q: ¿Quién carga las charlas? → A: Un panel de administración (proyecto aparte). Los tracks son fijos. Del ponente solo se muestra nombre, foto, cargo y empresa.
- Q: ¿Cómo funciona el ranking? → A: Muestra el nombre completo; el backend entrega el orden (incluido el desempate); el usuario lo actualiza con un botón de refresh.
- Q: ¿Modo oscuro, idiomas, notificaciones? → A: Sin modo oscuro. Solo español. Notificaciones fuera de esta versión.
- Q: ¿Plataformas mínimas? → A: Android 8+ e iOS 16+.
- Q: ¿Tests? → A: Tests de la lógica compartida.
- Q: ¿Cuántos días dura el evento? → A: Uno solo: sábado 21 de noviembre de 2026, Lima.
- Q: ¿Se canjean gemas por premios? → A: No. No hay canjes ni precios. Solo los primeros puestos del ranking obtienen premios; la app solo informa cuáles son.
- Q: ¿Cómo se decide que una charla está "LIVE NOW"? → A: Lo decide el administrador con una marca (flag) en cada charla que entrega el backend.
- Q: ¿Qué hora usa la línea "AHORA"? → A: La hora del teléfono.
- Q: ¿Cómo se ganan gemas? → A: Escaneando códigos QR (al final de cada charla hay un QR; también hay QR de stands y de retos). El backend valida el código, decide cuántas gemas otorga y suma el saldo. La app no conoce ni fija los valores.
- Q: ¿Contrato con el backend? → A: Se define uno a partir de los datos del diseño (`contracts/api.yaml`). Si el backend real difiere, se escribe un adapter en la app.

### Session 2026-09-29 (revisión de huecos)

- Q: ¿La app crea cuentas (y debe permitir eliminarlas)? → A: No. Solo usa la información de OAuth2 del proveedor; no hay registro ni eliminación de cuenta en la app.
- Q: ¿Aviso legal? → A: En Login: "Al continuar aceptas los Términos y la Política de privacidad" con enlaces; en Perfil, enlaces a Términos y Privacidad. Las URLs las define el equipo después.
- Q: ¿Cuándo se recarga la agenda (para que "LIVE NOW" se actualice)? → A: Cada vez que se entra a la pantalla y con el gesto de deslizar hacia abajo (pull-to-refresh).
- Q: ¿Qué se muestra si es la primera vez y no hay conexión? → A: Un estado "Sin conexión" con el botón "Intentar de nuevo" (diseñado: pantalla 02c).
- Q: ¿Estados de carga? → A: Esqueletos (bloques grises con la forma del contenido).
- Q: ¿Cómo se muestran los errores y avisos? → A: Con un modal genérico construido con el patrón Builder (título, texto, ícono y sus colores, color de sombra, uno o dos botones con sus acciones). No se crea un modal por pantalla.
- Q: ¿Qué pasa con las charlas guardadas al cerrar sesión? → A: Se guardan en el dispositivo; al cerrar sesión se muestra un modal de confirmación que avisa que se perderán, y si se confirma se borran.
- Q: ¿"Charla N / total"? → A: N es la posición de la charla por hora de inicio; el total cuenta solo charlas (sin pausas).
- Q: ¿Quién aparece en el ranking? → A: Lo decide el backend; la app muestra lo que recibe.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Iniciar sesión (Priority: P1)

Como asistente, quiero entrar con una cuenta que ya tengo (Google, Apple o GitHub) para usar la app sin crear una contraseña.

**Why this priority**: sin sesión no hay agenda personal, gemas ni ranking.

**Independent Test**: abrir la app sin sesión, tocar cualquiera de los 3 botones, completar el flujo del proveedor y llegar a la Agenda.

**Acceptance Scenarios**:

1. **Given** no hay sesión, **When** abro la app, **Then** veo la pantalla de Login con el título "{ DevFest Lima }", las etiquetas "Community Edition" y "2026", la fecha "Sáb 21 nov · Lima", los botones "Continuar con Google" (blanco), "Continuar con Apple" (negro, según las guías de Apple) y "Continuar con GitHub" (blanco), el texto "Al continuar aceptas los Términos y la Política de privacidad" (con enlaces) y el hashtag "#DevFestLima26".
2. **Given** estoy en Login, **When** toco un proveedor, **Then** ese botón muestra "Conectando…", los demás botones se deshabilitan y, al terminar con éxito, llego a la Agenda.
3. **Given** el proveedor falla o cancelo, **When** vuelvo a la app, **Then** sigo en Login, los botones se habilitan y veo el modal "No pudimos iniciar sesión" con "Reintentar" y "Usar otra cuenta".
4. **Given** ya inicié sesión antes, **When** abro la app, **Then** entro directo a la Agenda.

---

### User Story 2 — Consultar la agenda del día (Priority: P1)

Como asistente, quiero ver todas las charlas por horario, filtrar por track y saber cuál está en curso para decidir a dónde ir.

**Why this priority**: es la razón principal de la app; se usa decenas de veces durante el evento.

**Independent Test**: con datos falsos, abrir la Agenda, filtrar por cada track, marcar una charla como "en vivo" en los datos y verificar la etiqueta; cambiar la hora del teléfono y verificar la línea AHORA.

**Acceptance Scenarios**:

1. **Given** inicié sesión, **When** abro la Agenda, **Then** veo la cabecera (logo tipográfico, contador de gemas, foto del usuario), las etiquetas "Community Edition" y "2026", el título "Agenda", los filtros y la lista de sesiones ordenada por hora.
2. **Given** la lista, **Then** cada charla muestra: etiqueta del track con su color, título, sala, duración en minutos y nivel, y su tarjeta tiene siempre la sombra del color de su track; los bloques de pausa (registro, almuerzo, cierre) se muestran como filas punteadas no tocables con su duración.
3. **Given** varias sesiones a la misma hora, **Then** la hora se muestra solo en la primera de ese bloque.
4. **Given** el backend marca una charla como en vivo, **Then** esa charla muestra la etiqueta roja "LIVE NOW" con un punto que late (independiente de la hora del teléfono).
5. **Given** según la hora del teléfono una sesión ya terminó, **Then** se muestra atenuada (opacidad reducida) pero legible.
6. **Given** el filtro "Todo" y la hora del teléfono está dentro del horario del evento del 21 de noviembre, **Then** aparece una línea roja "AHORA" con la hora, justo antes de la primera sesión que aún no empieza.
7. **Given** toco un filtro de track (IA, Web, Mobile, Cloud), **Then** solo veo charlas de ese track (sin pausas ni línea AHORA); si no hay ninguna, veo el estado vacío "Nada en este track" con el botón "Ver todas las charlas".
8. **Given** los filtros no caben en el ancho, **Then** la fila se desplaza horizontalmente y el borde derecho se desvanece para indicar que hay más.
9. **Given** no hay charlas publicadas, **Then** veo el estado "Aún no hay charlas" con un mensaje de que la agenda se está actualizando, y no se muestran los filtros.
10. **Given** toco una charla, **Then** voy a su Detalle.
11. **Given** entro a la Agenda (o a Mi agenda), **Then** la app vuelve a pedir la agenda al backend; mientras llega muestra la última agenda guardada, o esqueletos si no hay ninguna.
12. **Given** estoy en la Agenda, **When** deslizo hacia abajo (pull-to-refresh), **Then** la app recarga la agenda y actualiza los "LIVE NOW".
13. **Given** es la primera vez que abro la agenda y no hay conexión (ni agenda guardada), **Then** veo el estado "Sin conexión" con "Necesitas internet para cargar la agenda por primera vez…" y el botón "Intentar de nuevo", que muestra "Conectando…" mientras reintenta.
14. **Given** falla una recarga pero ya tengo agenda guardada, **Then** sigo viendo la agenda guardada y aparece el modal "No se pudo actualizar".

---

### User Story 3 — Armar mi agenda (Priority: P1)

Como asistente, quiero guardar las charlas que me interesan y verlas juntas, para no perderme ninguna.

**Why this priority**: convierte la agenda general en un plan personal.

**Independent Test**: guardar 2 charlas del mismo horario y verificar que aparecen en Mi agenda con aviso de cruce.

**Acceptance Scenarios**:

1. **Given** una charla en la Agenda, **When** toco su marcador, **Then** queda guardada: el marcador se rellena con el color del track y hace una pequeña animación de confirmación; el contador de la pestaña "Mi agenda" aumenta.
2. **Given** una charla guardada, **When** toco su marcador, **Then** se quita de mi agenda.
3. **Given** tengo charlas guardadas, **When** abro Mi agenda, **Then** veo "N charlas guardadas", el título "Mi agenda", una tarjeta destacada y la lista "Todas tus charlas" ordenada por hora.
4. **Given** una de mis charlas está marcada en vivo por el backend, **Then** la tarjeta destacada dice "EN CURSO AHORA" con "LIVE NOW"; si ninguna está en vivo, dice "TU PRÓXIMA CHARLA" con la siguiente que no ha empezado según la hora del teléfono; si no hay ninguna próxima, no se muestra.
5. **Given** guardé dos charlas cuyos horarios se superponen, **Then** ambas muestran la etiqueta amarilla "Cruce de horario".
6. **Given** no tengo charlas guardadas, **Then** veo "Aún no guardas charlas" con el botón "Explorar la agenda".
7. **Given** cierro y vuelvo a abrir la app, **Then** mis charlas guardadas se conservan (también sin conexión).

---

### User Story 4 — Ver el detalle de una charla (Priority: P1)

Como asistente, quiero ver la información completa de una charla y guardarla desde ahí.

**Independent Test**: abrir el detalle desde la Agenda y desde Mi agenda, guardar/quitar y volver.

**Acceptance Scenarios**:

1. **Given** abro una charla, **Then** veo: botón volver, "Charla N / total", botón marcador, etiqueta "Track X" (y "LIVE NOW" si está en vivo), título, cuatro datos (Hora, Sala, Duración, Nivel), "Sobre la charla" con la descripción, "Ponente" (foto, nombre, cargo y empresa) y "Temas" (etiquetas).
2. **Given** el ponente aún no está confirmado, **Then** se muestra "Ponente por confirmar · Se anunciará pronto" con un avatar genérico.
3. **Given** el detalle, **When** toco el botón inferior "Agregar a mi agenda", **Then** la charla se guarda y el botón cambia a "Guardada en tu agenda"; tocarlo de nuevo la quita.
4. **Given** llegué desde Agenda o Mi agenda, **When** toco volver, **Then** regreso a la pantalla desde la que vine.

---

### User Story 5 — Ganar gemas escaneando QR (Priority: P2)

Como asistente, quiero escanear el QR que aparece al final de cada charla (y los de stands y retos) para ganar gemas y subir en el ranking.

**Why this priority**: es el sistema de recompensas del evento; depende de la sesión y del backend.

**Independent Test**: con datos falsos, escanear un QR válido, uno repetido y uno inválido; verificar que el saldo mostrado es el que devuelve la fuente de datos.

**Acceptance Scenarios**:

1. **Given** estoy en Mis gemas, **When** toco "Escanear QR", **Then** se abre el escáner a pantalla completa con el visor, la indicación "Apunta al código de la charla, el stand o el reto." y un botón para cerrar.
2. **Given** el escáner abierto, **When** leo un QR válido, **Then** el backend lo valida y la app muestra el modal "¡+N gemas!" con el origen y el nuevo saldo, usando **exactamente** la cantidad y el saldo que devuelve el backend; opciones "Listo" y "Seguir escaneando".
3. **Given** escaneo un QR que ya usé, **Then** no sumo gemas y veo el modal "Ya registraste este código".
4. **Given** escaneo un QR que no es del evento o no es válido, **Then** no sumo gemas y veo el modal "Este QR no es del DevFest" con "Intentar de nuevo" y "Cancelar".
5. **Given** no tengo conexión, **When** escaneo, **Then** veo el modal "Sin conexión" (no se suma nada sin validar) con "Reintentar".
6. **Given** la app no tiene permiso de cámara, **Then** veo el modal "Activa la cámara" con "Abrir configuración" y "Ahora no".
7. **Given** sumé gemas, **Then** el contador de la cabecera, el saldo de Mis gemas y mi posición se actualizan con los datos del backend.

---

### User Story 6 — Ver mis gemas y los premios (Priority: P2)

Como asistente, quiero ver mi saldo, saber cómo ganar más y qué premios reciben los primeros puestos.

**Independent Test**: abrir Mis gemas y verificar el saldo animado, los botones y las secciones informativas.

**Acceptance Scenarios**:

1. **Given** toco el contador de gemas de la cabecera o "Mis gemas" en el menú, **Then** abro Mis gemas.
2. **Given** abro Mis gemas, **Then** veo la tarjeta "TU SALDO" con el número de gemas animado de 0 al saldo y "gemas recolectadas", el botón "Escanear QR" (Charlas, stands y retos), el botón "Ver ranking · Vas en el puesto #N de M", la sección "Cómo ganar gemas" y la sección de premios.
3. **Given** la sección "Cómo ganar gemas", **Then** lista las formas de ganar (asistir a una charla, escanear el QR de un stand, completar retos) con el texto y la cantidad de gemas que entrega el backend.
4. **Given** la sección de premios, **Then** muestra los premios (p. ej. Stickers, Polos, Certificaciones, Cursos) y para qué puestos del ranking son, **sin precios en gemas y sin botón de canje**.

---

### User Story 7 — Consultar el ranking (Priority: P2)

Como asistente, quiero ver quién lleva más gemas y en qué puesto estoy.

**Independent Test**: abrir el Ranking con datos falsos donde el usuario está fuera del top 10 y verificar la fila separadora, la barra fija y el refresh.

**Acceptance Scenarios**:

1. **Given** abro el Ranking, **Then** veo el título "Ranking", "Quién lleva más gemas en el DevFest", "Actualizado a las HH:MM" y un podio con los puestos 1, 2 y 3 (1.º al centro y más alto, en amarillo; 2.º a la izquierda en celeste; 3.º a la derecha en verde) con foto o inicial, nombre completo y gemas (con animación de conteo).
2. **Given** el podio, **Then** debajo veo la lista del puesto 4 al 10 con posición, foto o inicial, nombre completo y gemas.
3. **Given** mi puesto es mayor que 10, **Then** después del puesto 10 aparece una fila "• • •" y luego mi fila resaltada ("Tú"); si es exactamente 11, no aparece la fila "• • •".
4. **Given** estoy en el top 10, **Then** mi fila de la lista (o mi lugar en el podio) aparece resaltada con mi foto.
5. **Given** siempre, **Then** una barra fija inferior muestra mi foto, "Tu posición #N de M" y mi saldo.
6. **Given** toco el botón de refresh (arriba a la derecha), **Then** el ícono gira, el botón se deshabilita, el texto cambia a "Actualizando…" y la app pide el ranking de nuevo; al terminar muestra "Actualizado hace un momento". El orden (incluido el desempate) es el que entrega el backend.

---

### User Story 8 — Perfil y menú (Priority: P2)

Como asistente, quiero ver mi cuenta, navegar a las secciones y cerrar sesión.

**Acceptance Scenarios**:

1. **Given** toco mi foto en la cabecera, **Then** abro Perfil y menú: foto grande, nombre completo, "Conectado con {proveedor}" con el logo del proveedor, y las opciones Agenda, Mi agenda (N charlas guardadas), Mis gemas (N gemas recolectadas) y Ranking (Vas en el puesto #N), más el botón "Cerrar sesión" y, debajo, una línea discreta y centrada con los enlaces "Términos · Política de privacidad" (sin hashtag).
2. **Given** toco "Cerrar sesión", **Then** aparece el modal "¿Cerrar sesión?" que avisa "Perderás las N charlas guardadas en tu agenda." (o, si no tengo guardadas, que tendré que volver a iniciar sesión), con "Cerrar sesión" y "Cancelar".
3. **Given** el modal de cierre, **When** confirmo, **Then** se borran las charlas guardadas del dispositivo, se cierra la sesión y vuelvo a Login; **When** cancelo, sigo en Perfil sin cambios.
4. **Given** el usuario no tiene foto en su cuenta, **Then** se muestra un avatar con su inicial.

---

### Edge Cases

- **Sin conexión**: la agenda y Mi agenda funcionan con la última información guardada (el estado "LIVE NOW" puede estar desactualizado); escanear muestra el modal "Sin conexión" y las recargas fallidas (agenda, saldo, ranking) muestran el modal "No se pudo actualizar".
- **Primera apertura sin conexión**: no hay agenda guardada → estado "Sin conexión" con "Intentar de nuevo" (pantalla 02c).
- **Hora del teléfono mal configurada**: solo afecta la línea "AHORA", el atenuado de sesiones pasadas y "Tu próxima charla"; "LIVE NOW" no depende del reloj.
- **Zona horaria**: las horas de la agenda se muestran en hora de Lima (America/Lima) aunque el teléfono esté en otra zona; la comparación para "AHORA" convierte la hora del teléfono a esa zona.
- **Fuera del 21 de noviembre**: no aparece la línea "AHORA"; antes del evento ninguna sesión se ve pasada; después del evento todas se ven pasadas.
- **Cambios en la agenda**: si una charla guardada se elimina de la agenda publicada, desaparece de Mi agenda sin error.
- **Textos largos**: títulos de charla, nombres de ponentes y nombres del ranking largos se ajustan en varias líneas o se recortan con "…" (ranking) sin romper el diseño.
- **Tamaño de letra del sistema grande**: el contenido sigue siendo legible y usable; nada queda cortado sin posibilidad de desplazarse.
- **"Reducir movimiento" activado**: no hay stickers flotantes, contadores animados ni desplazamientos; solo fundidos (ver FR-060).
- **Sesión vencida**: si el backend rechaza la sesión, se muestra el modal "Tu sesión expiró" y al aceptar se vuelve a Login; las charlas guardadas se conservan (solo el cierre de sesión voluntario las borra).
- **Doble toque**: tocar dos veces rápido un botón de inicio de sesión, escaneo, refresh o marcador no produce acciones duplicadas.

---

## Requirements *(mandatory)*

### Functional Requirements

**Sesión**
- **FR-001**: El sistema DEBE permitir iniciar sesión con Google, Sign in with Apple y GitHub, sin usuario ni contraseña propios.
- **FR-002**: El sistema DEBE mantener la sesión entre aperturas de la app hasta que el usuario cierre sesión o la sesión venza.
- **FR-003**: El sistema DEBE obtener el nombre completo y la foto del usuario, y recordar con qué proveedor inició sesión.
- **FR-004**: El sistema DEBE mostrar estado de carga ("Conectando…"), deshabilitar los demás botones durante el inicio de sesión y mostrar el modal de error si falla o se cancela.
- **FR-005**: El sistema DEBE mostrar en Login, debajo de la tarjeta "Inicia sesión" (fuera de ella), el texto "Al continuar aceptas los Términos y la Política de privacidad" en gris pequeño, centrado, con los enlaces en el mismo gris subrayados finos y sin partir "Política de privacidad" entre líneas; y en Perfil, debajo de "Cerrar sesión", la línea centrada "Términos · Política de privacidad" con el mismo estilo. Las URLs son configurables.
- **FR-006**: El sistema NO crea cuentas propias: usa solo los datos del proveedor OAuth2 (nombre, foto, id).
- **FR-007**: Al cerrar sesión, el sistema DEBE pedir confirmación con un modal que avisa cuántas charlas guardadas se perderán; al confirmar, DEBE borrar las charlas guardadas del dispositivo.

**Agenda**
- **FR-010**: El sistema DEBE mostrar las sesiones del 21 de noviembre ordenadas por hora de inicio, distinguiendo charlas (tocables) de pausas (no tocables).
- **FR-011**: Cada charla DEBE mostrar track, título, sala, duración en minutos, nivel y estado de guardado; su tarjeta usa siempre el color de su track.
- **FR-012**: El sistema DEBE ofrecer filtros "Todo", "IA", "Web", "Mobile" y "Cloud".
- **FR-013**: El sistema DEBE mostrar "LIVE NOW" en las charlas que el backend marca como en vivo (flag del administrador).
- **FR-014**: El sistema DEBE atenuar las sesiones pasadas y mostrar la línea "AHORA" usando la hora del teléfono convertida a America/Lima.
- **FR-015**: El sistema DEBE mostrar los estados vacíos: agenda sin charlas y track sin charlas.
- **FR-016**: El sistema DEBE guardar localmente la última agenda recibida para funcionar sin conexión.
- **FR-017**: El sistema DEBE recargar la agenda cada vez que se entra a la Agenda o a Mi agenda, y con el gesto pull-to-refresh.
- **FR-018**: Mientras carga datos por primera vez (agenda, saldo, ranking), el sistema DEBE mostrar esqueletos: bloques grises con la forma del contenido final (tarjetas de charla, tarjeta de saldo, filas del ranking), en el mismo lenguaje visual (radios y espaciado del diseño, sin bordes negros ni sombras), con un brillo suave que se desactiva con "reducir movimiento".
- **FR-019**: Si no hay agenda guardada y no hay conexión, el sistema DEBE mostrar el estado "Sin conexión" con el botón "Intentar de nuevo" (con estado "Conectando…").

**Mi agenda**
- **FR-020**: El usuario DEBE poder guardar y quitar charlas desde la Agenda, Mi agenda y el Detalle.
- **FR-021**: Las charlas guardadas DEBEN persistir en el dispositivo y funcionar sin conexión.
- **FR-022**: El sistema DEBE detectar y marcar "Cruce de horario" cuando dos charlas guardadas se superponen.
- **FR-023**: El sistema DEBE mostrar la tarjeta destacada: "En curso ahora" si alguna charla guardada está en vivo (flag del backend); si no, "Tu próxima charla" según la hora del teléfono.

**Detalle de charla**
- **FR-030**: El sistema DEBE mostrar todos los datos de la charla y del ponente (nombre, foto, cargo, empresa) o el estado "Ponente por confirmar".
- **FR-031**: "Charla N / total" DEBE calcularse con las charlas (sin pausas) ordenadas por hora de inicio.

**Gemas**
- **FR-040**: El sistema DEBE ofrecer el botón "Escanear QR" en Mis gemas y abrir un escáner con la cámara.
- **FR-041**: Cada QR DEBE enviarse al backend para validarse; el backend decide si es válido, si ya se usó y cuántas gemas otorga. La app NO DEBE calcular ni fijar cantidades de gemas.
- **FR-042**: El sistema DEBE mostrar el resultado del escaneo con los modales del diseño: gemas sumadas (cantidad y saldo del backend), código ya usado, código no válido, sin conexión, sin permiso de cámara.
- **FR-043**: El sistema DEBE mostrar el saldo de gemas en la cabecera y en Mis gemas, actualizado tras cada escaneo exitoso.
- **FR-044**: El sistema DEBE mostrar "Cómo ganar gemas" y los premios de los primeros puestos con los datos del backend, sin precios ni canje.

**Ranking**
- **FR-050**: El sistema DEBE mostrar el ranking que entrega el backend (quién aparece, el orden y el desempate los decide el backend), con nombre completo, foto o inicial y gemas de cada participante.
- **FR-051**: El sistema DEBE mostrar el podio (1–3), la lista (4–10), la posición del usuario si está fuera del top 10 (con "• • •" si hay puestos intermedios) y una barra fija con "Tu posición #N de M".
- **FR-052**: El usuario DEBE poder actualizar el ranking con un botón de refresh que muestra estado de carga y la hora de la última actualización.

**Navegación**
- **FR-055**: La navegación principal DEBE tener dos pestañas: "Agenda" y "Mi agenda" (con contador de guardadas).
- **FR-056**: Desde la cabecera, la foto abre Perfil y el contador de gemas abre Mis gemas.
- **FR-057**: "Volver" DEBE regresar a la pantalla anterior real en cualquier secuencia (p. ej. Perfil → Mis gemas → Ranking → volver → volver → Perfil).

**Movimiento y accesibilidad**
- **FR-060**: El sistema DEBE respetar la opción "reducir movimiento" del sistema: sin stickers flotantes, sin contadores animados, sin desplazamientos; se conservan los fundidos.
- **FR-061**: Todos los elementos tocables DEBEN medir al menos 44×44 pt y tener descripción para lectores de pantalla (incluidos los botones de solo ícono: marcador, volver, refresh, foto, gemas, cerrar escáner).
- **FR-062**: Los textos DEBEN respetar el tamaño de letra del sistema.
- **FR-063**: El contraste de texto DEBE ser al menos 4.5:1 (3:1 en textos de 24 pt o más).

**Modales**
- **FR-065**: Todos los avisos y errores DEBEN mostrarse con un único componente de modal genérico que se configura en el momento de usarlo (patrón Builder), sin crear un modal por pantalla. Parámetros: título; texto; ícono; color de fondo del ícono; color del ícono; color de sombra de la tarjeta; botón principal (texto y acción); botón secundario opcional (texto y acción); si se puede cerrar tocando fuera.
- **FR-066**: Casos mínimos a construir con ese componente (catálogo en la pantalla 08): no se pudo iniciar sesión, sesión vencida, confirmar cierre de sesión, gemas sumadas, QR ya usado, QR no válido, sin conexión (escáner), permiso de cámara, no se pudo actualizar.
- **FR-067**: El modal DEBE aparecer con fundido del fondo y escala 0.95 → 1 de la tarjeta (≈220 ms), anunciarse como diálogo a los lectores de pantalla y llevar el foco al título.

**Datos**
- **FR-070**: En esta versión, todos los datos (agenda, ponentes, usuario, gemas, resultados de QR, ranking, premios) DEBEN provenir de datos falsos locales detrás de una capa de repositorios, que luego se implementa contra el backend según `contracts/api.yaml`. Si el backend real difiere del contrato, se agrega un adapter sin cambiar las pantallas.
- **FR-071**: La app DEBE estar solo en español.

### Key Entities

- **Evento**: nombre, edición, fecha única (2026-11-21), ciudad, zona horaria (America/Lima), hashtag.
- **Track**: id, nombre, color. Fijos: IA (#4285F4), Web (#A142F4), Mobile (#34A853), Cloud (#FBBC04), General (#9AA0A6).
- **Sesión**: id, tipo (charla | pausa), título, hora inicio, hora fin, sala, track, nivel (Básico | Intermedio | Avanzado | Todos), descripción, temas, ponente(s), en vivo (flag del administrador).
- **Ponente**: nombre, foto, cargo, empresa (o "por confirmar").
- **Usuario**: id, nombre completo, foto, proveedor de inicio de sesión, saldo de gemas, posición en el ranking, total de participantes.
- **Charla guardada**: sesión guardada por el usuario (local en el dispositivo).
- **Resultado de escaneo**: resultado (sumado | ya usado | no válido), gemas otorgadas, nuevo saldo, origen (charla | stand | reto, con nombre).
- **Forma de ganar gemas**: tipo, título, descripción, gemas (informativo, del backend).
- **Premio**: nombre, descripción, puestos que lo reciben (informativo, sin precio).
- **Entrada de ranking**: posición, usuario (nombre completo, foto), gemas, es el usuario actual.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un asistente nuevo pasa de abrir la app a ver la Agenda en menos de 30 segundos.
- **SC-002**: Un asistente encuentra la charla en vivo de un track en menos de 5 segundos desde la Agenda.
- **SC-003**: La Agenda y Mi agenda abren y se usan sin conexión con la última información guardada.
- **SC-004**: El resultado de un escaneo QR se muestra en menos de 3 segundos con conexión normal.
- **SC-005**: El saldo y la cantidad de gemas mostrados coinciden siempre con los que devuelve el backend (la app nunca suma por su cuenta).
- **SC-006**: Las 8 pantallas y los modales coinciden con el prototipo de referencia (textos, colores, orden de elementos) en Android e iOS.
- **SC-007**: Todas las acciones principales se pueden completar con un lector de pantalla (TalkBack / VoiceOver).
- **SC-008**: La lógica compartida (atenuado/AHORA según hora del teléfono, LIVE NOW según flag, cruces de horario, tarjeta destacada, filtros, armado del ranking con "• • •") tiene tests automatizados.

---

## Assumptions

- El panel de administración y el backend propio son proyectos aparte; esta spec solo cubre la app de asistentes.
- Hay un solo evento (DevFest Lima 2026, 21 de noviembre) por instalación de la app.
- Mientras se usan datos falsos, el escaneo QR funciona con códigos de prueba definidos en esos datos.
- Las fotos de los ponentes y de los usuarios pueden faltar; siempre hay un reemplazo (inicial o avatar genérico).
- Las gemas nunca bajan (no hay canjes).

## Out of Scope (esta versión)

- Modo oscuro.
- Otros idiomas distintos al español.
- Notificaciones (recordatorios de charlas, avisos del evento).
- Usuario/contraseña propios, registro y eliminación de cuenta (solo OAuth2).
- Canje de gemas por premios y precios en gemas.
- Entrega de premios a los primeros puestos (se gestiona fuera de la app).
- Panel de administración y backend (solo se consume su contrato).
- Mapa del evento, lista de ponentes, patrocinadores.

---

## Appendix A — Pantallas (según el prototipo)

| # | Pantalla | Contenido clave |
|---|---|---|
| 01 | Login | Lockup "{ DevFest Lima }", "Community Edition" + "2026", texto introductorio, "Sáb 21 nov · Lima", tarjeta "Inicia sesión" con Google / Apple / GitHub, aviso de Términos y Privacidad debajo de la tarjeta, "#DevFestLima26", 6 stickers decorativos flotantes |
| 02 | Agenda | Cabecera (logo, gemas, foto), "Community Edition"/"2026", "Agenda", 2 stickers flotantes en la cabecera, filtros, lista con línea AHORA, barra inferior Agenda/Mi agenda |
| 02b | Agenda sin charlas | Igual que Agenda, sin filtros, con estado vacío |
| 02c | Agenda sin conexión | Igual que 02b, con ícono de wifi tachado, "Sin conexión", texto de ayuda y botón "Intentar de nuevo" (estado "Conectando…") |
| 03 | Mi agenda | Cabecera, "N charlas guardadas", tarjeta destacada, "Todas tus charlas", avisos de cruce, barra inferior |
| 04 | Detalle de charla | Volver, "Charla N / total", marcador, tarjeta con track/título/4 datos, descripción, ponente, temas, botón fijo "Agregar a mi agenda" |
| 05 | Perfil y menú | Foto, nombre, proveedor, 4 opciones, "Cerrar sesión" (con modal de confirmación), línea "Términos · Política de privacidad", stickers flotantes (sin hashtag) |
| 06 | Mis gemas | "Mis gemas", tarjeta de saldo con contador animado, "Escanear QR" (con escáner), "Ver ranking", cómo ganar gemas, premios |
| 07 | Ranking | Refresh, "Actualizado a las …", podio con conteo animado, lista 4–10, "• • •", fila propia, barra fija "Tu posición" |
| 08 | Modales (ModalBuilder) | Catálogo de 9 casos del mismo componente: login fallido, sesión vencida, confirmar cierre de sesión, +N gemas, QR ya usado, QR no válido, sin conexión, permiso de cámara, no se pudo actualizar |

**Solo en el prototipo (no van en la app):**

- Dentro del escáner de Mis gemas, la fila "Simular resultado (solo prototipo)" con botones Charla +20, Stand +40, Reto +80, Ya usado, No válido, Sin conexión y Sin cámara. Existen porque el prototipo no tiene cámara; en la app el resultado sale del escaneo real.
- El panel "Tweaks" (hora simulada, saldo de gemas, pantalla inicial, etc.).
- Los valores +20 / +40 / +80 de "Cómo ganar gemas" son de ejemplo; los reales vienen del backend.

**Aún sin diseño:**

- Esqueletos de carga (FR-018).
- Ícono de la app y pantalla de arranque (splash).

## Appendix B — Datos falsos de agenda (del prototipo)

Día: sábado 21 de noviembre de 2026, Lima. En los datos falsos, las charlas de las 11:30 tienen el flag `isLive = true`.

| Inicio | Fin | Tipo | Título | Track | Sala | Nivel |
|---|---|---|---|---|---|---|
| 08:30 | 09:30 | pausa | Registro y desayuno | — | — | — |
| 09:30 | 10:15 | charla | Keynote: la comunidad que construye el futuro | General | Auditorio | Todos |
| 10:30 | 11:15 | charla | Flutter en producción: de cero a las tiendas | Mobile | Sala 1 | Intermedio |
| 10:30 | 11:15 | charla | Angular sin miedo: signals y SSR | Web | Sala 2 | Intermedio |
| 11:30 | 12:15 | charla | Serverless en Google Cloud: arquitectura real | Cloud | Sala 3 | Avanzado |
| 11:30 | 12:15 | charla | Agentes con la Gemini API: del prototipo al producto | IA | Sala 1 | Intermedio |
| 12:30 | 13:45 | pausa | Almuerzo y networking | — | — | — |
| 14:00 | 14:45 | charla | Kotlin Multiplatform: compartir código sin sufrir | Mobile | Sala 2 | Intermedio |
| 14:00 | 14:45 | charla | Accesibilidad web que sí se nota | Web | Sala 1 | Básico |
| 15:00 | 15:45 | charla | Firebase + IA: apps que aprenden | Cloud | Sala 3 | Intermedio |
| 16:00 | 16:45 | charla | Panel: comunidades tech en el Perú | General | Auditorio | Todos |
| 17:00 | 17:30 | pausa | Cierre y sorteos | — | — | — |

Las descripciones y temas de cada charla están en el prototipo. Ponentes: "por confirmar". Ranking falso: 13 participantes con 480, 455, 410, 365, 330, 290, 260, 215, 190, 160, 135, 95 y 60 gemas; el usuario falso tiene 120 gemas (puesto 12 de 14). Formas de ganar (valores de ejemplo): charla +20, stand +40, reto +80.
