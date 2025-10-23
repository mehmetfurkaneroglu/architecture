package com.eroglu.architecture.data.source.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * Bu sınıf, uygulamadaki "NetworkDataSource" arayüzünü (interface) implement eder.
 * Gerçek bir ağ (network) isteği yapmaz; bunun yerine sahte (mock) veriler döndürür.
 * Yani bu, uygulamanın ağ katmanını taklit eden bir test/örnek veri kaynağıdır.
 */
class TaskNetworkDataSource @Inject constructor() : NetworkDataSource {
    // TaskNetworkDataSource: Ağ üzerinden veri sağlayan sınıfın sahte (mock) versiyonu
    // NetworkDataSource: Uygulamanın veri erişim arayüzü (interface)
    // Mutex: Eşzamanlı işlemlerde veri tutarlılığını korur
    // Mutex (karşılıklı dışlama kilidi), aynı anda birden fazla thread’in
    // veriye erişmesini engeller. Thread-safe işlem yapılmasını sağlar.
    private val accessMutex = Mutex()

    // Bunlar, “uzaktan geliyormuş” gibi davranılan fake veri kayıtları.
    private var tasks = listOf(
        NetworkTask(
            id = "PISA",
            title = "Build tower in Pisa",
            shortDescription = "Ground looks good, no foundation work required."
        ),
        NetworkTask(
            id = "TACOMA",
            title = "Finish bridge in Tacoma",
            shortDescription = "Found awesome girders at half the cost!"
        )
    )

    /**
     * Bu fonksiyon “ağdan görevleri yükle” işlemini simüle eder.
     * - 'delay' ile sahte bir gecikme eklenir (örneğin 2 saniye bekleme).
     * - 'withLock' ile Mutex üzerinden kilitlenir, thread-safe hale gelir.
     * - Ardından mevcut 'tasks' listesi döndürülür.
     */
    override suspend fun loadTasks(): List<NetworkTask> {
        delay(SERVICE_LATENCY_IN_MILLIS) // Ağ gecikmesi similasyonu
        return tasks // Görev listesini döndür
    }

    /**
     * Bu fonksiyon “ağ üzerinden görevleri kaydet” işlemini taklit eder.
     * - Yine gecikme ekler (ağ yanıt süresi gibi).
     * - Verilen 'newTasks' listesini mevcut 'tasks' listesiyle değiştirir.
     * - Mutex ile kilitlenerek veri bütünlüğü korunur.
     */
    override suspend fun saveTasks(newTasks: List<NetworkTask>) = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        tasks = newTasks // Yeni görev listesini kaydet
    }

}

// Ağ yanıt süresini taklit eden sabit gecikme süresi (2 saniye)
private const val SERVICE_LATENCY_IN_MILLIS = 2000L