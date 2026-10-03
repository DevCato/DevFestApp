package pe.gdg.open.devfest.app.ui.components

import org.jetbrains.compose.resources.DrawableResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.ic_alert
import pe.gdg.open.devfest.app.resources.ic_back
import pe.gdg.open.devfest.app.resources.ic_bookmark
import pe.gdg.open.devfest.app.resources.ic_bookmark_fill
import pe.gdg.open.devfest.app.resources.ic_calendar
import pe.gdg.open.devfest.app.resources.ic_camera
import pe.gdg.open.devfest.app.resources.ic_check_circle
import pe.gdg.open.devfest.app.resources.ic_chevron
import pe.gdg.open.devfest.app.resources.ic_clock
import pe.gdg.open.devfest.app.resources.ic_close
import pe.gdg.open.devfest.app.resources.ic_flag
import pe.gdg.open.devfest.app.resources.ic_gem
import pe.gdg.open.devfest.app.resources.ic_gem_filled
import pe.gdg.open.devfest.app.resources.ic_lock
import pe.gdg.open.devfest.app.resources.ic_logout
import pe.gdg.open.devfest.app.resources.ic_prize_course
import pe.gdg.open.devfest.app.resources.ic_prize_medal
import pe.gdg.open.devfest.app.resources.ic_prize_shirt
import pe.gdg.open.devfest.app.resources.ic_prize_stickers
import pe.gdg.open.devfest.app.resources.ic_qr
import pe.gdg.open.devfest.app.resources.ic_qr_invalid
import pe.gdg.open.devfest.app.resources.ic_refresh
import pe.gdg.open.devfest.app.resources.ic_trophy
import pe.gdg.open.devfest.app.resources.ic_wifi_off
import pe.gdg.open.devfest.app.resources.logo_apple
import pe.gdg.open.devfest.app.resources.logo_github
import pe.gdg.open.devfest.app.resources.logo_google

/**
 * Íconos de trazo del prototipo (24×24). Se tiñen con `Icon(tint = …)`.
 * Los de varios colores (gema rellena, logos) van con `Image`.
 */
object AppIcons {
    val Back: DrawableResource = Res.drawable.ic_back
    val Close: DrawableResource = Res.drawable.ic_close
    val Bookmark: DrawableResource = Res.drawable.ic_bookmark
    /** Solo el relleno del marcador: se tiñe con el color del track bajo [Bookmark]. */
    val BookmarkFill: DrawableResource = Res.drawable.ic_bookmark_fill
    val Gem: DrawableResource = Res.drawable.ic_gem
    val GemFilled: DrawableResource = Res.drawable.ic_gem_filled
    val Refresh: DrawableResource = Res.drawable.ic_refresh
    val WifiOff: DrawableResource = Res.drawable.ic_wifi_off
    val Camera: DrawableResource = Res.drawable.ic_camera
    val Lock: DrawableResource = Res.drawable.ic_lock
    val Clock: DrawableResource = Res.drawable.ic_clock
    val CheckCircle: DrawableResource = Res.drawable.ic_check_circle
    val QrInvalid: DrawableResource = Res.drawable.ic_qr_invalid
    val Qr: DrawableResource = Res.drawable.ic_qr
    val Logout: DrawableResource = Res.drawable.ic_logout
    val Calendar: DrawableResource = Res.drawable.ic_calendar
    val Alert: DrawableResource = Res.drawable.ic_alert
    val Chevron: DrawableResource = Res.drawable.ic_chevron
    val Trophy: DrawableResource = Res.drawable.ic_trophy
    val Flag: DrawableResource = Res.drawable.ic_flag

    val PrizeStickers: DrawableResource = Res.drawable.ic_prize_stickers
    val PrizeShirt: DrawableResource = Res.drawable.ic_prize_shirt
    val PrizeMedal: DrawableResource = Res.drawable.ic_prize_medal
    val PrizeCourse: DrawableResource = Res.drawable.ic_prize_course

    val LogoGoogle: DrawableResource = Res.drawable.logo_google
    val LogoApple: DrawableResource = Res.drawable.logo_apple
    val LogoGithub: DrawableResource = Res.drawable.logo_github
}
