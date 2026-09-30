# Quickstart: validar la app DevFest Lima 2026

**Fecha**: 2026-09-30 | **Plan**: [plan.md](plan.md) | **Datos**: [data-model.md](data-model.md) |
**Contratos**: [contracts/api.yaml](contracts/api.yaml), [contracts/repositories.md](contracts/repositories.md)

Guía para comprobar que la feature funciona de punta a punta con datos falsos.

## Requisitos

- JDK 17 o superior y Android SDK con la plataforma del `compileSdk` del catálogo.
- Dispositivo o emulador Android 8.0+ (API 26+). Para el escáner, un dispositivo con cámara.
- Para iOS: macOS con Xcode y un simulador o dispositivo iOS 16+. **Los pasos de iOS no se
  pueden ejecutar en Windows**; se corren en una Mac o en CI con macOS.
- Opcional para login real: `androidApp/google-services.json` e
  `iosApp/iosApp/GoogleService-Info.plist`. Sin ellos la app usa el login simulado.

## Compilar y probar

```bash
# Tests de la lógica compartida
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test      # solo macOS

# App Android
./gradlew :androidApp:assembleDebug
```

iOS: abrir `iosApp/` en Xcode y ejecutar el esquema `iosApp`.

**Resultado esperado**: todos los tests pasan en ambos targets y las dos apps compilan.

## Escenarios de validación

Los textos exactos de cada pantalla y modal están en [spec.md](spec.md). Los códigos QR de
prueba y los escenarios del fake están en [data-model.md](data-model.md#datos-falsos).

### 1. Login (US1)

1. Abrir la app sin sesión → pantalla de Login con los tres botones.
2. Tocar un proveedor → el botón muestra "Conectando…" y los otros se deshabilitan.
3. Completar el flujo → se llega a la Agenda.
4. Repetir cancelando el flujo → modal "No pudimos iniciar sesión".
5. Cerrar y abrir la app → entra directo a la Agenda.

### 2. Agenda (US2)

1. La lista muestra las 12 filas del Appendix B ordenadas por hora; las pausas no son tocables.
2. Las dos charlas de las 11:30 muestran "LIVE NOW".
3. Poner la fecha del teléfono en 2026-11-21 a las 13:00 (hora de Lima) → las sesiones
   anteriores se atenúan y la línea "AHORA" aparece antes de la sesión de las 14:00.
4. Poner otra fecha → no hay línea "AHORA".
5. Tocar cada filtro → solo charlas de ese track, sin pausas.
6. Deslizar hacia abajo → la agenda se recarga.
7. Con `FakeScenario` de agenda vacía → estado "Aún no hay charlas", sin filtros.
8. Con `FakeScenario` de fallo de red y sin caché (instalación limpia) → estado "Sin conexión"
   con "Intentar de nuevo".
9. Con `FakeScenario` de fallo de red y con caché → se ve la agenda guardada y el modal
   "No se pudo actualizar".

### 3. Mi agenda y detalle (US3, US4)

1. Guardar las dos charlas de las 10:30 → el contador de la pestaña sube a 2 y ambas muestran
   "Cruce de horario" en Mi agenda.
2. Guardar una charla de las 11:30 → la tarjeta destacada dice "EN CURSO AHORA".
3. Quitar todas → estado "Aún no guardas charlas".
4. Abrir un detalle → "Charla N / 9", ponente "por confirmar"; el botón inferior guarda y quita.
5. Volver regresa a la pantalla de origen (Agenda o Mi agenda).
6. Cerrar y abrir la app → las charlas guardadas siguen ahí.

### 4. Gemas y escáner (US5, US6)

1. Tocar el contador de gemas → Mis gemas con saldo 120 animado desde 0.
2. "Escanear QR" sin permiso de cámara → modal "Activa la cámara".
3. Escanear `DFL26-TALK-t5-7f3a` → modal "¡+20 gemas!" con saldo 140; la cabecera muestra 140.
4. Escanear el mismo código → modal "Ya registraste este código"; el saldo no cambia.
5. Escanear un QR cualquiera → modal "Este QR no es del DevFest".
6. Con `FakeScenario` de fallo de red → modal "Sin conexión"; el saldo no cambia.
7. La sección de premios no muestra precios ni botón de canje.

### 5. Ranking (US7)

1. Abrir el Ranking → podio 1–3, lista 4–10, fila "• • •" y la fila propia en el puesto 12.
2. La barra fija muestra "Tu posición #12 de 14".
3. Tocar refresh → el ícono gira, el botón se deshabilita y luego "Actualizado hace un momento".

### 6. Perfil y cierre de sesión (US8)

1. Tocar la foto → Perfil con nombre, proveedor y las cuatro opciones.
2. Perfil → Mis gemas → Ranking → volver → volver → se regresa a Perfil.
3. "Cerrar sesión" con charlas guardadas → el modal avisa cuántas se perderán.
4. Cancelar → nada cambia. Confirmar → vuelve a Login y Mi agenda queda vacía al reingresar.
5. Con `FakeScenario` de sesión vencida → modal "Tu sesión expiró", vuelta a Login y las charlas
   guardadas se conservan.

### 7. Accesibilidad y apariencia

1. Activar "reducir movimiento" (Android: quitar animaciones; iOS: Reducir movimiento) → no hay
   stickers flotantes, contadores animados, brillo en esqueletos ni punto que late; los
   fundidos se conservan.
2. Poner el sistema en modo oscuro → la app no cambia de apariencia.
3. Poner el sistema en otro idioma → la app sigue en español.
4. Subir el tamaño de letra al máximo → todo el contenido sigue accesible con desplazamiento.
5. Con TalkBack / VoiceOver → todos los botones de solo ícono tienen descripción y los modales
   se anuncian como diálogo.

## Controles de la constitución antes de fusionar

- [ ] Tests de `commonTest` en verde en Android e iOS.
- [ ] La app compila para Android e iOS.
- [ ] Ningún texto de UI fuera de `strings.xml` ni en otro idioma.
- [ ] Animaciones nuevas verificadas con "reducir movimiento".
- [ ] Todo acceso a datos pasa por una interfaz de repositorio.
