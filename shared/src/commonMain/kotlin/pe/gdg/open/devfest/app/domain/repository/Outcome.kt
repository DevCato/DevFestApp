package pe.gdg.open.devfest.app.domain.repository

/** Resultado de una operación de datos que puede fallar. */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: DataError) : Outcome<Nothing>
}

/** Causa de un fallo, tal como la necesita la UI (ver data-model.md). */
enum class DataError {
    /** Fallo de transporte: "Sin conexión" / "No se pudo actualizar". */
    Offline,

    /** El backend rechazó la sesión (401): modal "Tu sesión expiró" y vuelta a Login. */
    SessionExpired,

    /** Cualquier otro error: "No se pudo actualizar". */
    Unknown,
}
