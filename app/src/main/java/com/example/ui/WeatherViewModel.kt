package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiDioramaResult
import com.example.data.GeminiRepository
import com.example.data.WeatherDataBundle
import com.example.data.WeatherRepository
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecast
import com.example.data.model.DioramaStyle
import com.example.data.model.HourlyForecast
import com.example.data.model.LocationItem
import com.example.data.model.MiniatureDioramaState
import com.example.data.model.WeatherType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val location: LocationItem,
    val weatherBundle: WeatherDataBundle? = null,
    val dioramaState: MiniatureDioramaState,
    val isFahrenheit: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<LocationItem> = emptyList(),
    val isApiKeyConfigured: Boolean = false
)

class WeatherViewModel(
    private val weatherRepo: WeatherRepository = WeatherRepository(),
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _uiState: MutableStateFlow<WeatherUiState>
    val uiState: StateFlow<WeatherUiState>

    private var searchJob: Job? = null
    private var dioramaJob: Job? = null

    init {
        val initialLocation = weatherRepo.defaultLocation
        val initialDioramaState = MiniatureDioramaState(
            location = initialLocation,
            fallbackDrawableRes = initialLocation.defaultDrawableRes,
            activeStyle = DioramaStyle.TILT_SHIFT
        )

        _uiState = MutableStateFlow(
            WeatherUiState(
                location = initialLocation,
                dioramaState = initialDioramaState,
                isApiKeyConfigured = geminiRepo.isApiKeyConfigured()
            )
        )
        uiState = _uiState.asStateFlow()

        // Load initial weather and diorama
        loadWeatherAndDiorama(initialLocation, style = DioramaStyle.TILT_SHIFT)
    }

    val presetLocations: List<LocationItem>
        get() = weatherRepo.presetLocations

    fun selectLocation(location: LocationItem) {
        if (location == _uiState.value.location) return
        loadWeatherAndDiorama(location, style = _uiState.value.dioramaState.activeStyle)
    }

    fun toggleTemperatureUnit() {
        _uiState.update { it.copy(isFahrenheit = !it.isFahrenheit) }
    }

    fun searchLocations(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(350) // Debounce search
            val results = weatherRepo.searchLocations(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun regenerateDioramaWithStyle(style: DioramaStyle) {
        val currentLoc = _uiState.value.location
        val currentWeather = _uiState.value.weatherBundle?.current ?: return
        generateDiorama(currentLoc, currentWeather, style)
    }

    fun refreshWeather() {
        loadWeatherAndDiorama(_uiState.value.location, _uiState.value.dioramaState.activeStyle)
    }

    private fun loadWeatherAndDiorama(location: LocationItem, style: DioramaStyle) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    location = location,
                    isLoadingWeather = true,
                    dioramaState = it.dioramaState.copy(
                        location = location,
                        fallbackDrawableRes = location.defaultDrawableRes,
                        errorMessage = null
                    )
                )
            }

            val bundle = weatherRepo.getWeatherForecast(location)

            _uiState.update {
                it.copy(
                    weatherBundle = bundle,
                    isLoadingWeather = false
                )
            }

            // Trigger Gemini diorama generation
            generateDiorama(location, bundle.current, style)
        }
    }

    private fun generateDiorama(location: LocationItem, weather: CurrentWeather, style: DioramaStyle) {
        dioramaJob?.cancel()
        dioramaJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    dioramaState = it.dioramaState.copy(
                        isGenerating = true,
                        errorMessage = null,
                        activeStyle = style
                    )
                )
            }

            val result = geminiRepo.generateMiniatureDiorama(location, weather, style)

            when (result) {
                is GeminiDioramaResult.Success -> {
                    _uiState.update {
                        it.copy(
                            dioramaState = it.dioramaState.copy(
                                bitmap = result.bitmap,
                                aiCommentary = result.commentary,
                                promptUsed = result.promptUsed,
                                isGenerating = false,
                                errorMessage = null
                            )
                        )
                    }
                }
                is GeminiDioramaResult.Error -> {
                    _uiState.update {
                        it.copy(
                            dioramaState = it.dioramaState.copy(
                                isGenerating = false,
                                errorMessage = result.message,
                                aiCommentary = result.fallbackCommentary.ifBlank {
                                    geminiRepo.generateProceduralCommentary(location, weather, style)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
