package com.example.data

import com.example.R
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.LocationItem
import com.example.data.model.WeatherType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Pre-curated landmark locations with pre-baked drawable clusters where available
    val presetLocations = listOf(
        LocationItem(
            name = "Paris",
            country = "France",
            latitude = 48.8566,
            longitude = 2.3522,
            iconicLandmarks = listOf("Eiffel Tower", "Louvre Pyramid", "Arc de Triomphe", "Haussmann Facades"),
            defaultDrawableRes = R.drawable.img_paris_cluster_1789451529911
        ),
        LocationItem(
            name = "Tokyo",
            country = "Japan",
            latitude = 35.6762,
            longitude = 139.6503,
            iconicLandmarks = listOf("Tokyo Tower", "Tokyo Skytree", "Senso-ji Pagoda", "Shibuya Modern Skyline"),
            defaultDrawableRes = R.drawable.img_tokyo_cluster_1789451541355
        ),
        LocationItem(
            name = "New York",
            country = "United States",
            latitude = 40.7128,
            longitude = -74.0060,
            iconicLandmarks = listOf("Empire State Building", "Statue of Liberty", "Chrysler Building", "Brooklyn Bridge")
        ),
        LocationItem(
            name = "London",
            country = "United Kingdom",
            latitude = 51.5074,
            longitude = -0.1278,
            iconicLandmarks = listOf("Big Ben & Elizabeth Tower", "London Eye", "Tower Bridge", "The Shard")
        ),
        LocationItem(
            name = "Cairo",
            country = "Egypt",
            latitude = 30.0444,
            longitude = 31.2357,
            iconicLandmarks = listOf("Great Pyramids of Giza", "The Sphinx", "Cairo Tower", "Historic Citadel")
        ),
        LocationItem(
            name = "Sydney",
            country = "Australia",
            latitude = -33.8688,
            longitude = 151.2093,
            iconicLandmarks = listOf("Sydney Opera House", "Harbour Bridge", "Sydney Tower Eye", "Darling Harbour")
        ),
        LocationItem(
            name = "San Francisco",
            country = "United States",
            latitude = 37.7749,
            longitude = -122.4194,
            iconicLandmarks = listOf("Golden Gate Bridge", "Transamerica Pyramid", "Coit Tower", "Victorian Painted Ladies")
        ),
        LocationItem(
            name = "Rome",
            country = "Italy",
            latitude = 41.9028,
            longitude = 12.4964,
            iconicLandmarks = listOf("The Colosseum", "St. Peter's Dome", "The Pantheon", "Trevi Fountain")
        )
    )

    val defaultLocation: LocationItem get() = presetLocations[0]

    /**
     * Search locations using Open-Meteo Geocoding API
     */
    suspend fun searchLocations(query: String): List<LocationItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext presetLocations

        try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=${trimmed}&count=6&language=en&format=json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful || body.isBlank()) {
                return@withContext filterPresets(trimmed)
            }

            val json = JSONObject(body)
            if (!json.has("results")) {
                return@withContext filterPresets(trimmed)
            }

            val resultsArray = json.getJSONArray("results")
            val list = mutableListOf<LocationItem>()
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val cityName = item.optString("name", "")
                val country = item.optString("country", "")
                val lat = item.optDouble("latitude", 0.0)
                val lon = item.optDouble("longitude", 0.0)

                // Match with presets to keep pre-baked landmarks and drawables if matching
                val matchingPreset = presetLocations.firstOrNull { it.name.equals(cityName, ignoreCase = true) }

                val landmarks = matchingPreset?.iconicLandmarks ?: listOf(
                    "$cityName City Center",
                    "$cityName Main Cathedral / Monument",
                    "$cityName Waterfront & Skyline"
                )

                list.add(
                    LocationItem(
                        name = cityName,
                        country = country,
                        latitude = lat,
                        longitude = lon,
                        iconicLandmarks = landmarks,
                        defaultDrawableRes = matchingPreset?.defaultDrawableRes
                    )
                )
            }
            if (list.isEmpty()) filterPresets(trimmed) else list
        } catch (e: Exception) {
            filterPresets(trimmed)
        }
    }

    private fun filterPresets(query: String): List<LocationItem> {
        return presetLocations.filter {
            it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true)
        }
    }

    /**
     * Fetch real-time weather from Open-Meteo
     */
    suspend fun getWeatherForecast(location: LocationItem): WeatherDataBundle = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=${location.latitude}&longitude=${location.longitude}&" +
                    "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m&" +
                    "hourly=temperature_2m,weather_code,precipitation_probability&" +
                    "daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset,uv_index_max&" +
                    "timezone=auto"

            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.isNotBlank()) {
                parseWeatherData(body)
            } else {
                createFallbackWeather(location)
            }
        } catch (e: Exception) {
            createFallbackWeather(location)
        }
    }

    private fun parseWeatherData(jsonString: String): WeatherDataBundle {
        val root = JSONObject(jsonString)
        val currentObj = root.getJSONObject("current")
        val weatherCode = currentObj.optInt("weather_code", 0)
        val weatherType = WeatherType.fromWmoCode(weatherCode)

        val dailyObj = root.optJSONObject("daily")
        val sunriseStr = dailyObj?.optJSONArray("sunrise")?.optString(0, "06:45") ?: "06:45"
        val sunsetStr = dailyObj?.optJSONArray("sunset")?.optString(0, "19:30") ?: "19:30"
        val uvMax = dailyObj?.optJSONArray("uv_index_max")?.optDouble(0, 4.5) ?: 4.5

        val current = CurrentWeather(
            temperatureC = currentObj.optDouble("temperature_2m", 21.0),
            feelsLikeC = currentObj.optDouble("apparent_temperature", 20.5),
            weatherType = weatherType,
            conditionDescription = weatherType.title,
            humidityPercent = currentObj.optInt("relative_humidity_2m", 58),
            windSpeedKmh = currentObj.optDouble("wind_speed_10m", 14.2),
            windDirectionDeg = currentObj.optInt("wind_direction_10m", 210),
            pressureHpa = currentObj.optDouble("pressure_msl", 1014.0),
            uvIndex = uvMax,
            cloudCoverPercent = currentObj.optInt("cloud_cover", 35),
            precipitationMm = currentObj.optDouble("precipitation", 0.0),
            isDay = currentObj.optInt("is_day", 1) == 1,
            sunrise = formatTime(sunriseStr),
            sunset = formatTime(sunsetStr)
        )

        // Parse hourly (next 24 hours)
        val hourlyList = mutableListOf<HourlyForecast>()
        val hourlyObj = root.optJSONObject("hourly")
        if (hourlyObj != null) {
            val times = hourlyObj.optJSONArray("time")
            val temps = hourlyObj.optJSONArray("temperature_2m")
            val codes = hourlyObj.optJSONArray("weather_code")
            val rainProbs = hourlyObj.optJSONArray("precipitation_probability")

            val count = minOf(times?.length() ?: 0, 24)
            for (i in 0 until count) {
                val t = times?.optString(i, "") ?: ""
                val hourStr = t.substringAfter("T").take(5)
                val hourInt = hourStr.take(2).toIntOrNull() ?: i
                val temp = temps?.optDouble(i, 20.0) ?: 20.0
                val code = codes?.optInt(i, 0) ?: 0
                val rainP = rainProbs?.optInt(i, 0) ?: 0

                hourlyList.add(
                    HourlyForecast(
                        timeDisplay = if (i == 0) "Now" else hourStr,
                        hour = hourInt,
                        tempC = temp,
                        weatherType = WeatherType.fromWmoCode(code),
                        rainProbability = rainP
                    )
                )
            }
        }

        // Parse daily (next 7 days)
        val dailyList = mutableListOf<DailyForecast>()
        if (dailyObj != null) {
            val times = dailyObj.optJSONArray("time")
            val codes = dailyObj.optJSONArray("weather_code")
            val maxTemps = dailyObj.optJSONArray("temperature_2m_max")
            val minTemps = dailyObj.optJSONArray("temperature_2m_min")
            val rainProbMax = dailyObj.optJSONArray("precipitation_probability_max")

            val count = minOf(times?.length() ?: 0, 7)
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val calendar = Calendar.getInstance()

            for (i in 0 until count) {
                val dateStr = times?.optString(i, "") ?: ""
                val code = codes?.optInt(i, 0) ?: 0
                val maxT = maxTemps?.optDouble(i, 22.0) ?: 22.0
                val minT = minTemps?.optDouble(i, 14.0) ?: 14.0
                val rainP = rainProbMax?.optInt(i, 10) ?: 10

                val dayName = if (i == 0) "Today" else {
                    calendar.timeInMillis = System.currentTimeMillis() + i * 86400000L
                    dayFormat.format(calendar.time)
                }

                dailyList.add(
                    DailyForecast(
                        dayName = dayName,
                        dateDisplay = dateStr.takeLast(5),
                        weatherType = WeatherType.fromWmoCode(code),
                        minTempC = minT,
                        maxTempC = maxT,
                        rainProbability = rainP
                    )
                )
            }
        }

        return WeatherDataBundle(
            current = current,
            hourly = hourlyList,
            daily = dailyList
        )
    }

    private fun formatTime(rawIso: String): String {
        return if (rawIso.contains("T")) {
            rawIso.substringAfter("T").take(5)
        } else {
            rawIso
        }
    }

    private fun createFallbackWeather(location: LocationItem): WeatherDataBundle {
        val current = CurrentWeather(
            temperatureC = 21.0,
            feelsLikeC = 20.0,
            weatherType = WeatherType.PARTLY_CLOUDY,
            conditionDescription = "Mild & Partly Cloudy",
            humidityPercent = 55,
            windSpeedKmh = 12.0,
            windDirectionDeg = 180,
            pressureHpa = 1015.0,
            uvIndex = 5.2,
            cloudCoverPercent = 40,
            precipitationMm = 0.0,
            isDay = true,
            sunrise = "06:30",
            sunset = "19:45"
        )

        val hourly = (0..23).map { hour ->
            HourlyForecast(
                timeDisplay = if (hour == 0) "Now" else String.format(Locale.US, "%02d:00", hour),
                hour = hour,
                tempC = 18.0 + (hour % 6) * 1.5,
                weatherType = if (hour in 12..16) WeatherType.CLEAR else WeatherType.PARTLY_CLOUDY,
                rainProbability = (hour * 3) % 25
            )
        }

        val days = listOf("Today", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daily = days.mapIndexed { idx, day ->
            DailyForecast(
                dayName = day,
                dateDisplay = "Day ${idx + 1}",
                weatherType = if (idx == 3) WeatherType.RAIN else WeatherType.PARTLY_CLOUDY,
                minTempC = 14.0 + (idx % 3),
                maxTempC = 22.0 + (idx % 4),
                rainProbability = if (idx == 3) 65 else 15
            )
        }

        return WeatherDataBundle(current, hourly, daily)
    }
}

data class WeatherDataBundle(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)
