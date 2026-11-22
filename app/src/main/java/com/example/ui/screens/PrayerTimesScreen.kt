package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.PrayerCalculationMethod
import com.example.core.PrayerCity
import com.example.core.PrayerTimesCalculator
import com.example.data.local.UserPreferences
import com.example.ui.theme.GoldAccent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

@Composable
fun PrayerTimesScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    onSelectCity: (String) -> Unit,
    onCustomLocation: (Double, Double, String) -> Unit,
    onSelectMethod: (String) -> Unit
) {
    val context = LocalContext.current
    var locationMessage by remember { mutableStateOf<String?>(null) }

    @SuppressLint("MissingPermission")
    fun fetchCoarseLocation() {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val label = if (strings.isArabic) "موقعي الحالي (GPS)" else "Current GPS Location"
                    onCustomLocation(loc.latitude, loc.longitude, label)
                    locationMessage = null
                } else {
                    locationMessage = if (strings.isArabic) {
                        "تعذر الحصول على الموقع الآن، اختر مدينة من القائمة."
                    } else {
                        "Location unavailable right now; select a city below."
                    }
                }
            }
            .addOnFailureListener {
                locationMessage = if (strings.isArabic) "تعذر تحديد الموقع" else "Unable to detect location"
            }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCoarseLocation()
        } else {
            locationMessage = if (strings.isArabic) {
                "تم رفض إذن الموقع؛ يمكنك اختيار المدينة يدوياً."
            } else {
                "Location permission denied; choose a city manually."
            }
        }
    }

    val activeCity = remember(prefs.selectedCityId, prefs.customLatitude, prefs.customLongitude) {
        if (prefs.selectedCityId == "custom" && prefs.customLatitude != null && prefs.customLongitude != null) {
            PrayerCity(
                id = "custom",
                nameArabic = prefs.customCityName ?: "موقعي الحالي",
                nameEnglish = prefs.customCityName ?: "Current Location",
                latitude = prefs.customLatitude,
                longitude = prefs.customLongitude,
                timeZoneId = TimeZone.getDefault().id
            )
        } else {
            PrayerTimesCalculator.presetCities.find { it.id == prefs.selectedCityId }
                ?: PrayerTimesCalculator.presetCities.first()
        }
    }

    val activeMethod = remember(prefs.selectedCalculationMethodId) {
        PrayerCalculationMethod.entries.find { it.id == prefs.selectedCalculationMethodId }
            ?: PrayerCalculationMethod.UMM_AL_QURA
    }

    val dailyTimes = remember(activeCity, activeMethod) {
        PrayerTimesCalculator.calculate(activeCity, activeMethod)
    }

    val nowCal = Calendar.getInstance(TimeZone.getTimeZone(activeCity.timeZoneId))
    val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

    val rows = listOf(
        Triple(strings.fajr, dailyTimes.fajr, dailyTimes.fajrMinutes),
        Triple(strings.sunrise, dailyTimes.sunrise, dailyTimes.sunriseMinutes),
        Triple(strings.dhuhr, dailyTimes.dhuhr, dailyTimes.dhuhrMinutes),
        Triple(strings.asr, dailyTimes.asr, dailyTimes.asrMinutes),
        Triple(strings.maghrib, dailyTimes.maghrib, dailyTimes.maghribMinutes),
        Triple(strings.isha, dailyTimes.isha, dailyTimes.ishaMinutes)
    )
    val nextPrayer = rows.firstOrNull { it.third > nowMinutes } ?: rows.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_times_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (strings.isArabic) activeCity.nameArabic else activeCity.nameEnglish,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${strings.nextPrayer}: ${nextPrayer.first}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = GoldAccent
                            )
                        }
                        Text(
                            text = nextPrayer.second,
                            style = MaterialTheme.typography.displayLarge,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Explore, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.qiblaDirection}: ${dailyTimes.qiblaDegreesFromNorth.roundToInt()}°",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = { locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color(0xFF1A1400)
                            ),
                            modifier = Modifier.testTag("prayer_detect_location_btn")
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.detectLocation)
                        }
                    }

                    locationMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFFE5B4))
                    }
                }
            }
        }

        // City selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PrayerTimesCalculator.presetCities, key = { it.id }) { city ->
                    FilterChip(
                        selected = prefs.selectedCityId == city.id,
                        onClick = { onSelectCity(city.id) },
                        label = { Text(if (strings.isArabic) city.nameArabic else city.nameEnglish) }
                    )
                }
            }
        }

        // Prayer schedule rows
        items(rows, key = { it.first }) { (name, timeStr, _) ->
            val isNext = name == nextPrayer.first
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Calculation Method selector
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.calculationMethod, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PrayerCalculationMethod.entries.forEach { method ->
                    FilterChip(
                        selected = activeMethod == method,
                        onClick = { onSelectMethod(method.id) },
                        label = { Text(if (strings.isArabic) method.nameArabic else method.nameEnglish) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
