package com.example.data.model

import java.util.Locale
import kotlin.math.*

enum class UnitSystem(val label: String, val shortLabel: String) {
    FEET_INCHES("Feet & Inches", "ft-in"),
    METRIC_METERS("Meters", "m"),
    METRIC_MILLIMETERS("Millimeters", "mm")
}

enum class MarlaConvention(val label: String, val sqFtPerMarla: Double, val description: String) {
    LAHORE_COMMERCIAL(
        label = "LDA / Lahore Urban (225 sq ft / Marla)",
        sqFtPerMarla = 225.0,
        description = "Standard for urban residential schemes (LDA, DHA, Bahria, etc.). 1 Kanal = 4,500 sq ft."
    ),
    TRADITIONAL_REVENUE(
        label = "Traditional Revenue (272.25 sq ft / Marla)",
        sqFtPerMarla = 272.25,
        description = "Official revenue department standard (30.25 sq yd / marla). 1 Kanal = 5,445 sq ft. 8 Kanals = 1 Acre."
    );

    fun toMarla(sqFt: Double): Double = sqFt / sqFtPerMarla
    fun toKanal(sqFt: Double): Double = toMarla(sqFt) / 20.0
    fun fromMarlaToSqFt(marla: Double): Double = marla * sqFtPerMarla
}

object UnitConverter {
    const val SQFT_TO_SQM = 0.09290304
    const val SQM_TO_SQFT = 10.76391041671
    const val FEET_TO_METERS = 0.3048
    const val METERS_TO_FEET = 3.280839895
    const val FEET_TO_MM = 304.8
    const val SQFT_PER_ACRE = 43560.0

    /**
     * Converts internal feet dimension to display string according to active unit system.
     */
    fun formatLength(feet: Float, unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.FEET_INCHES -> formatFeetInches(feet)
            UnitSystem.METRIC_METERS -> {
                val meters = feet * FEET_TO_METERS.toFloat()
                String.format(Locale.US, "%.2f m", meters)
            }
            UnitSystem.METRIC_MILLIMETERS -> {
                val mm = feet * FEET_TO_MM.toFloat()
                String.format(Locale.US, "%.0f mm", mm)
            }
        }
    }

    /**
     * Formats feet to architectural standard e.g. 14'-6", 10'-0", 3'-6 1/2"
     */
    fun formatFeetInches(feetDecimal: Float): String {
        val totalInches = round(feetDecimal * 12.0f)
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).toInt()
        return "$feet'-$inches\""
    }

    /**
     * Parses user string like "14'-6\"", "14.5", "14 ft", "14'6", "4.5m" into feet.
     */
    fun parseLengthToFeet(input: String, currentUnit: UnitSystem): Float? {
        val clean = input.trim().replace("\"", "").replace("'", " ").replace("ft", " ").replace("in", " ")
        if (clean.endsWith("m", ignoreCase = true)) {
            val numStr = clean.dropLast(1).trim()
            val m = numStr.toFloatOrNull() ?: return null
            return m * METERS_TO_FEET.toFloat()
        }
        if (clean.endsWith("mm", ignoreCase = true)) {
            val numStr = clean.dropLast(2).trim()
            val mm = numStr.toFloatOrNull() ?: return null
            return (mm / FEET_TO_MM).toFloat()
        }

        // Check if input contains space or dash between feet and inches (e.g. "14 6" or "14-6")
        val parts = clean.split(Regex("[\\s-]+")).filter { it.isNotBlank() }
        if (parts.size == 2) {
            val f = parts[0].toFloatOrNull()
            val i = parts[1].toFloatOrNull()
            if (f != null && i != null) {
                return f + (i / 12f)
            }
        } else if (parts.size == 1) {
            val v = parts[0].toFloatOrNull() ?: return null
            return when (currentUnit) {
                UnitSystem.FEET_INCHES -> v
                UnitSystem.METRIC_METERS -> v * METERS_TO_FEET.toFloat()
                UnitSystem.METRIC_MILLIMETERS -> (v / FEET_TO_MM).toFloat()
            }
        }
        return null
    }

    /**
     * Complete area report calculation.
     */
    data class AreaReport(
        val sqFt: Double,
        val sqM: Double,
        val marla: Double,
        val kanal: Double,
        val acre: Double,
        val formattedSummary: String
    )

    fun calculateAreaReport(sqFt: Double, convention: MarlaConvention): AreaReport {
        val sqM = sqFt * SQFT_TO_SQM
        val marla = convention.toMarla(sqFt)
        val kanal = convention.toKanal(sqFt)
        val acre = sqFt / SQFT_PER_ACRE

        val summary = if (kanal >= 1.0) {
            val wholeKanals = kanal.toInt()
            val remMarla = (kanal - wholeKanals) * 20.0
            String.format(Locale.US, "%d Kanal %.1f Marla (%,.0f sq ft)", wholeKanals, remMarla, sqFt)
        } else {
            String.format(Locale.US, "%.2f Marla (%,.0f sq ft)", marla, sqFt)
        }

        return AreaReport(
            sqFt = sqFt,
            sqM = sqM,
            marla = marla,
            kanal = kanal,
            acre = acre,
            formattedSummary = summary
        )
    }
}
