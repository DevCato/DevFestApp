<!--
Sync Impact Report
==================
Version change: (plantilla sin ratificar) → 1.0.0
Bump: adopción inicial; primera versión ratificada de la constitución.

Principios modificados (placeholder → título definido):
- [PRINCIPLE_1_NAME] → I. Lógica compartida primero (commonMain)
- [PRINCIPLE_2_NAME] → II. Lógica probada en commonTest
- [PRINCIPLE_3_NAME] → III. Datos detrás de repositorios
- [PRINCIPLE_4_NAME] → IV. UI solo en español y solo tema claro
- [PRINCIPLE_5_NAME] → V. Respeto a "reducir movimiento"

Secciones añadidas:
- Restricciones de plataforma y stack (antes [SECTION_2_NAME])
- Flujo de desarrollo y controles de calidad (antes [SECTION_3_NAME])
- Governance (reglas concretas)

Secciones eliminadas: ninguna

TODOs pendientes: ninguno

Nota: este reporte es material temporal de revisión; eliminarlo antes de hacer commit.
-->

# DevfestApp Constitution

## Core Principles

### I. Lógica compartida primero (commonMain)

- Toda la lógica de negocio, los modelos de dominio, el estado de presentación y la UI en
  Compose Multiplatform DEBEN vivir en `shared/src/commonMain`.
- El código en `androidMain`, `iosMain`, `androidApp` e `iosApp` DEBE limitarse a puntos de
  entrada y a integraciones que requieren APIs de plataforma.
- Toda API de plataforma DEBE exponerse a `commonMain` mediante una interfaz o una declaración
  `expect`/`actual`; `commonMain` NO DEBE depender de tipos específicos de Android o iOS.
- Cada pieza de código específico de plataforma DEBE justificarse en el plan de la feature.

**Justificación**: una sola implementación garantiza el mismo comportamiento en Android e iOS y
evita duplicar esfuerzo y defectos.

### II. Lógica probada en commonTest

- Toda lógica en `commonMain` (casos de uso, repositorios, mapeos, reducción de estado) DEBE
  tener tests en `shared/src/commonTest`.
- Los tests DEBEN ejecutarse y pasar en ambos targets: `./gradlew :shared:testAndroidHostTest`
  y `./gradlew :shared:iosSimulatorArm64Test`.
- Los tests DEBEN ser deterministas: sin red, sin reloj real y sin dependencia del orden de
  ejecución. Las dependencias se sustituyen por implementaciones fake.
- Un cambio de lógica sin tests que lo cubran NO DEBE fusionarse.

**Justificación**: los tests comunes verifican el comportamiento una vez para todas las
plataformas y protegen la migración futura de datos fake a backend.

### III. Datos detrás de repositorios

- La UI y la lógica de presentación DEBEN obtener datos únicamente a través de interfaces de
  repositorio definidas en `commonMain`; NO DEBEN conocer el origen de los datos.
- Las interfaces de repositorio DEBEN exponer modelos de dominio, nunca DTOs ni tipos de red o
  de persistencia.
- La implementación actual DEBE ser de datos fake. La conexión a backend DEBE incorporarse como
  adapters que implementan las mismas interfaces, sin modificar la UI ni los consumidores.
- Las interfaces DEBEN diseñarse desde ahora para un origen remoto: operaciones asíncronas
  (`suspend`/`Flow`) y resultados que representan carga y error.

**Justificación**: aislar el origen de datos permite avanzar con datos fake hoy y cambiar a
backend después reemplazando solo el adapter.

### IV. UI solo en español y solo tema claro

- Todo texto visible para el usuario DEBE estar en español. NO se admiten otros idiomas ni
  selector de idioma.
- Los textos DEBEN declararse como recursos de cadena compartidos; NO DEBEN escribirse como
  literales dentro de los composables.
- La app DEBE usar un único tema claro. NO DEBE implementarse modo oscuro y la UI NO DEBE
  cambiar de apariencia cuando el sistema está en modo oscuro.
- Fechas, horas y números DEBEN formatearse con convenciones en español.

**Justificación**: un solo idioma y un solo tema reducen el alcance de diseño y pruebas, y
centralizar los textos mantiene la consistencia del contenido.

### V. Respeto a "reducir movimiento"

- La app DEBE leer la preferencia de accesibilidad del sistema para reducir movimiento en
  Android e iOS y exponerla a `commonMain` mediante una abstracción compartida.
- Con la preferencia activa, las animaciones no esenciales (transiciones decorativas, parallax,
  desplazamientos automáticos, animaciones en bucle) DEBEN desactivarse o sustituirse por
  cambios instantáneos o fundidos simples.
- Ninguna información ni funcionalidad DEBE depender exclusivamente de una animación.
- Toda animación nueva DEBE definir su comportamiento con la preferencia activa.

**Justificación**: el movimiento puede causar malestar a algunas personas; respetar la
preferencia del sistema es un requisito de accesibilidad, no una mejora opcional.

## Restricciones de plataforma y stack

- **Tecnología**: Kotlin Multiplatform con Compose Multiplatform para la UI compartida.
- **Plataformas soportadas**: Android 8.0 o superior (`minSdk` 26) e iOS 16 o superior. NO se
  admiten APIs que requieran versiones superiores sin una alternativa para las mínimas.
- **Paquete**: todo el código DEBE residir bajo el paquete raíz `pe.gdg.open.devfest.app`,
  que también es el identificador base de la aplicación.
- **Módulos**: `shared` contiene el código común; `androidApp` e `iosApp` son puntos de entrada.
- **Dependencias**: toda librería usada en `commonMain` DEBE ser compatible con Kotlin
  Multiplatform para Android e iOS.

## Flujo de desarrollo y controles de calidad

- Cada spec y cada plan DEBEN incluir una verificación de cumplimiento de esta constitución
  antes de iniciar la implementación.
- Antes de fusionar un cambio DEBEN cumplirse todos estos controles:
  - Los tests de `commonTest` pasan en los targets de Android e iOS.
  - La app compila para Android (`./gradlew :androidApp:assembleDebug`) y para iOS.
  - No hay textos de UI fuera de los recursos de cadena ni en un idioma distinto al español.
  - Las animaciones nuevas se verificaron con "reducir movimiento" activado.
  - El acceso a datos nuevo pasa por una interfaz de repositorio.
- Toda desviación de un principio DEBE documentarse en el plan con su justificación y la
  alternativa más simple que se descartó.

## Governance

- Esta constitución prevalece sobre cualquier otra práctica o convención del proyecto.
- **Enmiendas**: toda modificación DEBE realizarse editando este archivo, documentar el motivo
  del cambio y, si afecta código existente, incluir un plan de migración. Requiere la aprobación
  de quienes mantienen el proyecto.
- **Versionado**: se usa versionado semántico.
  - MAJOR: eliminación o redefinición incompatible de un principio o regla de gobernanza.
  - MINOR: principio o sección nuevos, o ampliación sustancial de una guía.
  - PATCH: aclaraciones, redacción y correcciones sin cambio de significado.
- **Cumplimiento**: toda revisión de código DEBE verificar el cumplimiento de estos principios.
  Un cambio que los incumple sin una desviación documentada NO DEBE fusionarse.

**Version**: 1.0.0 | **Ratified**: 2026-09-30 | **Last Amended**: 2026-09-30
