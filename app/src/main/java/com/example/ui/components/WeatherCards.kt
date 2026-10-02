package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherHeroCard(
    current: CurrentWeather,
    isFahrenheit: Boolean,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temp = if (isFahrenheit) current.tempF().roundToInt() else current.temperatureC.roundToInt()
    val feelsLike = if (isFahrenheit) current.feelsLikeF().roundToInt() else current.feelsLikeC.roundToInt()
    val unitSymbol = if (isFahrenheit) "°F" else "°C"

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_weather_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (current.isDay) current.weatherType.dayColor.copy(alpha = 0.2f)
                                else current.weatherType.nightColor.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = current.weatherType.icon,
                            contentDescription = current.weatherType.title,
                            tint = if (current.isDay) current.weatherType.dayColor else current.weatherType.nightColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = current.conditionDescription,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Feels like $feelsLike$unitSymbol • ${if (current.isDay) "Day" else "Night"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Temperature Unit Toggle
                OutlinedButton(
                    onClick = onToggleUnit,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("toggle_temp_unit_button")
                ) {
                    Text(
                        text = if (isFahrenheit) "°F" else "°C",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Temperature Display
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$temp",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 72.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unitSymbol,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Three key metrics in right stack
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Humidity ${current.humidityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFahrenheit) "${current.windSpeedMph().roundToInt()} mph" else "${current.windSpeedKmh.roundToInt()} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Precip ${current.precipitationMm} mm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastStrip(
    hourlyList: List<HourlyForecast>,
    isFahrenheit: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "Hourly Forecast (24h)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                hourlyList.forEachIndexed { index, forecast ->
                    val temp = if (isFahrenheit) {
                        ((forecast.tempC * 9 / 5) + 32).roundToInt()
                    } else {
                        forecast.tempC.roundToInt()
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (index == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .width(68.dp)
                            .testTag("hourly_item_$index")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = forecast.timeDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Icon(
                                imageVector = forecast.weatherType.icon,
                                contentDescription = null,
                                tint = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = "$temp°",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (index == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )

                            if (forecast.rainProbability > 0) {
                                Text(
                                    text = "${forecast.rainProbability}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastCard(
    dailyList: List<DailyForecast>,
    isFahrenheit: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "7-Day Outlook",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            dailyList.forEachIndexed { index, item ->
                val minT = if (isFahrenheit) ((item.minTempC * 9 / 5) + 32).roundToInt() else item.minTempC.roundToInt()
                val maxT = if (isFahrenheit) ((item.maxTempC * 9 / 5) + 32).roundToInt() else item.maxTempC.roundToInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.dayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.width(72.dp)
                    )

                    Row(
                        modifier = Modifier.width(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.weatherType.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (item.rainProbability > 10) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.rainProbability}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "$minT°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )

                    // Visual temperature bar
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "$maxT°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AtmosphericMetricsGrid(
    current: CurrentWeather,
    isFahrenheit: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Atmospheric Conditions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Wind & Direction",
                value = if (isFahrenheit) "${current.windSpeedMph().roundToInt()} mph" else "${current.windSpeedKmh.roundToInt()} km/h",
                subtitle = "Bearing: ${current.windDirectionDeg}°",
                icon = Icons.Default.Air,
                directionAngle = current.windDirectionDeg.toFloat(),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Humidity",
                value = "${current.humidityPercent}%",
                subtitle = if (current.humidityPercent > 65) "Humid" else "Comfortable",
                icon = Icons.Default.Opacity,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val uvDesc = when {
                current.uvIndex < 3 -> "Low"
                current.uvIndex < 6 -> "Moderate"
                current.uvIndex < 8 -> "Very High"
                else -> "Extreme"
            }

            MetricCard(
                title = "UV Index",
                value = String.format("%.1f", current.uvIndex),
                subtitle = uvDesc,
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Pressure",
                value = "${current.pressureHpa.roundToInt()} hPa",
                subtitle = "Stable atmosphere",
                icon = Icons.Default.Compress,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Sunrise",
                value = current.sunrise,
                subtitle = "Dawn",
                icon = Icons.Default.WbTwilight,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Sunset",
                value = current.sunset,
                subtitle = "Dusk",
                icon = Icons.Default.WbTwilight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    directionAngle: Float? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.testTag("metric_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (directionAngle != null) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(directionAngle)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
