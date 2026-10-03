package pe.gdg.open.devfest.app.domain.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Las horas de la agenda se muestran siempre en hora de Lima (spec, Edge Cases). */
val LimaTimeZone: TimeZone = TimeZone.of("America/Lima")

/** Hora en Lima, formato 24 h: `09:30`. */
fun formatHour(instant: Instant, timeZone: TimeZone = LimaTimeZone): String {
    val time = instant.toLocalDateTime(timeZone).time
    return "${time.hour.twoDigits()}:${time.minute.twoDigits()}"
}

/** Duración en minutos enteros entre dos instantes. */
fun durationMinutes(start: Instant, end: Instant): Int = (end - start).inWholeMinutes.toInt()

private val shortDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val shortMonths = listOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
)

/** Fecha corta en español: `Sáb 21 nov`. */
fun formatShortDate(date: LocalDate): String =
    "${shortDays[date.dayOfWeek.ordinal]} ${date.day} ${shortMonths[date.month.ordinal]}"

private fun Int.twoDigits(): String = toString().padStart(2, '0')
