package pe.gdg.open.devfest.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** Almacenamiento clave-valor del dispositivo. */
interface KeyValueStore {
    fun observeString(key: String): Flow<String?>
    suspend fun putString(key: String, value: String)
    fun observeStringSet(key: String): Flow<Set<String>>
    suspend fun putStringSet(key: String, value: Set<String>)
    suspend fun remove(key: String)
}

/** Implementación con DataStore Preferences (research.md, R5). */
class DataStoreKeyValueStore(private val dataStore: DataStore<Preferences>) : KeyValueStore {

    // Un archivo ilegible se trata como vacío: la app sigue funcionando y lo reescribe.
    private val data: Flow<Preferences> = dataStore.data.catch { emit(emptyPreferences()) }

    override fun observeString(key: String): Flow<String?> =
        data.map { it[stringPreferencesKey(key)] }.distinctUntilChanged()

    override suspend fun putString(key: String, value: String) {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    override fun observeStringSet(key: String): Flow<Set<String>> =
        data.map { it[stringSetPreferencesKey(key)].orEmpty() }.distinctUntilChanged()

    override suspend fun putStringSet(key: String, value: Set<String>) {
        dataStore.edit { it[stringSetPreferencesKey(key)] = value }
    }

    override suspend fun remove(key: String) {
        dataStore.edit {
            it.remove(stringPreferencesKey(key))
            it.remove(stringSetPreferencesKey(key))
        }
    }
}

const val DATASTORE_FILE_NAME = "devfest.preferences_pb"

fun createDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })
