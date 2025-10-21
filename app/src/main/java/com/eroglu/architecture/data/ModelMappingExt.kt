package com.eroglu.architecture.data

import com.eroglu.architecture.data.source.local.LocalTask
import com.eroglu.architecture.data.source.network.NetworkTask
import com.eroglu.architecture.data.source.network.TaskStatus

/**
 * Veri modeli dönüştürme (mapping) uzantı fonksiyonları. Üç model türü vardır:
 *
 * - Task: Mimarinin diğer katmanlarına sunulan dış (external) model.
 *   `toExternal` kullanılarak elde edilir.
 *
 * - NetworkTask: Ağdan gelen bir görevi temsil eden dahili model.
 *   `toNetwork` kullanılarak elde edilir.
 *
 * - LocalTask: Veritabanında yerel olarak depolanan bir görevi temsil eden dahili model.
 *   `toLocal` kullanılarak elde edilir.
 *
 */

// Dış modelden yerel modele dönüştürme
fun Task.toLocal() = LocalTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

fun List<Task>.toLocal() = map(Task::toLocal)

// Yerel modelden dış modele dönüştürme
fun LocalTask.toExternal() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

// Not: JvmName, aynı ada sahip birden fazla uzantı fonksiyonuna JVM üzerinde
// benzersiz bir ad sağlamak için kullanılır.
// Aksi halde, tip silinmesi (type erasure) nedeniyle bu metotlar JVM üzerinde
// aynı imzaya sahip olur ve derleyici hatası oluşur.
@JvmName("localToExternal")
fun List<LocalTask>.toExternal() = map(LocalTask::toExternal)

// Ağ modelinden yerel modele dönüştürme
fun NetworkTask.toLocal() = LocalTask(
    id = id,
    title = title,
    description = shortDescription,
    isCompleted = (status == TaskStatus.COMPLETE),
)

@JvmName("networkToLocal")
fun List<NetworkTask>.toLocal() = map(NetworkTask::toLocal)

// Yerel modelden ağ modeline dönüştürme
fun LocalTask.toNetwork() = NetworkTask(
    id = id,
    title = title,
    shortDescription = description,
    status = if (isCompleted) { TaskStatus.COMPLETE } else { TaskStatus.ACTIVE },
)

fun List<LocalTask>.toNetwork() = map(LocalTask::toNetwork)

// Dış modelden ağ modeline dönüştürme
fun Task.toNetwork() = toLocal().toNetwork()

@JvmName("externalToNetwork")
fun List<Task>.toNetwork() = map(Task::toNetwork)

// Ağ modelinden dış modele dönüştürme
fun NetworkTask.toExternal() = toLocal().toExternal()

@JvmName("networkToExternal")
fun List<NetworkTask>.toExternal() = map(NetworkTask::toExternal)

