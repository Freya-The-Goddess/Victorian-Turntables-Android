package au.edu.swin.sdmd.vic_turntables

import androidx.lifecycle.*
import kotlinx.coroutines.launch

//View model holds turntable data persistently through activity lifecycle
//And is primary way to interact with database
class TurntableViewModel(private val repository : TurntableRepository) : ViewModel() {
    val allTurntables : LiveData<List<TurntableEntity>> = repository.allTurntables.asLiveData()

    //start repository insert function in new thread
    fun insert(turntable : TurntableEntity) = viewModelScope.launch {
        repository.insert(turntable)
    }

    //start repository update function in new thread
    fun update(turntable : TurntableEntity) = viewModelScope.launch {
        repository.update(turntable)
    }

    //start repository update function in new thread
    fun delete(turntable : TurntableEntity) = viewModelScope.launch {
        repository.delete(turntable)
    }

    //start repository update function in new thread
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

//Creates turntable view model
class TurntableViewModelFactory(private val repository : TurntableRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if (modelClass.isAssignableFrom(TurntableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TurntableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}