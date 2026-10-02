package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeather
import com.example.data.model.DioramaStyle
import com.example.data.model.LocationItem
import com.example.data.model.MiniatureDioramaState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiniatureClusterBanner(
    dioramaState: MiniatureDioramaState,
    currentWeather: CurrentWeather,
    onRegenerateClicked: (DioramaStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStylePicker by remember { mutableStateOf(false) }
    var selectedStyle by remember(dioramaState.activeStyle) { mutableStateOf(dioramaState.activeStyle) }
    val horizontalScrollState = rememberScrollState()

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("miniature_cluster_card"),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "3D Miniature Landmark Cluster",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Horizontal Diorama • Gemini AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showStylePicker = !showStylePicker },
                        modifier = Modifier.testTag("toggle_diorama_style_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Change Diorama Style",
                            tint = if (showStylePicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onRegenerateClicked(selectedStyle) },
                        enabled = !dioramaState.isGenerating,
                        modifier = Modifier.testTag("regenerate_diorama_button")
                    ) {
                        if (dioramaState.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Diorama with Gemini",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Style Picker Row
            AnimatedVisibility(
                visible = showStylePicker,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DioramaStyle.values().forEach { style ->
                        val isSelected = style == selectedStyle
                        AssistChip(
                            onClick = {
                                selectedStyle = style
                                onRegenerateClicked(style)
                            },
                            label = { Text(style.title, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = if (isSelected) {
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                AssistChipDefaults.assistChipColors()
                            },
                            modifier = Modifier.testTag("chip_style_${style.name}")
                        )
                    }
                }
            }

            // Panoramic 3D Miniature Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Wide horizontal scrollable panoramic cluster image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    val clusterWidth = 620.dp

                    Box(modifier = Modifier.width(clusterWidth).height(230.dp)) {
                        when {
                            dioramaState.bitmap != null -> {
                                Image(
                                    bitmap = dioramaState.bitmap.asImageBitmap(),
                                    contentDescription = "Gemini generated 3D miniature cluster of ${dioramaState.location.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            dioramaState.fallbackDrawableRes != null -> {
                                Image(
                                    painter = painterResource(id = dioramaState.fallbackDrawableRes),
                                    contentDescription = "3D miniature cluster of ${dioramaState.location.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                // Default procedural miniature diorama background
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.img_paris_cluster_1789451529911),
                                    contentDescription = "3D miniature cluster placeholder",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Weather particle effects directly over the 3D miniature cluster
                        WeatherEffectsOverlay(
                            weatherType = currentWeather.weatherType,
                            isDay = currentWeather.isDay,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Tilt-shift depth-of-field overlay: top and bottom soft gradient vignette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                // Scroll hint indicator chip on top right
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "↔ Pan Cluster",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Bottom Overlay Bar: Location & Landmarks in Miniature
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = dioramaState.location.displayName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dioramaState.activeStyle.title + " Render",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Weather pill tag
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = currentWeather.weatherType.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentWeather.temperatureC.toInt()}°C",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Loading overlay when Gemini is generating
                if (dioramaState.isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Gemini is generating 3D miniature cluster...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dioramaState.location.name + " (" + dioramaState.activeStyle.title + ")",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Landmarks chips row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Micro-Sculpted Landmarks in Cluster:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dioramaState.location.iconicLandmarks.forEach { landmark ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = landmark,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // AI Meteorological Narrative
                if (dioramaState.aiCommentary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dioramaState.aiCommentary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // If error or api key warning
                if (dioramaState.errorMessage != null && !dioramaState.isGenerating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dioramaState.errorMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
