package pe.gdg.open.devfest.app.data.repository

import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.domain.repository.DataError
import pe.gdg.open.devfest.app.domain.repository.Outcome

/** Publica la sesión vencida para que la app muestre "Tu sesión expiró" (contracts/repositories.md). */
internal fun <T> Outcome<T>.reportSessionExpiry(events: SessionEvents): Outcome<T> {
    if (this is Outcome.Failure && error == DataError.SessionExpired) events.notifyExpired()
    return this
}

internal fun <T> Outcome<T>.asUnit(): Outcome<Unit> = when (this) {
    is Outcome.Success -> Outcome.Success(Unit)
    is Outcome.Failure -> this
}
