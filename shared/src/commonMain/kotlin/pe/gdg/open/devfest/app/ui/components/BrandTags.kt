package pe.gdg.open.devfest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.brand_community_edition
import pe.gdg.open.devfest.app.resources.brand_year
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.DevFestDimens
import pe.gdg.open.devfest.app.ui.theme.LocalDevFestFonts

/** Etiquetas "Community Edition" y "2026" (Login, Agenda). */
sealed class BrandTagsSize {
    object Normal : BrandTagsSize()
    object Small : BrandTagsSize()
}

@Composable
fun BrandTags(modifier: Modifier = Modifier, brandTagsSize: BrandTagsSize = BrandTagsSize.Normal) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        OutlinedPill(
            text = stringResource(Res.string.brand_community_edition),
            background = DevFestColors.Card,
            fontWeight = FontWeight.SemiBold,
            brandTagsSize = brandTagsSize
        )
        OutlinedPill(
            text = stringResource(Res.string.brand_year),
            background = DevFestColors.SkyTint,
            fontWeight = FontWeight.Bold,
            brandTagsSize = brandTagsSize
        )
    }
}

/** Píldora con borde de tinta, 32 dp de alto. */
@Composable
fun OutlinedPill(
    text: String,
    background: Color,
    fontWeight: FontWeight,
    brandTagsSize: BrandTagsSize,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    val textSize = if (brandTagsSize == BrandTagsSize.Normal) 15.sp else 10.sp
    Box(
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(shape)
            .background(background)
            .border(DevFestDimens.BorderThin, DevFestColors.Ink, shape)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = LocalDevFestFonts.current.outfit,
            fontSize = textSize,
            fontWeight = fontWeight,
            color = DevFestColors.Ink,
        )
    }
}
