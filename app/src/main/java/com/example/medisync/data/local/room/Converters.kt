package com.example.medisync.data.local.room

import androidx.room.TypeConverter
import com.example.medisync.model.MemberVitals

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        return value.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun fromMemberVitalsMap(value: Map<String, MemberVitals>?): String {
        if (value == null) return "{}"
        val json = org.json.JSONObject()
        value.forEach { (name, vitals) ->
            val obj = org.json.JSONObject()
            obj.put("bloodType", vitals.bloodType)
            obj.put("bloodPressure", vitals.bloodPressure)
            obj.put("bloodSugar", vitals.bloodSugar)
            obj.put("bloodTypeLastUpdated", vitals.bloodTypeLastUpdated)
            obj.put("bloodPressureLastUpdated", vitals.bloodPressureLastUpdated)
            obj.put("bloodSugarLastUpdated", vitals.bloodSugarLastUpdated)
            json.put(name, obj)
        }
        return json.toString()
    }

    @TypeConverter
    fun toMemberVitalsMap(value: String?): Map<String, MemberVitals> {
        if (value.isNullOrEmpty()) return emptyMap()
        val map = mutableMapOf<String, MemberVitals>()
        try {
            val json = org.json.JSONObject(value)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = json.getJSONObject(key)
                map[key] = MemberVitals(
                    bloodType = obj.optString("bloodType", ""),
                    bloodPressure = obj.optString("bloodPressure", ""),
                    bloodSugar = obj.optString("bloodSugar", ""),
                    bloodTypeLastUpdated = obj.optLong("bloodTypeLastUpdated", 0L),
                    bloodPressureLastUpdated = obj.optLong("bloodPressureLastUpdated", 0L),
                    bloodSugarLastUpdated = obj.optLong("bloodSugarLastUpdated", 0L)
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
        return map
    }
}
