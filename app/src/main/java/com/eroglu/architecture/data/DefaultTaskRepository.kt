package com.eroglu.architecture.data

import com.eroglu.android_architecture.di.ApplicationScope
import com.eroglu.android_architecture.di.DefaultDispatcher
import com.eroglu.architecture.data.source.local.TaskDao
import com.eroglu.architecture.data.source.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTaskRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: TaskDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
) : TaskRepository {

    override suspend fun createTask(title: String, description: String): String {
        // ID oluşturma işlemi karmaşık bir işlem olabileceği için, verilen coroutine dispatcher ile yürütülür.
        val taskId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val task = Task(
            id = taskId,
            title = title,
            description = description,
        )
        localDataSource.upsert(task.toLocal())
        saveTasksToNetwork()
        return taskId
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val task = getTask(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")
        localDataSource.upsert(task.toLocal())
        saveTasksToNetwork()
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) { // kullanıcıyı güncellemeye zorlayan (force update) bir yapı
            refresh()
        }
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return localDataSource.observeAll().map { tasks ->
            withContext(dispatcher) {
                tasks.toExternal()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        refresh()
    }

    override fun getTaskStream(taskId: String): Flow<Task> {
        return localDataSource.observeById(taskId).map { it.toExternal() }
    }

    //        Verilen ID’ye sahip bir görevi getirir. Görev bulunamazsa null döner.
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (forceUpdate) {
            refresh()
        }
        return localDataSource.getById(taskId)?.toExternal()
    }

    override suspend fun completeTask(taskId: String) {
        localDataSource.updatedCompleted(taskId = taskId, completed = true)
        saveTasksToNetwork()
    }

    override suspend fun activateTask(taskId: String) {
        localDataSource.updatedCompleted(taskId = taskId, completed = false)
        saveTasksToNetwork()
    }

    override suspend fun clearCompletedTasks() {
        localDataSource.deleteCompleted()
        saveTasksToNetwork()
    }

    override suspend fun deleteAllTasks() {
        localDataSource.deleteAll()
        saveTasksToNetwork()
    }

    override suspend fun deleteTask(taskId: String) {
        localDataSource.deleteById(taskId)
        saveTasksToNetwork()
    }

    /**
     * Aşağıdaki metotlar görevleri ağdan yükler (refresh) ve ağa kaydeder (save).
     *
     * Gerçek uygulamalarda burada yapılan “tek yönlü tüm senkronizasyon” yaklaşımı yerine
     * daha kapsamlı bir senkronizasyon yapılabilir.
     * Daha verimli ve sağlam stratejiler için şu bağlantıya bakabilirsiniz:
     * https://developer.android.com/topic/architecture/data-layer/offline-first
     *
     * Dikkat edilmesi gereken nokta:
     * - refresh işlemi askıya alınabilir (suspend) bir fonksiyondur, yani çağıran beklemek zorundadır.
     * - save işlemi askıya alınabilir değildir, hemen döner. Böylece çağıran beklemez.
     */

    /**
     * Yerel veri kaynağındaki her şeyi silip, ağ veri kaynağından alınan tüm görevlerle değiştirir.
     *
     * `withContext` burada toplu `toLocal` dönüştürme işlemi karmaşık olabileceği için kullanılır.
     */

    override suspend fun refresh() {
        withContext(dispatcher) {
            val remoteTasks = networkDataSource.loadTasks()
            localDataSource.deleteAll()
            localDataSource.upsertAll(remoteTasks.toLocal())
        }
    }

    /**
     * Yerel veri kaynağındaki görevleri ağ veri kaynağına gönderir.
     *
     * Bu işlem başlatıldıktan hemen sonra döner. Gerçek uygulamalarda,
     * bu işlemin tamamlanmasını beklemek veya (daha iyisi) WorkManager kullanarak planlamak tercih edilir.
     * Her iki yöntem de hataların kullanıcıya bildirilmesini sağlamalıdır
     * (örneğin verilerinin yedeklenmediğini anlaması için).
     */

    private fun saveTasksToNetwork() {
        scope.launch {
            try {
                val localTasks = localDataSource.getAll()
                val networkTask = withContext(dispatcher) {
                    localTasks.toNetwork()
                }
            } catch (e: Exception) {
                // Gerçek bir uygulamada bu hatayı uygun şekilde ele almak gerekir,
                // örneğin bir `networkStatus` akışı (flow) üzerinden UI durumuna ileterek
                // kullanıcıya Toast mesajı göstermek gibi.
            }
        }
    }

}