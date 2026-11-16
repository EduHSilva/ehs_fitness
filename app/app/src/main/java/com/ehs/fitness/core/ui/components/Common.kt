package com.ehs.fitness.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehs.fitness.core.ui.theme.FitnessTheme

@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FitnessTheme.colors.border),
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun CardTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        action?.invoke()
    }
}

@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text, color = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Tentar novamente",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FitnessTheme.colors.dangerSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            color = FitnessTheme.colors.danger,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = FitnessTheme.colors.danger,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(retryLabel, color = FitnessTheme.colors.danger, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun Modifier.dashedRoundedBorder(color: Color, radius: Dp = 8.dp, strokeWidth: Dp = 1.dp): Modifier =
    drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(radius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
            ),
        )
    }

@Composable
fun EmptyNote(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .dashedRoundedBorder(FitnessTheme.colors.borderStrong)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    background: Color = FitnessTheme.colors.primarySoft,
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LoadingScreen(text: String = "Carregando...") {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun Subtitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Campo de seleção simples: botão com o valor atual e diálogo com as opções. */
@Composable
fun PickerField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var open by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        OutlinedButton(
            onClick = { open = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                value,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
            )
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                Column {
                    options.forEachIndexed { index, option ->
                        TextButton(
                            onClick = {
                                onSelect(index)
                                open = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
fun ClickableRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Faixa de abertura das páginas internas, com o gradiente da marca. */
@Composable
fun IntroBanner(
    eyebrow: String,
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF4F46E5), Color(0xFF6D28D9))),
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE0E7FF),
                )
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEDE9FE))
            }
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

/** Estado vazio/erro de uma listagem, com ação opcional. */
@Composable
fun PageState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    isError: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dashedRoundedBorder(if (isError) FitnessTheme.colors.danger else FitnessTheme.colors.borderStrong, radius = 14.dp)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isError) FitnessTheme.colors.danger else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isError) FitnessTheme.colors.danger else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(2.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(8.dp)) { Text(actionLabel) }
        }
    }
}

@Composable
fun InitialAvatar(name: String, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    val initials = remember(name) {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts.first().take(1).uppercase()
            else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = if (size >= 56.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    dismissLabel: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) FitnessTheme.colors.danger else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
    )
}

/** Botão principal com indicador de progresso embutido. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(9.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

/** Barra fina de progresso usada nos resumos. */
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val track = FitnessTheme.colors.border
    Canvas(
        modifier
            .fillMaxWidth()
            .height(6.dp),
    ) {
        val radius = size.height / 2f
        drawRoundRect(color = track, cornerRadius = CornerRadius(radius), topLeft = Offset.Zero)
        drawRoundRect(
            color = color,
            size = androidx.compose.ui.geometry.Size(size.width * fraction.coerceIn(0f, 1f), size.height),
            cornerRadius = CornerRadius(radius),
            topLeft = Offset.Zero,
            style = androidx.compose.ui.graphics.drawscope.Fill,
        )
    }
}

@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        action?.invoke()
    }
}

@Composable
fun ScreenDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.fillMaxWidth(), color = FitnessTheme.colors.border)
}

/** Círculo decorativo usado nos cartões de destaque. */
@Composable
fun DecorCircle(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .background(color, CircleShape),
    )
}

@Composable
fun FullScreenMessage(title: String, description: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Pequeno marcador circular (ex.: dia da semana planejado). */
@Composable
fun Dot(active: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier
            .size(8.dp)
            .background(if (active) color else FitnessTheme.colors.border, CircleShape),
    )
}

@Suppress("unused")
private val keepStrokeCap = StrokeCap.Round
