package pe.gdg.open.devfest.app.platform

/** Ruta absoluta de un archivo en el directorio de datos de la app (lo entrega cada sistema). */
fun interface DataStorePathProvider {
    fun path(fileName: String): String
}
