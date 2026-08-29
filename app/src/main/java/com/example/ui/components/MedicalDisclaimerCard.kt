package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalNavyPrimary
import com.example.ui.theme.MinimalOutlineLight
import com.example.ui.theme.MinimalSurfaceVariantLight
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medical_disclaimer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalSurfaceVariantLight
        ),
        border = BorderStroke(1.dp, MinimalOutlineLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 10.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Medical Disclaimer",
                    tint = MinimalNavyPrimary,
                    modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Educational Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MinimalNavyPrimary,
                        fontSize = if (compact) 10.sp else 11.sp
                    )
                )
            }
            Text(
                text = "MediLens provides educational information only. It is not a diagnosis or medical advice and does not replace evaluation by a healthcare professional.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MinimalTextTertiary,
                    fontSize = if (compact) 9.sp else 10.sp,
                    lineHeight = if (compact) 13.sp else 15.sp,
                    fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

