package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/**
 * Foto circular con borde de tinta. Sin foto (o mientras carga, o si falla) muestra la inicial
 * del nombre.
 *
 * @param contentDescription descripción para lectores de pantalla; `null` si es decorativa.
 */
@Composable
fun Avatar(
    name: String,
    photoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    background: Color = DevFestColors.SkyTint,
    borderWidth: Dp = DevFestDimens.BorderThin,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
    } else {
        Modifier.clearAndSetSemantics { }
    }
    // Tamaño fijo en relación al círculo: la inicial no crece con la letra del sistema.
    val initialSize = with(LocalDensity.current) { (size * 0.42f).toSp() }

    Box(
        modifier = modifier
            .then(semanticsModifier)
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(borderWidth, DevFestColors.Ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialOf(name),
            fontFamily = LocalDevFestFonts.current.outfit,
            fontWeight = FontWeight.ExtraBold,
            fontSize = initialSize,
            color = DevFestColors.Ink,
        )
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().clip(CircleShape),
            )
        }
    }
}

/** Primera letra del nombre en mayúscula; `?` si el nombre está vacío. */
fun initialOf(name: String): String = name.trim().firstOrNull()?.uppercase() ?: "?"
