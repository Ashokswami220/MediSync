package com.example.medisync.utils

object HealthStatusHelper {

    fun getBloodPressureStatus(bp: String): String {
        if (bp.isBlank() || bp == "--/--") return "Unknown"
        
        val parts = bp.split("/")
        if (parts.size != 2) return "Invalid Reading"
        
        val systolic = parts[0].trim().toIntOrNull()
        val diastolic = parts[1].trim().toIntOrNull()
        
        if (systolic == null || diastolic == null) return "Invalid Reading"
        
        return when {
            systolic > 180 || diastolic > 120 -> "Hypertensive Crisis"
            systolic >= 140 || diastolic >= 90 -> "Hypertension Stage 2"
            (systolic in 130..139) || (diastolic in 80..89) -> "Hypertension Stage 1"
            systolic in 120..129 -> "Elevated"
            else -> "Normal"
        }
    }

    fun getBloodTypeStatus(type: String): String {
        val cleanType = type.trim().uppercase()
        if (cleanType.isBlank() || cleanType == "--") return "Unknown"
        
        return when (cleanType) {
            "O-" -> "Universal Donor"
            "AB+" -> "Universal Recipient"
            else -> {
                if (cleanType.endsWith("+")) "Rh Positive"
                else if (cleanType.endsWith("-")) "Rh Negative"
                else "Unknown"
            }
        }
    }

    fun getBloodSugarStatus(sugar: String): String {
        if (sugar.isBlank() || sugar == "--") return "Unknown"
        
        val value = sugar.trim().toIntOrNull() ?: return "Invalid Reading"
        
        return when {
            value >= 126 -> "High (Diabetes)"
            value in 100..125 -> "Prediabetes"
            value in 70..99 -> "Normal"
            else -> "Low (Hypoglycemia)"
        }
    }
}
