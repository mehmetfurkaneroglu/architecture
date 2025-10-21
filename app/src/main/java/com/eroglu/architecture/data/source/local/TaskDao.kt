package com.eroglu.architecture.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao // Database access object
interface TaskDao { //Room veritabanı üzerinde yapılacak işlemleri (ekleme, silme, güncelleme, sorgulama) tanımlayan arayüzü oluştur.

//    suspend: Bu fonksiyonlar tek seferlik işlemler içindir. Veriyi bir kez çekerler ve işlem biter. Örneğin, bir arka plan servisi tüm görevleri alıp sunucuya gönderecekse getAll() kullanır.
//    Flow: Bu fonksiyonlar ise veri akışı sağlar. Veritabanındaki veri değiştiğinde (yeni görev eklendiğinde, güncellendiğinde vb.) bu fonksiyonu dinleyen arayüz otomatik olarak güncellenir. Ekranda görev listesini gösterirken observeAll() kullanmak, listeyi her zaman güncel tutar.
//    Bir tek görevi eklemek/güncellemek için upsert(task) kullanılır.
//    Bir liste dolusu görevi tek seferde eklemek/güncellemek için upsertAll(tasks) kullanılır.

    // Sürekli değişimleri gözlemler
    @Query("SELECT * FROM task")
    fun observeAll(): Flow<List<LocalTask>>

    @Query("SELECT * FROM task WHERE id = :taskId")
    fun observeById(taskId: String): Flow<LocalTask>

    // Sadece bir defa çalışır
    @Query("SELECT * FROM task")
    suspend fun getAll(): List<LocalTask>

    @Query("SELECT * FROM task WHERE id = :taskId")
    suspend fun getById(taskId: String): LocalTask?

    @Upsert
    suspend fun upsert(task: LocalTask)

    @Upsert
    suspend fun upsertAll(tasks: List<LocalTask>)

    @Query("UPDATE task SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updatedCompleted(taskId: String, completed: Boolean)

    @Query("DELETE FROM task WHERE id = :taskId")
    suspend fun deleteById(taskId: String): Int // Int dönüş tipi o sorgunun veritabanında kaç satırı etkilediğini (sildiğini) belirtir.

    @Query("DELETE FROM task")
    suspend fun deleteAll()

    @Query("DELETE FROM task WHERE isCompleted = 1")
    suspend fun deleteCompleted(): Int
}