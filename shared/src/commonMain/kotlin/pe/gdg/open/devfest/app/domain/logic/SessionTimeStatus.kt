package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Session
import kotlin.time.Instant

/**
 * Una sesión es pasada cuando la hora del teléfono llega a su fin. Se compara como instante,
 * así que la zona horaria del teléfono no influye. "LIVE NOW" no depende de esto: es el flag
 * `Talk.isLive` del backend.
 */
fun isPast(session: Session, now: Instant): Boolean = now >= session.endsAt
