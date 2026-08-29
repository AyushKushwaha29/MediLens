package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusAttentionBg
import com.example.ui.theme.StatusAttentionText
import com.example.ui.theme.StatusLowBg
import com.example.ui.theme.StatusLowText
import com.example.ui.theme.StatusNormalBg
import com.example.ui.theme.StatusNormalText
import com.example.ui.theme.StatusUnknownBg
import com.example.ui.theme.StatusUnknownGray

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (status.uppercase()) {
        "NORMAL" -> Quadruple(
            StatusNormalBg,
            StatusNormalText,
            Icons.Default.CheckCircle,
            "Normal"
        )
        "HIGH" -> Quadruple(
            StatusAttentionBg,
            StatusAttentionText,
            Icons.Default.ArrowUpward,
            "Attention"
        )
        "LOW" -> Quadruple(
            StatusLowBg,
            StatusLowText,
            Icons.Default.ArrowDownward,
            "Low"
        )
        else -> Quadruple(
            StatusUnknownBg,
            StatusUnknownGray,
            Icons.Default.HelpOutline,
            "Unknown"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .testTag("status_badge_${status.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

