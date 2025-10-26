package com.drivetree.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromLangs(list: List<String>): String = list.joinToString("|")
    @TypeConverter fun toLangs(csv: String): List<String> =
        if (csv.isBlank()) emptyList() else csv.split("|")
}
