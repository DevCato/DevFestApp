package pe.gdg.open.devfest.app.domain.model

enum class AuthProvider { GOOGLE, APPLE, GITHUB }

/** Identidad que entrega el proveedor tras iniciar sesión. */
data class AuthSession(
    val userId: String,
    val fullName: String,
    val photoUrl: String?,
    val provider: AuthProvider,
)

/** Usuario con saldo y posición, tal como lo devuelve el backend. */
data class User(
    val id: String,
    val fullName: String,
    val photoUrl: String?,
    val provider: AuthProvider,
    val gems: Int,
    val rank: Int,
    val totalParticipants: Int,
)
