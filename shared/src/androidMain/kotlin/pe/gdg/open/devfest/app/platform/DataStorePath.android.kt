package pe.gdg.open.devfest.app.platform

import android.content.Context

class AndroidDataStorePathProvider(private val context: Context) : DataStorePathProvider {
    override fun path(fileName: String): String =
        context.filesDir.resolve(fileName).absolutePath
}
