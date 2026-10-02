package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationItem

@Composable
fun LocationSearchHeader(
    currentLocation: LocationItem,
    presetLocations: List<LocationItem>,
    searchResults: List<LocationItem>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onLocationSelected: (LocationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_search_section")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChange(it)
                isExpanded = it.isNotBlank()
            },
            placeholder = {
                Text(
                    text = "Search city or landmark (e.g. Paris, Tokyo, Cairo)...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            onSearchQueryChange("")
                            isExpanded = false
                        },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("location_search_input")
        )

        // Dropdown Search Results
        AnimatedVisibility(visible = isExpanded && searchResults.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 240.dp)
                    .testTag("search_results_card"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    searchResults.forEachIndexed { index, loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(loc)
                                    searchQuery = ""
                                    isExpanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("search_result_item_$index"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = loc.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = loc.country.ifBlank { "Landmarks: " + loc.landmarkSummary },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < searchResults.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Cities Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetLocations.forEach { preset ->
                val isSelected = preset.name.equals(currentLocation.name, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        searchQuery = ""
                        isExpanded = false
                        onLocationSelected(preset)
                    },
                    label = {
                        Text(
                            text = preset.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("preset_chip_${preset.name.lowercase()}")
                )
            }
        }
    }
}
