package com.flowcast.demo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object FlowColors {
    val Ink = Color(0xFF111815)
    val InkMuted = Color(0xFF5D6863)
    val Page = Color(0xFFF5F8F6)
    val Card = Color(0xFFFFFFFF)
    val Brand = Color(0xFF0A7B67)
    val BrandDark = Color(0xFF064C42)
    val BrandSoft = Color(0xFFE4F4EF)
    val Mint = Color(0xFF69D6BD)
    val Gold = Color(0xFFE3B341)
    val Coral = Color(0xFFE96555)
    val Blue = Color(0xFF3A78D8)
    val VideoBlack = Color(0xFF090D0C)
    val VideoPanel = Color(0xFF111A17)
    val Success = Color(0xFF158A55)
    val Error = Color(0xFFD33A32)
    val Warning = Color(0xFFC8891D)
}

val BrandGradient = Brush.linearGradient(
    listOf(FlowColors.BrandDark, FlowColors.Brand, FlowColors.Mint),
)

@Composable
fun FlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FlowColors.Brand),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FlowChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    dark: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val container = when {
        selected && dark -> Color.White
        selected -> FlowColors.Brand
        dark -> Color.White.copy(alpha = 0.13f)
        else -> FlowColors.BrandSoft
    }
    val content = when {
        selected && dark -> FlowColors.Ink
        selected -> Color.White
        dark -> Color.White
        else -> FlowColors.Brand
    }
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(8.dp),
        color = container,
        border = if (dark && !selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)) else null,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun TechBlock(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val bg = if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFF0F4F2)
    val border = if (dark) Color.White.copy(alpha = 0.16f) else Color(0xFFE0E8E4)
    val titleColor = if (dark) Color(0xFFB8D8CF) else FlowColors.InkMuted
    val valueColor = if (dark) Color.White else FlowColors.Ink

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = titleColor, style = MaterialTheme.typography.labelMedium)
        Text(
            value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, color = FlowColors.InkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.26f)),
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "⊘",
            style = MaterialTheme.typography.displayLarge,
            color = FlowColors.InkMuted.copy(alpha = 0.6f),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = FlowColors.InkMuted,
        )
        if (onRetry != null) {
            FlowPrimaryButton(text = "重试", onClick = onRetry)
        }
    }
}

@Composable
fun LoadingState(
    message: String = "加载中...",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = FlowColors.Brand,
            strokeWidth = 3.dp,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = FlowColors.InkMuted,
        )
    }
}
