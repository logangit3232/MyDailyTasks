package com.mydailytasks.app.data.local

import androidx.room.TypeConverter
import com.mydailytasks.app.data.model.RepeatType

class Converters {
    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = runCatching {
        RepeatType.valueOf(value)
    }.getOrDefault(RepeatType.ONCE)

    @TypeConverter
    fun fromIntList(list: List<Int>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
