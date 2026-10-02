package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.CurrentWeather
import com.example.data.model.DioramaStyle
import com.example.data.model.LocationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiDioramaResult {
    data class Success(
        val bitmap: Bitmap?,
        val commentary: String,
        val promptUsed: String
    ) : GeminiDioramaResult()

    data class Error(
        val message: String,
        val isApiKeyMissing: Boolean = false,
        val fallbackCommentary: String = ""
    ) : GeminiDioramaResult()
}

class GeminiRepository {

    // OkHttp configured with 60s timeout as mandated in Gemini API guidelines
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Generate the 3D miniature horizontal cluster diorama image and meteorological commentary
     */
    suspend fun generateMiniatureDiorama(
        location: LocationItem,
        weather: CurrentWeather,
        style: DioramaStyle
    ): GeminiDioramaResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val landmarksStr = location.landmarkSummary

        val prompt = "A 3D miniature horizontal cluster diorama of landmarks and architectural buildings representing ${location.name} ($landmarksStr) in a wide horizontal panoramic cluster on an elevated floating platform, tilt-shift lens perspective, ${style.promptModifier}, atmospheric weather mood reflecting ${weather.conditionDescription}, clean soft depth of field, high detail 3D model render, horizontal panoramic composition."

        if (!isApiKeyConfigured()) {
            val proceduralCommentary = generateProceduralCommentary(location, weather, style)
            return@withContext GeminiDioramaResult.Error(
                message = "Add GEMINI_API_KEY in the AI Studio Secrets panel to enable real-time 3D miniature diorama generation with Gemini.",
                isApiKeyMissing = true,
                fallbackCommentary = proceduralCommentary
            )
        }

        try {
            // 1. Generate 3D Miniature Image using gemini-2.5-flash-image
            val imageBitmap = fetchGeminiImage(apiKey, prompt)

            // 2. Generate atmospheric diorama narrative commentary using gemini-3.5-flash
            val commentary = fetchGeminiCommentary(apiKey, location, weather, style)

            GeminiDioramaResult.Success(
                bitmap = imageBitmap,
                commentary = commentary,
                promptUsed = prompt
            )
        } catch (e: Exception) {
            val proceduralCommentary = generateProceduralCommentary(location, weather, style)
            GeminiDioramaResult.Error(
                message = e.message ?: "Failed to generate diorama with Gemini",
                isApiKeyMissing = false,
                fallbackCommentary = proceduralCommentary
            )
        }
    }

    /**
     * Call gemini-2.5-flash-image to generate the 3D miniature image
     */
    private fun fetchGeminiImage(apiKey: String, prompt: String): Bitmap? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)

            val generationConfig = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                }
                put("responseModalities", modalities)
                val imageConfig = JSONObject().apply {
                    put("aspectRatio", "16:9")
                    put("imageSize", "1K")
                }
                put("imageConfig", imageConfig)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(responseBody)
            throw RuntimeException(errorMsg)
        }

        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null

        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("inlineData")) {
                val inlineData = part.getJSONObject("inlineData")
                val base64Data = inlineData.optString("data", "")
                if (base64Data.isNotBlank()) {
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                }
            }
        }
        return null
    }

    /**
     * Call gemini-3.5-flash for narrative diorama analysis
     */
    private fun fetchGeminiCommentary(
        apiKey: String,
        location: LocationItem,
        weather: CurrentWeather,
        style: DioramaStyle
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val textPrompt = "Provide a vivid 2-3 sentence meteorological critique and architectural description of a 3D miniature horizontal diorama representing ${location.name} (${location.landmarkSummary}) rendered in ${style.title} style under ${weather.conditionDescription} at ${weather.temperatureC.toInt()}°C. Mention specific miniature details like tiny clouds, rainfall or sunlight catching the micro-sculpted landmark facades."

        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", textPrompt)
                })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val text = candidates.getJSONObject(0)
                    .optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "")
                if (!text.isNullOrBlank()) {
                    return text.trim()
                }
            }
        }
        return generateProceduralCommentary(location, weather, style)
    }

    private fun parseErrorMessage(body: String): String {
        return try {
            val root = JSONObject(body)
            val error = root.optJSONObject("error")
            error?.optString("message", "Gemini API error") ?: "Gemini API error: HTTP error"
        } catch (e: Exception) {
            "Gemini API request failed"
        }
    }

    fun generateProceduralCommentary(
        location: LocationItem,
        weather: CurrentWeather,
        style: DioramaStyle
    ): String {
        val landmarks = location.iconicLandmarks.take(3).joinToString(", ")
        val skyAtmosphere = when (weather.weatherType) {
            com.example.data.model.WeatherType.CLEAR -> "bathed in brilliant miniature sunshine with crisp architectural shadows"
            com.example.data.model.WeatherType.PARTLY_CLOUDY -> "framed by soft floating cumulus cloudlets casting micro shadows"
            com.example.data.model.WeatherType.OVERCAST -> "shrouded in atmospheric diffuse daylight with delicate tonal gradients"
            com.example.data.model.WeatherType.RAIN, com.example.data.model.WeatherType.DRIZZLE -> "glistening under fine micro-droplets and gentle reflective puddles"
            com.example.data.model.WeatherType.THUNDERSTORM -> "dramatically backlit by storm clouds with high-contrast ambient glow"
            com.example.data.model.WeatherType.SNOW -> "dusted with delicate miniature snowfall coating rooftops and spires"
            com.example.data.model.WeatherType.FOG -> "veiled in a low-hanging soft mist hugging the miniature skyline"
        }

        return "This 3D miniature horizontal diorama highlights the iconic contours of $landmarks in ${location.name}, $skyAtmosphere in ${style.title} style at ${weather.temperatureC.toInt()}°C."
    }
}
