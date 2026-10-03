package pe.gdg.open.devfest.app.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [KeyValueStore] en memoria para tests. */
class InMemoryKeyValueStore : KeyValueStore {
    private val values = MutableStateFlow<Map<String, Any>>(emptyMap())

    override fun observeString(key: String): Flow<String?> =
        values.map { it[key] as? String }.distinctUntilChanged()

    override suspend fun putString(key: String, value: String) {
        values.update { it + (key to value) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun observeStringSet(key: String): Flow<Set<String>> =
        values.map { (it[key] as? Set<String>).orEmpty() }.distinctUntilChanged()

    override suspend fun putStringSet(key: String, value: Set<String>) {
        values.update { it + (key to value) }
    }

    override suspend fun remove(key: String) {
        values.update { it - key }
    }
}
