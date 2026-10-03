package pe.gdg.open.devfest.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class Event(
    val id: String,
    val name: String,
    val edition: String,
    val date: LocalDate,
    val city: String,
    val timeZone: TimeZone,
    val hashtag: String,
)
