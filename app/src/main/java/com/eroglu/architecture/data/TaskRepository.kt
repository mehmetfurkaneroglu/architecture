package com.eroglu.architecture.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository { // ViewModel, veriye doğrudan erişmez. Bunun yerine TaskRepository arayüzü üzerinden veri talep eder.

    fun getTasksStream(): Flow<List<Task>> // Tüm görevlerin listesini gözlemlenebilir bir akış (Flow) olarak döndürür.

    suspend fun getTasks(forceUpdate: Boolean = false): List<Task> // Tüm görev listesini tek seferlik olarak getirir

    suspend fun refresh() // Tüm görev listesini sunucudan zorla yenilemek için kullanılır.

    fun getTaskStream(taskId: String): Flow<Task> // Belirli bir görevin detaylarını gözlemlenebilir bir akış (Flow) olarak döndürür.

    suspend fun getTask(taskId: String, forceUpdate: Boolean = false): Task? // Belirli bir görevin detaylarını tek seferlik olarak getirir.

    suspend fun refreshTask(taskId: String) // Belirli bir görevin detaylarını sunucudan zorla yenilemek için kullanılır.

    suspend fun createTask(title: String, description: String): String // Yeni bir görevi oluşturmak için kullanılır.

    suspend fun updateTask(taskId: String, title: String, description: String) // Görevin detaylarını güncellemek için kullanılır.

    suspend fun completeTask(taskId: String) // Görevi tamamlamak için kullanılır.

    suspend fun activateTask(taskId: String) // Görevi aktif etmek için kullanılır.

    suspend fun clearCompletedTasks() // Tamamlanmış görevleri temizlemek için kullanılır.

    suspend fun deleteAllTasks() // Tüm görevleri silmek için kullanılır.

    suspend fun deleteTask(taskId: String) // Belirli bir görevi silmek için kullanılır.

}