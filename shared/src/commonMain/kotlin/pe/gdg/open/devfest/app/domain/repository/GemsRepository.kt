package pe.gdg.open.devfest.app.domain.repository

import pe.gdg.open.devfest.app.domain.model.GemsInfo
import pe.gdg.open.devfest.app.domain.model.ScanResult

interface GemsRepository {
    suspend fun info(): Outcome<GemsInfo>

    /**
     * Valida un QR. "Ya usado" y "no válido" son resultados, no fallos. En `Awarded` fija el
     * saldo con `newBalance` y refresca al usuario.
     */
    suspend fun scan(code: String): Outcome<ScanResult>
}
