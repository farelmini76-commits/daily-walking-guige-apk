package com.example.data.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Grain
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categorized weather condition with presentation metadata
 */
enum class WeatherType(
    val title: String,
    val icon: ImageVector,
    val dayColor: Color,
    val nightColor: Color,
    val skyGradient: List<Color>
) {
    CLEAR(
        title = "Clear Sky",
        icon = Icons.Default.WbSunny,
        dayColor = Color(0xFFFFB300),
        nightColor = Color(0xFF90CAF9),
        skyGradient = listOf(Color(0xFF1E88E5), Color(0xFF64B5F6), Color(0xFFBBDEFB))
    ),
    PARTLY_CLOUDY(
        title = "Partly Cloudy",
        icon = Icons.Default.Cloud,
        dayColor = Color(0xFFFFCA28),
        nightColor = Color(0xFF78909C),
        skyGradient = listOf(Color(0xFF2E78B8), Color(0xFF5B9BD5), Color(0xFFADCDE8))
    ),
    OVERCAST(
        title = "Overcast",
        icon = Icons.Default.Cloud,
        dayColor = Color(0xFF9E9E9E),
        nightColor = Color(0xFF546E7A),
        skyGradient = listOf(Color(0xFF455A64), Color(0xFF607D8B), Color(0xFF90A4AE))
    ),
    RAIN(
        title = "Rain Showers",
        icon = Icons.Default.WaterDrop,
        dayColor = Color(0xFF29B6F6),
        nightColor = Color(0xFF0288D1),
        skyGradient = listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64))
    ),
    DRIZZLE(
        title = "Drizzle",
        icon = Icons.Default.Grain,
        dayColor = Color(0xFF4FC3F7),
        nightColor = Color(0xFF37474F),
        skyGradient = listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF78909C))
    ),
    THUNDERSTORM(
        title = "Thunderstorm",
        icon = Icons.Default.Thunderstorm,
        dayColor = Color(0xFFFFD54F),
        nightColor = Color(0xFF512DA8),
        skyGradient = listOf(Color(0xFF1A1A2E), Color(0xFF283593), Color(0xFF3F51B5))
    ),
    SNOW(
        title = "Snow",
        icon = Icons.Default.AcUnit,
        dayColor = Color(0xFFE1F5FE),
        nightColor = Color(0xFFB0BEC5),
        skyGradient = listOf(Color(0xFF4A6572), Color(0xFF78909C), Color(0xFFCFD8DC))
    ),
    FOG(
        title = "Foggy & Mist",
        icon = Icons.Default.Air,
        dayColor = Color(0xFFB0BEC5),
        nightColor = Color(0xFF455A64),
        skyGradient = listOf(Color(0xFF546E7A), Color(0xFF78909C), Color(0xFFB0BEC5))
    );

    companion object {
        fun fromWmoCode(code: Int): WeatherType {
            return when (code) {
                0 -> CLEAR
                1, 2 -> PARTLY_CLOUDY
                3 -> OVERCAST
                45, 48 -> FOG
                51, 53, 55, 56, 57 -> DRIZZLE
                61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
                71, 73, 75, 77, 85, 86 -> SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> if (code in 50..69) RAIN else PARTLY_CLOUDY
            }
        }
    }
}

/**
 * Current meteorological conditions
 */
data class CurrentWeather(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val weatherType: WeatherType,
    val conditionDescription: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val pressureHpa: Double,
    val uvIndex: Double,
    val cloudCoverPercent: Int,
    val precipitationMm: Double,
    val isDay: Boolean,
    val sunrise: String,
    val sunset: String
) {
    fun tempF(): Double = (temperatureC * 9 / 5) + 32
    fun feelsLikeF(): Double = (feelsLikeC * 9 / 5) + 32
    fun windSpeedMph(): Double = windSpeedKmh * 0.621371
}

/**
 * Hourly forecast point
 */
data class HourlyForecast(
    val timeDisplay: String,
    val hour: Int,
    val tempC: Double,
    val weatherType: WeatherType,
    val rainProbability: Int
)

/**
 * Daily forecast point
 */
data class DailyForecast(
    val dayName: String,
    val dateDisplay: String,
    val weatherType: WeatherType,
    val minTempC: Double,
    val maxTempC: Double,
    val rainProbability: Int
)

/**
 * City location metadata and its famous landmarks for 3D miniature generation
 */
data class LocationItem(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val iconicLandmarks: List<String>,
    val defaultDrawableRes: Int? = null
) {
    val displayName: String get() = if (country.isNotBlank()) "$name, $country" else name
    val landmarkSummary: String get() = iconicLandmarks.joinToString(", ")
}

/**
 * Diorama styling presets for Gemini prompt customization
 */
enum class DioramaStyle(
    val title: String,
    val promptModifier: String
) {
    TILT_SHIFT(
        title = "Tilt-Shift 3D",
        promptModifier = "tilt-shift 3D architectural miniature diorama, shallow depth of field, micro scale, clean lighting"
    ),
    GOLDEN_HOUR(
        title = "Golden Hour",
        promptModifier = "golden hour sunlight warm glow, long shadows, amber sunset reflections on miniature landmark facades"
    ),
    CYBER_NIGHT(
        title = "Neon Twilight",
        promptModifier = "dramatic twilight with vivid neon glows, miniature streetlights, reflective wet miniature pavement"
    ),
    CLAY_MODEL(
        title = "Pastel Clay",
        promptModifier = "stylized artisan clay model diorama, smooth matte tactile surfaces, pastel palette, whimsical miniature aesthetic"
    ),
    PAPERCRAFT(
        title = "3D Origami",
        promptModifier = "delicate multi-layered geometric papercraft architecture, folded paper 3D miniature diorama, soft shadow cuts"
    )
}

/**
 * The 3D miniature cluster state
 */
data class MiniatureDioramaState(
    val location: LocationItem,
    val bitmap: Bitmap? = null,
    @param:DrawableRes val fallbackDrawableRes: Int? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val promptUsed: String = "",
    val aiCommentary: String = "",
    val activeStyle: DioramaStyle = DioramaStyle.TILT_SHIFT
)
