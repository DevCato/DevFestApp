package pe.gdg.open.devfest.app.domain.logic

import pe.gdg.open.devfest.app.domain.model.Talk

/**
 * Charlas guardadas con "Cruce de horario" (FR-022): A y B se cruzan si
 * `A.startsAt < B.endsAt` y `B.startsAt < A.endsAt`. Charlas contiguas no se cruzan.
 */
fun conflictingTalkIds(talks: List<Talk>): Set<String> {
    val conflicts = mutableSetOf<String>()
    for (i in talks.indices) {
        for (j in i + 1 until talks.size) {
            val a = talks[i]
            val b = talks[j]
            if (a.startsAt < b.endsAt && b.startsAt < a.endsAt) {
                conflicts += a.id
                conflicts += b.id
            }
        }
    }
    return conflicts
}
