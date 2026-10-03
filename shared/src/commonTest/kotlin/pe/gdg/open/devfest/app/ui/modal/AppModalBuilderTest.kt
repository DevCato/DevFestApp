package pe.gdg.open.devfest.app.ui.modal

import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppModalBuilderTest {

    @Test
    fun buildFailsWithoutTitle() {
        assertFailsWith<IllegalArgumentException> {
            AppModal.Builder().primaryButton("Listo") {}.build()
        }
    }

    @Test
    fun buildFailsWithBlankTitle() {
        assertFailsWith<IllegalArgumentException> {
            AppModal.Builder().title("  ").primaryButton("Listo") {}.build()
        }
    }

    @Test
    fun buildFailsWithoutPrimaryButton() {
        assertFailsWith<IllegalArgumentException> {
            AppModal.Builder().title("Sin conexión").build()
        }
    }

    @Test
    fun oneButtonModal() {
        val modal = AppModal.Builder()
            .title("Ya registraste este código")
            .primaryButton("Entendido") {}
            .build()

        assertEquals("Entendido", modal.primaryButton.text)
        assertNull(modal.secondaryButton)
    }

    @Test
    fun twoButtonModalRunsEachAction() {
        var retried = false
        var cancelled = false
        val modal = AppModal.Builder()
            .title("Este QR no es del DevFest")
            .primaryButton("Intentar de nuevo") { retried = true }
            .secondaryButton("Cancelar") { cancelled = true }
            .build()

        modal.primaryButton.onClick()
        assertNotNull(modal.secondaryButton).onClick()

        assertTrue(retried)
        assertTrue(cancelled)
    }

    @Test
    fun defaults() {
        val modal = AppModal.Builder().title("Título").primaryButton("OK") {}.build()

        assertNull(modal.message)
        assertNull(modal.icon)
        assertEquals(DevFestColors.Ink, modal.shadowColor)
        assertEquals(DevFestColors.YellowTint, modal.iconBackground)
        assertEquals(DevFestColors.Ink, modal.iconTint)
        assertTrue(modal.dismissOnOutsideTap)
    }

    @Test
    fun allParametersAreKept() {
        val modal = AppModal.Builder()
            .title("No se pudo actualizar")
            .message("Revisa tu conexión.")
            .iconBackground(DevFestColors.PurpleTint)
            .iconTint(DevFestColors.Ink)
            .shadowColor(DevFestColors.Purple)
            .primaryButton("Intentar de nuevo") {}
            .secondaryButton("Cerrar")
            .dismissOnOutsideTap(false)
            .build()

        assertEquals("Revisa tu conexión.", modal.message)
        assertEquals(DevFestColors.PurpleTint, modal.iconBackground)
        assertEquals(DevFestColors.Purple, modal.shadowColor)
        assertEquals("Cerrar", modal.secondaryButton?.text)
        assertEquals(false, modal.dismissOnOutsideTap)
    }

    @Test
    fun inkShadowUsesSkyShadowOnPrimaryButton() {
        val ink = AppModal.Builder().title("T").shadowColor(DevFestColors.Ink).primaryButton("OK") {}.build()
        val yellow = AppModal.Builder().title("T").shadowColor(DevFestColors.Yellow).primaryButton("OK") {}.build()

        assertEquals(DevFestColors.Sky, ink.primaryButtonShadowColor)
        assertEquals(DevFestColors.Yellow, yellow.primaryButtonShadowColor)
    }
}
