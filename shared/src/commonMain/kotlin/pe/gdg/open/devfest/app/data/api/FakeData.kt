package pe.gdg.open.devfest.app.data.api

import pe.gdg.open.devfest.app.data.api.dto.AgendaDto
import pe.gdg.open.devfest.app.data.api.dto.EarnWayDto
import pe.gdg.open.devfest.app.data.api.dto.EventDto
import pe.gdg.open.devfest.app.data.api.dto.GemsInfoDto
import pe.gdg.open.devfest.app.data.api.dto.PrizeDto
import pe.gdg.open.devfest.app.data.api.dto.ScanSourceDto
import pe.gdg.open.devfest.app.data.api.dto.SessionDto
import pe.gdg.open.devfest.app.data.api.dto.TrackDto

/**
 * Datos falsos del Appendix B de spec.md y del prototipo, con la forma exacta de
 * contracts/api.yaml. Los valores de gemas son de ejemplo: los reales los decide el backend.
 */
object FakeData {

    val event = EventDto(
        id = "devfest-lima-2026",
        name = "DevFest Lima",
        edition = "Community Edition",
        year = 2026,
        date = "2026-11-21",
        city = "Lima",
        timezone = "America/Lima",
        hashtag = "#DevFestLima26",
    )

    val tracks = listOf(
        TrackDto("ia", "IA", "#4285F4"),
        TrackDto("web", "Web", "#A142F4"),
        TrackDto("mobile", "Mobile", "#34A853"),
        TrackDto("cloud", "Cloud", "#FBBC04"),
        TrackDto("general", "General", "#9AA0A6"),
    )

    private fun at(time: String) = "2026-11-21T$time:00-05:00"

    private fun pause(id: String, start: String, end: String, title: String) = SessionDto(
        id = id,
        type = "break",
        title = title,
        startsAt = at(start),
        endsAt = at(end),
    )

    private fun talk(
        id: String,
        start: String,
        end: String,
        track: String,
        room: String,
        level: String,
        title: String,
        description: String,
        topics: List<String>,
        isLive: Boolean = false,
    ) = SessionDto(
        id = id,
        type = "talk",
        title = title,
        startsAt = at(start),
        endsAt = at(end),
        room = room,
        trackId = track,
        level = level,
        description = description,
        topics = topics,
        speakers = emptyList(),
        isLive = isLive,
    )

    /** 12 sesiones: 9 charlas y 3 pausas. Las dos charlas de las 11:30 están en vivo. */
    val sessions = listOf(
        pause("b1", "08:30", "09:30", "Registro y desayuno"),
        talk(
            "t1", "09:30", "10:15", "general", "Auditorio", "Todos",
            "Keynote: la comunidad que construye el futuro",
            "Apertura oficial del DevFest Lima 2026. Un recorrido por lo que viene en el ecosistema de desarrollo de Google y por qué las comunidades locales son el motor de todo.",
            listOf("Keynote", "Comunidad"),
        ),
        talk(
            "t2", "10:30", "11:15", "mobile", "Sala 1", "Intermedio",
            "Flutter en producción: de cero a las tiendas",
            "Arquitectura, testing y despliegue de una app Flutter real en Android y iOS. Qué funcionó, qué no y cómo automatizar las publicaciones en Google Play y App Store.",
            listOf("Flutter", "Dart", "CI/CD"),
        ),
        talk(
            "t3", "10:30", "11:15", "web", "Sala 2", "Intermedio",
            "Angular sin miedo: signals y SSR",
            "Una guía práctica para migrar a signals, activar el renderizado del lado del servidor y medir el impacto en el rendimiento de tu app web.",
            listOf("Angular", "Signals", "SSR"),
        ),
        talk(
            "t4", "11:30", "12:15", "cloud", "Sala 3", "Avanzado",
            "Serverless en Google Cloud: arquitectura real",
            "Cloud Run, Pub/Sub y Firestore trabajando juntos. Patrones para escalar sin administrar servidores y cómo mantener los costos bajo control.",
            listOf("Cloud Run", "Pub/Sub", "Firestore"),
            isLive = true,
        ),
        talk(
            "t5", "11:30", "12:15", "ia", "Sala 1", "Intermedio",
            "Agentes con la Gemini API: del prototipo al producto",
            "Cómo diseñar, evaluar y poner en producción un agente construido con la Gemini API: herramientas, memoria, límites de costo y las lecciones aprendidas en el camino.",
            listOf("Gemini API", "Agentes", "IA"),
            isLive = true,
        ),
        pause("b2", "12:30", "13:45", "Almuerzo y networking"),
        talk(
            "t6", "14:00", "14:45", "mobile", "Sala 2", "Intermedio",
            "Kotlin Multiplatform: compartir código sin sufrir",
            "Estrategias para compartir la lógica de negocio entre Android e iOS manteniendo interfaces nativas, con ejemplos de un equipo que ya lo hizo.",
            listOf("Kotlin", "KMP", "Android"),
        ),
        talk(
            "t7", "14:00", "14:45", "web", "Sala 1", "Básico",
            "Accesibilidad web que sí se nota",
            "Pequeños cambios con gran impacto: semántica, foco, contraste y pruebas con lectores de pantalla para que tu web funcione para todas las personas.",
            listOf("a11y", "HTML", "UX"),
        ),
        talk(
            "t8", "15:00", "15:45", "cloud", "Sala 3", "Intermedio",
            "Firebase + IA: apps que aprenden",
            "Integra modelos de IA en tus apps con Firebase: autenticación, datos en tiempo real y funciones que responden de forma inteligente.",
            listOf("Firebase", "IA", "Serverless"),
        ),
        talk(
            "t9", "16:00", "16:45", "general", "Auditorio", "Todos",
            "Panel: comunidades tech en el Perú",
            "Organizadores de comunidades conversan sobre cómo empezar, crecer y sostener una comunidad tecnológica en el país.",
            listOf("Panel", "Comunidad"),
        ),
        pause("b3", "17:00", "17:30", "Cierre y sorteos"),
    )

    fun agenda(empty: Boolean = false) = AgendaDto(
        updatedAt = "2026-11-20T18:00:00-05:00",
        tracks = tracks,
        sessions = if (empty) emptyList() else sessions,
    )

    val gemsInfo = GemsInfoDto(
        earnWays = listOf(
            EarnWayDto("talk", "Asistir a una charla", "Por cada charla a la que asistas", 20),
            EarnWayDto("stand", "Escanear el QR de un stand", "Por cada stand que visites", 40),
            EarnWayDto("challenge", "Completar retos", "Por cada reto que resuelvas", 80),
        ),
        prizes = listOf(
            PrizeDto("stickers", "Stickers", forPositions = "Puestos por anunciar"),
            PrizeDto("polos", "Polos", forPositions = "Puestos por anunciar"),
            PrizeDto("certificaciones", "Certificaciones", forPositions = "Puestos por anunciar"),
            PrizeDto("cursos", "Cursos", forPositions = "Puestos por anunciar"),
        ),
    )

    /** Saldo inicial del usuario falso (puesto 12 de 14). */
    const val INITIAL_BALANCE = 120

    /** Los 13 participantes del ranking falso, además del usuario. */
    val participants: List<Pair<String, Int>> = listOf(
        "Participante A" to 480,
        "Participante B" to 455,
        "Participante C" to 410,
        "Participante D" to 365,
        "Participante E" to 330,
        "Participante F" to 290,
        "Participante G" to 260,
        "Participante H" to 215,
        "Participante I" to 190,
        "Participante J" to 160,
        "Participante K" to 135,
        "Participante L" to 95,
        "Participante M" to 60,
    )

    /** Códigos QR de prueba (data-model.md, "Datos falsos"). */
    data class FakeQr(val source: ScanSourceDto, val gems: Int)

    const val QR_TALK = "DFL26-TALK-t5-7f3a"
    const val QR_STAND = "DFL26-STAND-s1-2b9c"
    const val QR_CHALLENGE = "DFL26-CHALLENGE-c1-9d41"

    val qrCodes: Map<String, FakeQr> = mapOf(
        QR_TALK to FakeQr(ScanSourceDto("talk", "Agentes con la Gemini API"), 20),
        QR_STAND to FakeQr(ScanSourceDto("stand", "Stand de la comunidad GDG"), 40),
        QR_CHALLENGE to FakeQr(ScanSourceDto("challenge", "Reto Kotlin"), 80),
    )
}
