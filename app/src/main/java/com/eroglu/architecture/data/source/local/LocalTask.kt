package com.eroglu.architecture.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class LocalTask(
    // Room veritabanı için tabloyu temsil eden entity sınıfı.
    @PrimaryKey val id: String,
    var title: String,
    var description: String,
    var isCompleted: Boolean,
)