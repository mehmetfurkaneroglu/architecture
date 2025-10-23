package com.eroglu.architecture.data.source.network

interface NetworkDataSource {
    suspend fun loadTasks(): List<NetworkTask>
    suspend fun saveTasks(tasks: List<NetworkTask>)
}