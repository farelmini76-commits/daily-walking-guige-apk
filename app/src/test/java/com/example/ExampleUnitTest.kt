package com.example

import com.example.data.model.CurrentWeather
import com.example.data.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testWeatherTypeFromWmoCode() {
    assertEquals(WeatherType.CLEAR, WeatherType.fromWmoCode(0))
    assertEquals(WeatherType.PARTLY_CLOUDY, WeatherType.fromWmoCode(1))
    assertEquals(WeatherType.OVERCAST, WeatherType.fromWmoCode(3))
    assertEquals(WeatherType.RAIN, WeatherType.fromWmoCode(61))
    assertEquals(WeatherType.SNOW, WeatherType.fromWmoCode(71))
    assertEquals(WeatherType.THUNDERSTORM, WeatherType.fromWmoCode(95))
  }

  @Test
  fun testTemperatureConversion() {
    val weather = CurrentWeather(
      temperatureC = 20.0,
      feelsLikeC = 20.0,
      weatherType = WeatherType.CLEAR,
      conditionDescription = "Clear",
      humidityPercent = 50,
      windSpeedKmh = 10.0,
      windDirectionDeg = 0,
      pressureHpa = 1013.0,
      uvIndex = 5.0,
      cloudCoverPercent = 0,
      precipitationMm = 0.0,
      isDay = true,
      sunrise = "06:00",
      sunset = "18:00"
    )
    assertEquals(68.0, weather.tempF(), 0.01)
  }
}

