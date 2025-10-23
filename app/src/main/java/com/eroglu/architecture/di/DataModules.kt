package com.eroglu.android_architecture.di

import android.content.Context
import androidx.room.Room
import com.eroglu.architecture.data.DefaultTaskRepository
import com.eroglu.architecture.data.TaskRepository
import com.eroglu.architecture.data.source.local.TaskDao
import com.eroglu.architecture.data.source.local.ToDoDatabase
import com.eroglu.architecture.data.source.network.NetworkDataSource
import com.eroglu.architecture.data.source.network.TaskNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt, TaskRepository’ye ihtiyaç duyulan her yerde DefaultTaskRepository örneğini otomatik verir.
 */

// di/DataModules.kt ve di/CoroutinesModule.kt: "Yapıştırıcı" (Hilt)
// Bu dosyalar, Hilt'e (Dependency Injection) hangi parçayı nasıl oluşturacağını söyleyen "talimat" dosyalarıdır.

@Module
@InstallIn(SingletonComponent::class) // @InstallIn(SingletonComponent::class) → Bu modül, uygulamanın en üst seviyesine (Application) yüklenir.
abstract class RepositoryModule {

    @Singleton // @Singleton → Tüm uygulama boyunca tek bir DefaultTaskRepository nesnesi oluşturulacak.
    @Binds // @Binds → Hilt’e der ki: “Bir yere TaskRepository istenirse, DefaultTaskRepository nesnesini ver.”
    abstract fun bindTaskRepository(repository: DefaultTaskRepository): TaskRepository
}

/**
 * Hilt, NetworkDataSource ihtiyacında TaskNetworkDataSource’u inject eder.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton // @Singleton → Bu TaskNetworkDataSource da tek bir instance olarak kullanılır.
    @Binds // @Binds → Hilt’e der ki: “Bir yere NetworkDataSource istenirse, TaskNetworkDataSource nesnesini ver.”
    abstract fun bindNetworkDataSource(dataSource: TaskNetworkDataSource): NetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Room Database sağlanıyor
    @Singleton // @Singleton → Veritabanı tek bir instance olarak oluşturulur.
    @Provides // @Provides → Bu sefer @Binds değil, çünkü Room.databaseBuilder() ile elle nesne oluşturuluyor.
    fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext, // @ApplicationContext → Uygulama context’i enjekte edilir.
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }

    // DAO (Data Access Object) sağlanıyor
    @Provides
    fun provideTaskDao(database: ToDoDatabase): TaskDao = database.taskDao()
}
