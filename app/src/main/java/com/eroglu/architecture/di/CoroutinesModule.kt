package com.eroglu.android_architecture.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// Yani bu sınıf Hilt’e şunu söyler: “Ben uygulamanın her yerinde kullanılacak coroutine’lerle ilgili bağımlılıkları (Dispatcher, Scope) sağlayacağım.”

// di/DataModules.kt ve di/CoroutinesModule.kt: "Yapıştırıcı" (Hilt)
// Bu dosyalar, Hilt'e (Dependency Injection) hangi parçayı nasıl oluşturacağını söyleyen "talimat" dosyalarıdır.

/**
 * -----------------------------
 * 1️⃣ Qualifier Annotation’ları
 * -----------------------------
 * Bu annotation’lar, Hilt’e hangi tür dependency’in hangi yerde kullanılacağını söyler.
 * Çünkü CoroutineDispatcher farklı türlerde olabilir (IO, Default vb.).
 */

// Qualifier annotation

// IO işlemleri için Dispatcher
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

// Default dispatcher (CPU yoğun işleri için)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

// Uygulama scope’u (Singleton Scope)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

/**
 * -----------------------------
 * 2️⃣ Hilt Module
 * -----------------------------
 * Bu modül, uygulama genelinde kullanılacak Coroutine Dispatcher ve Scope sağlayacak.
 */

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    // Sağlanan bağımlılıklar (Providers)
    // IO Dispatcher sağlanıyor
    @Provides
    @IoDispatcher // “Birisi @IoDispatcher isterse, ona Dispatchers.IO ver.”
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    // Dispatchers.IO -> IO işlemleri için optimize edilmiş thread pool

    // Default Dispatcher sağlanıyor
    @Provides
    @DefaultDispatcher // “Birisi @DefaultDispatcher isterse, Dispatchers.Default ver.”
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    // Dispatchers.Default -> CPU yoğun işlemler için optimize edilmiş dispatcher

    // CoroutineScope sağlanıyor, Singleton olarak uygulama boyunca kullanılacak
    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher // Default dispatcher inject ediliyor
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    // SupervisorJob -> Hiyerarşik Coroutine yapılandırmasında bir job’un failure’ı diğerlerini bozmaz
    /*
    Bu da uygulama genelinde tek bir coroutine scope oluşturur.
    SupervisorJob() → alt coroutinelerden biri hata alsa bile diğerleri devam eder.
    @Singleton → tek bir örnek olur (tüm uygulamada aynı scope).
    @ApplicationScope → özel etiket (Qualifier), Hilt’in nereye enjekte edeceğini anlaması için.
    Bu scope genellikle “arka planda sessizce” çalışan görevler (ör. veritabanı temizliği, cache güncellemesi) için kullanılır.
     */
}