package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CurrentWeather
import com.example.data.model.WeatherType
import com.example.ui.components.CurrentWeatherHeroCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleWeather = CurrentWeather(
      temperatureC = 22.0,
      feelsLikeC = 21.0,
      weatherType = WeatherType.CLEAR,
      conditionDescription = "Sunny & Clear",
      humidityPercent = 48,
      windSpeedKmh = 12.0,
      windDirectionDeg = 180,
      pressureHpa = 1014.0,
      uvIndex = 6.0,
      cloudCoverPercent = 10,
      precipitationMm = 0.0,
      isDay = true,
      sunrise = "06:15",
      sunset = "20:10"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        CurrentWeatherHeroCard(
          current = sampleWeather,
          isFahrenheit = false,
          onToggleUnit = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

