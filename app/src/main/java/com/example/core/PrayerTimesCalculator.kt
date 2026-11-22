package com.example.core

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class PrayerCalculationMethod(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    UMM_AL_QURA(
        id = "umm_al_qura",
        nameArabic = "أم القرى (مكة المكرمة)",
        nameEnglish = "Umm al-Qura University, Makkah",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaMinutesAfterMaghrib = 90
    ),
    MWL(
        id = "mwl",
        nameArabic = "رابطة العالم الإسلامي",
        nameEnglish = "Muslim World League",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPTIAN(
        id = "egyptian",
        nameArabic = "الهيئة المصرية العامة للمساحة",
        nameEnglish = "Egyptian General Authority",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    ISNA(
        id = "isna",
        nameArabic = "الجمعية الإسلامية لأمريكا الشمالية",
        nameEnglish = "ISNA (North America)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    KARACHI(
        id = "karachi",
        nameArabic = "جامعة العلوم الإسلامية بكراتشي",
        nameEnglish = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    )
}

data class PrayerCity(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

data class DailyPrayerTimes(
    val city: PrayerCity,
    val method: PrayerCalculationMethod,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val fajrMinutes: Int,
    val sunriseMinutes: Int,
    val dhuhrMinutes: Int,
    val asrMinutes: Int,
    val maghribMinutes: Int,
    val ishaMinutes: Int,
    val qiblaDegreesFromNorth: Double
)

object PrayerTimesCalculator {

    val presetCities = listOf(
        PrayerCity("makkah", "مكة المكرمة", "Makkah", 21.3891, 39.8579, "Asia/Riyadh"),
        PrayerCity("madinah", "المدينة المنورة", "Madinah", 24.5247, 39.5692, "Asia/Riyadh"),
        PrayerCity("riyadh", "الرياض", "Riyadh", 24.7136, 46.6753, "Asia/Riyadh"),
        PrayerCity("khartoum", "الخرطوم", "Khartoum", 15.5007, 32.5599, "Africa/Khartoum"),
        PrayerCity("cairo", "القاهرة", "Cairo", 30.0444, 31.2357, "Africa/Cairo"),
        PrayerCity("dubai", "دبي", "Dubai", 25.2048, 55.2708, "Asia/Dubai"),
        PrayerCity("amman", "عمّان", "Amman", 31.9454, 35.9284, "Asia/Amman"),
        PrayerCity("istanbul", "إسطنبول", "Istanbul", 41.0082, 28.9784, "Europe/Istanbul"),
        PrayerCity("kuala_lumpur", "كوالالمبور", "Kuala Lumpur", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        PrayerCity("london", "لندن", "London", 51.5072, -0.1276, "Europe/London"),
        PrayerCity("new_york", "نيويورك", "New York", 40.7128, -74.0060, "America/New_York")
    )

    fun calculate(
        city: PrayerCity,
        method: PrayerCalculationMethod,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
    ): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tz = TimeZone.getTimeZone(city.timeZoneId)
        val offsetHours = tz.getOffset(calendar.timeInMillis) / 3_600_000.0

        val jd = julianDate(year, month, day) - city.longitude / (15.0 * 24.0)
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d

        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val decl = darcsin(dsin(e) * dsin(l))
        val eqOfTime = q / 15.0 - fixHour(ra)

        val transit = 12.0 + offsetHours - city.longitude / 15.0 - eqOfTime

        val fajrHours = transit - sunAngleTime(method.fajrAngle, city.latitude, decl)
        val sunriseHours = transit - sunAngleTime(0.833, city.latitude, decl)
        val dhuhrHours = transit + (2.0 / 60.0)
        val asrHours = transit + asrTime(1.0, city.latitude, decl)
        val maghribHours = transit + sunAngleTime(0.833, city.latitude, decl)
        val ishaHours = if (method.ishaMinutesAfterMaghrib != null) {
            maghribHours + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            transit + sunAngleTime(method.ishaAngle, city.latitude, decl)
        }

        val fajrMin = toMinutes(fajrHours)
        val sunriseMin = toMinutes(sunriseHours)
        val dhuhrMin = toMinutes(dhuhrHours)
        val asrMin = toMinutes(asrHours)
        val maghribMin = toMinutes(maghribHours)
        val ishaMin = toMinutes(ishaHours)

        return DailyPrayerTimes(
            city = city,
            method = method,
            fajr = formatMinutes(fajrMin),
            sunrise = formatMinutes(sunriseMin),
            dhuhr = formatMinutes(dhuhrMin),
            asr = formatMinutes(asrMin),
            maghrib = formatMinutes(maghribMin),
            isha = formatMinutes(ishaMin),
            fajrMinutes = fajrMin,
            sunriseMinutes = sunriseMin,
            dhuhrMinutes = dhuhrMin,
            asrMinutes = asrMin,
            maghribMinutes = maghribMin,
            ishaMinutes = ishaMin,
            qiblaDegreesFromNorth = calculateQiblaBearing(city.latitude, city.longitude)
        )
    }

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLon = Math.toRadians(39.826206)
        val latRad = Math.toRadians(latitude)
        val dLon = kaabaLon - Math.toRadians(longitude)

        val y = sin(dLon)
        val x = cos(latRad) * tan(kaabaLat) - sin(latRad) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val num = -dsin(angle) - dsin(lat) * dsin(decl)
        val den = dcos(lat) * dcos(decl)
        val ratio = (num / den).coerceIn(-1.0, 1.0)
        return darccos(ratio) / 15.0
    }

    private fun asrTime(factor: Double, lat: Double, decl: Double): Double {
        val angle = -darccot(factor + dtan(kotlin.math.abs(lat - decl)))
        return sunAngleTime(angle, lat, decl)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun toMinutes(hours: Double): Int {
        val fixed = fixHour(hours)
        return ((fixed * 60.0) + 0.5).toInt() % 1440
    }

    private fun formatMinutes(totalMinutes: Int): String {
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return String.format("%02d:%02d", h, m)
    }

    private fun fixAngle(a: Double): Double {
        val res = a - 360.0 * floor(a / 360.0)
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(a: Double): Double {
        val res = a - 24.0 * floor(a / 24.0)
        return if (res < 0) res + 24.0 else res
    }

    private fun dsin(d: Double) = sin(Math.toRadians(d))
    private fun dcos(d: Double) = cos(Math.toRadians(d))
    private fun dtan(d: Double) = tan(Math.toRadians(d))
    private fun darcsin(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun darccos(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun darctan2(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun darccot(x: Double) = Math.toDegrees(atan(1.0 / x))
}
