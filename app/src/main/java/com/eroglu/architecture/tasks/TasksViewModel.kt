package com.eroglu.architecture.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eroglu.architecture.ADD_EDIT_RESULT_OK
import com.eroglu.architecture.DELETE_RESULT_OK
import com.eroglu.architecture.EDIT_RESULT_OK
import com.eroglu.architecture.R
import com.eroglu.architecture.data.Task
import com.eroglu.architecture.data.TaskRepository
import com.eroglu.architecture.tasks.TasksFilterType.ACTIVE_TASKS
import com.eroglu.architecture.tasks.TasksFilterType.ALL_TASKS
import com.eroglu.architecture.tasks.TasksFilterType.COMPLETED_TASKS
import com.eroglu.architecture.util.Async
import com.eroglu.architecture.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State (Durum) için data class
data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(TASKS_FILTER_SAVED_STATE_KEY, ALL_TASKS)

    private val _filterUiInfo = _savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)

    // REAKTİF KISIM: Repository'den gelen görev akışı (`getTasksStream`) ile seçili filtre (`_savedFilterType`) birleştiriliyor (`combine`).
    private val _filteredTasksAsync =
        combine(taskRepository.getTasksStream(), _savedFilterType) { tasks, type ->
            filterTasks(tasks, type)
        }
            .map { Async.Success(it) }
            .catch<Async<List<Task>>> { emit(Async.Error(R.string.loading_tasks_error)) }

    // Ana UI State: Diğer tüm akışlar (`Flow`) burada birleştirilerek UI için tek bir `StateFlow<TasksUiState>` oluşturuluyor.
    val uiState: StateFlow<TasksUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredTasksAsync
    ) { filterUiInfo, isLoading, userMessage, tasksAsync ->
        when (tasksAsync) {
            Async.Loading -> {
                TasksUiState(isLoading = true)
            }

            is Async.Error -> {
                TasksUiState(userMessage = tasksAsync.errorMessage)
            }

            is Async.Success -> {
                TasksUiState(
                    items = tasksAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed, // 5 saniye gecikmeli durdurma
            initialValue = TasksUiState(isLoading = true)
        )

    // EVENT: Kullanıcı filtre değiştirdiğinde bu fonksiyon çağrılır.
    fun setFiltering(requestType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
    }

    // EVENT: Tamamlananları temizle
    fun clearCompletedTasks() {
        viewModelScope.launch {
            taskRepository.clearCompletedTasks()
            showSnackbarMessage(R.string.completed_tasks_cleared)
            refresh() // Listeyi yenile
        }
    }

    // EVENT: Bir görevi tamamlama veya aktif etme
    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            taskRepository.completeTask(task.id)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(task.id)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    // EVENT: Başka bir ekrandan (örn: AddEditTask) dönüldüğünde mesaj gösterme
    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    // EVENT: Ekranı yenile (swipe-to-refresh)
    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            taskRepository.refresh() // Repository'ye "ağdan veri çek" komutu
            _isLoading.value = false
        }
    }

    // Görevleri seçilen filtreye göre filtreleyen özel fonksiyon
    private fun filterTasks(tasks: List<Task>, filteringType: TasksFilterType): List<Task> {
        val tasksToShow = ArrayList<Task>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                ALL_TASKS -> tasksToShow.add(task)
                ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }

                COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    // Filtreye göre UI metinlerini belirleyen fonksiyon
    private fun getFilterUiInfo(requestType: TasksFilterType): FilteringUiInfo =
        when (requestType) {
            ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )
            }

            ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )
            }

            COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }
}

// SavedStateHandle için anahtar
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

// Filtreleme ile ilgili UI bilgilerini tutan data class
data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)
