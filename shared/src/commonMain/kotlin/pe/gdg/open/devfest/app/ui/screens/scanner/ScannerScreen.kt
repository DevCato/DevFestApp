package pe.gdg.open.devfest.app.ui.screens.scanner

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pe.gdg.open.devfest.app.platform.QrScannerView
import pe.gdg.open.devfest.app.platform.rememberCameraPermissionRequester
import pe.gdg.open.devfest.app.platform.rememberOpenAppSettings
import pe.gdg.open.devfest.app.resources.Res
import pe.gdg.open.devfest.app.resources.cd_close_scanner
import pe.gdg.open.devfest.app.resources.scanner_hint
import pe.gdg.open.devfest.app.resources.scanner_title
import pe.gdg.open.devfest.app.resources.scanner_validating
import pe.gdg.open.devfest.app.ui.components.AppIcons
import pe.gdg.open.devfest.app.ui.modal.ModalRequestEffect
import pe.gdg.open.devfest.app.ui.theme.DevFestColors
import pe.gdg.open.devfest.app.ui.theme.EaseInOutStrong
import pe.gdg.open.devfest.app.ui.theme.LocalReduceMotion

private val ScannerBackground = Color(0xFF141414)
private val ViewfinderBackground = Color(0xFF222222)
private val HintColor = Color(0xFFD0D0D0)
private val ViewfinderSize = 250.dp

/** Escáner a pantalla completa (US5, escenario 1). */
@Composable
fun ScannerScreen(
    onClose: () -> Unit,
    viewModel: ScannerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openSettings = rememberOpenAppSettings()
    ModalRequestEffect(viewModel.modals)

    var hasPermission by remember { mutableStateOf(false) }
    val permission = rememberCameraPermissionRequester { granted ->
        hasPermission = granted
        if (!granted) viewModel.onPermissionDenied()
    }
    LaunchedEffect(Unit) {
        hasPermission = permission.isGranted()
        if (!hasPermission) permission.request()
    }
    // Al volver de los ajustes del sistema, la cámara se activa si el usuario dio permiso.
    LifecycleResumeEffect(permission) {
        hasPermission = permission.isGranted()
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ScannerEvent.Close -> onClose()
                ScannerEvent.OpenSettings -> openSettings()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val description = stringResource(Res.string.cd_close_scanner)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, DevFestColors.Card, CircleShape)
                    .clickable(role = Role.Button, onClick = onClose)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(AppIcons.Close), contentDescription = null, tint = DevFestColors.Card, modifier = Modifier.size(20.dp))
            }
            Text(
                text = stringResource(Res.string.scanner_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DevFestColors.Card,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            Box(Modifier.size(46.dp))
        }

        Viewfinder(
            showCamera = hasPermission,
            validating = state.validating,
            onCodeScanned = viewModel::onCodeScanned,
            modifier = Modifier.padding(top = 12.dp),
        )

        Text(
            text = stringResource(if (state.validating) Res.string.scanner_validating else Res.string.scanner_hint),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = HintColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp),
        )
    }
}

@Composable
private fun Viewfinder(
    showCamera: Boolean,
    validating: Boolean,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(ViewfinderSize)) {
        Box(
            Modifier
                .matchParentSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(ViewfinderBackground),
        ) {
            if (showCamera) QrScannerView(onCodeScanned = onCodeScanned, modifier = Modifier.matchParentSize())
        }
        CornerBrackets(Modifier.matchParentSize())
        if (showCamera && !validating) ScanLine()
    }
}

/** Esquinas celestes del visor. */
@Composable
private fun CornerBrackets(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = 5.dp.toPx()
        val arm = 46.dp.toPx()
        val r = 26.dp.toPx()
        val i = stroke / 2
        val w = size.width
        val h = size.height
        val corners = Path().apply {
            // Arriba a la izquierda.
            moveTo(i, i + arm); lineTo(i, i + r); quadraticTo(i, i, i + r, i); lineTo(i + arm, i)
            // Arriba a la derecha.
            moveTo(w - i - arm, i); lineTo(w - i - r, i); quadraticTo(w - i, i, w - i, i + r); lineTo(w - i, i + arm)
            // Abajo a la derecha.
            moveTo(w - i, h - i - arm); lineTo(w - i, h - i - r); quadraticTo(w - i, h - i, w - i - r, h - i); lineTo(w - i - arm, h - i)
            // Abajo a la izquierda.
            moveTo(i + arm, h - i); lineTo(i + r, h - i); quadraticTo(i, h - i, i, h - i - r); lineTo(i, h - i - arm)
        }
        drawPath(corners, DevFestColors.Sky, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/** Línea que recorre el visor; con "reducir movimiento" queda quieta arriba. */
@Composable
private fun ScanLine() {
    val reduceMotion = LocalReduceMotion.current
    val progress = if (reduceMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "scan").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutStrong), RepeatMode.Reverse),
            label = "line",
        )
    }
    Box(
        Modifier
            .padding(start = 20.dp, end = 20.dp, top = 18.dp)
            .fillMaxWidth()
            .height(3.dp)
            .graphicsLayer { translationY = (progress?.value ?: 0f) * 212.dp.toPx() }
            .clip(RoundedCornerShape(3.dp))
            .background(DevFestColors.Sky),
    )
}
