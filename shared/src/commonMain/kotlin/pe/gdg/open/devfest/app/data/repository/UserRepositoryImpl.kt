package pe.gdg.open.devfest.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.gdg.open.devfest.app.data.SessionEvents
import pe.gdg.open.devfest.app.data.api.DevFestApi
import pe.gdg.open.devfest.app.data.api.safeApiCall
import pe.gdg.open.devfest.app.data.mapper.toDomain
import pe.gdg.open.devfest.app.domain.model.User
import pe.gdg.open.devfest.app.domain.repository.Outcome
import pe.gdg.open.devfest.app.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: DevFestApi,
    private val sessionEvents: SessionEvents,
) : UserRepository {

    private val _user = MutableStateFlow<User?>(null)
    override val user: StateFlow<User?> = _user.asStateFlow()

    /** Un fallo conserva el último usuario conocido. */
    override suspend fun refresh(): Outcome<Unit> {
        val result = safeApiCall { api.getMe().toDomain() }.reportSessionExpiry(sessionEvents)
        if (result is Outcome.Success) _user.value = result.value
        return result.asUnit()
    }

    override fun setBalance(gems: Int) {
        _user.update { it?.copy(gems = gems) }
    }
}
