package com.eroglu.architecture.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocalTask::class], version = 1, exportSchema = false)
abstract class ToDoDatabase :
    RoomDatabase() { // Room veritabanını tanımlayan abstract sınıfı oluştur
    abstract fun taskDao(): TaskDao
}