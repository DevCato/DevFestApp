package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Talk
import kotlin.time.Instant

enum class FeaturedKind {
    /** "EN CURSO AHORA": una charla guardada está en vivo según el backend. */
    IN_PROGRESS,

    /** "TU PRÓXIMA CHARLA": la siguiente que no ha empezado según la hora del teléfono. */
    NEXT,
}

data class FeaturedTalk(val talk: Talk, val kind: FeaturedKind)

/**
 * Tarjeta destacada de Mi agenda (FR-023), con [saved] ordenadas por inicio:
 * 1. La primera charla en vivo (flag del backend).
 * 2. Si no hay, la primera con `startsAt > now`.
 * 3. Si tampoco, ninguna.
 */
fun featuredTalk(saved: List<Talk>, now: Instant): FeaturedTalk? {
    saved.firstOrNull { it.isLive }?.let { return FeaturedTalk(it, FeaturedKind.IN_PROGRESS) }
    saved.firstOrNull { it.startsAt > now }?.let { return FeaturedTalk(it, FeaturedKind.NEXT) }
    return null
}
