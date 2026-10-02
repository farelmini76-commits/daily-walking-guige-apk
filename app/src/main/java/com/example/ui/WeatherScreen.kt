package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.WeatherType
import com.example.ui.components.AtmosphericMetricsGrid
import com.example.ui.components.CurrentWeatherHeroCard
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.HourlyForecastStrip
import com.example.ui.components.LocationSearchHeader
import com.example.ui.components.MiniatureClusterBanner
import com.example.ui.components.WeatherEffectsOverlay

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val weather = uiState.weatherBundle?.current

    // Animated sky background gradient based on current weather
    val weatherType = weather?.weatherType ?: WeatherType.CLEAR
    val skyColors = weatherType.skyGradient

    val topColor by animateColorAsState(
        targetValue = if (weather?.isDay == false) Color(0xFF0F172A) else skyColors.first(),
        animationSpec = tween(1000),
        label = "skyTop"
    )
    val midColor by animateColorAsState(
        targetValue = if (weather?.isDay == false) Color(0xFF1E1B4B) else skyColors.getOrElse(1) { skyColors.first() },
        animationSpec = tween(1000),
        label = "skyMid"
    )
    val bottomColor by animateColorAsState(
        targetValue = if (weather?.isDay == false) Color(0xFF0B0F19) else skyColors.last(),
        animationSpec = tween(1000),
        label = "skyBottom"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(topColor, midColor, bottomColor)
    )

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_screen_root"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refreshWeather() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(bottom = navBarPadding)
                    .testTag("fab_refresh_weather")
            ) {
                if (uiState.isLoadingWeather || uiState.dioramaState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh weather and diorama"
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // Ambient weather effects overlay across the scene
            WeatherEffectsOverlay(
                weatherType = weatherType,
                isDay = weather?.isDay ?: true,
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("weather_content_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top spacing for status bar
                item {
                    Spacer(modifier = Modifier.height(statusBarPadding + 8.dp))
                }

                // Search & Quick Selection Header
                item {
                    LocationSearchHeader(
                        currentLocation = uiState.location,
                        presetLocations = viewModel.presetLocations,
                        searchResults = uiState.searchResults,
                        isSearching = uiState.isSearching,
                        onSearchQueryChange = { viewModel.searchLocations(it) },
                        onLocationSelected = { viewModel.selectLocation(it) }
                    )
                }

                // 3D Miniature Landmark Cluster Banner (Gemini generated background diorama)
                item {
                    if (weather != null) {
                        MiniatureClusterBanner(
                            dioramaState = uiState.dioramaState,
                            currentWeather = weather,
                            onRegenerateClicked = { style ->
                                viewModel.regenerateDioramaWithStyle(style)
                            }
                        )
                    } else {
                        // Skeleton placeholder while loading initial weather
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                // Current Weather Card
                item {
                    if (weather != null) {
                        CurrentWeatherHeroCard(
                            current = weather,
                            isFahrenheit = uiState.isFahrenheit,
                            onToggleUnit = { viewModel.toggleTemperatureUnit() }
                        )
                    }
                }

                // Hourly Forecast
                item {
                    val hourly = uiState.weatherBundle?.hourly
                    if (!hourly.isNullOrEmpty()) {
                        HourlyForecastStrip(
                            hourlyList = hourly,
                            isFahrenheit = uiState.isFahrenheit
                        )
                    }
                }

                // 7-Day Forecast
                item {
                    val daily = uiState.weatherBundle?.daily
                    if (!daily.isNullOrEmpty()) {
                        DailyForecastCard(
                            dailyList = daily,
                            isFahrenheit = uiState.isFahrenheit
                        )
                    }
                }

                // Atmospheric Conditions
                item {
                    if (weather != null) {
                        AtmosphericMetricsGrid(
                            current = weather,
                            isFahrenheit = uiState.isFahrenheit
                        )
                    }
                }

                // Gemini API Key Notice (Informational for prototype/production guidance)
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("gemini_info_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isApiKeyConfigured) Icons.Default.AutoAwesome else Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (uiState.isApiKeyConfigured) "Gemini 3D Engine Active" else "Gemini AI Miniature Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.isApiKeyConfigured)
                                        "Connected to gemini-2.5-flash-image and gemini-3.5-flash for real-time landmark dioramas."
                                    else
                                        "Using pre-rendered 3D miniature clusters. Configure GEMINI_API_KEY in the Secrets panel for infinite custom city generation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Bottom navigation bar clearance
                item {
                    Spacer(modifier = Modifier.height(navBarPadding + 64.dp))
                }
            }
        }
    }
}
