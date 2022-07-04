package com.example.blogapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.blogapp.core.Result
import com.example.blogapp.data.model.Post
import com.example.blogapp.domain.home.HomeScreenRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.Exception

class HomeScreenViewModel(private val repo: HomeScreenRepo) : ViewModel() {
    fun fetchLatestPosts() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getLatestPosts()
        }.onSuccess { posts ->
            emit(posts)
        }.onFailure { throwable ->
            emit(Result.Failure(Exception(throwable.message)))
        }
    }

    fun registerLikeButtonState(postId: String, liked: Boolean) =
        liveData(viewModelScope.coroutineContext + Dispatchers.Main) {
            emit(Result.Loading())
            kotlin.runCatching {
                repo.registerLike(postId, liked)
            }.onSuccess{
                emit(Result.Success(Unit))
            }.onFailure {
                emit(Result.Failure(Exception(it.message)))
            }
        }

    val latestPosts: StateFlow<Result<List<Post>>> = flow {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getLatestPosts()
        }.onSuccess { posts ->
            emit(posts)
        }.onFailure { throwable ->
            emit(Result.Failure(Exception(throwable.message)))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.Loading()
    )
}

class HomeScreenViewModelFactory(private val repo: HomeScreenRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(HomeScreenRepo::class.java).newInstance(repo)
    }

}