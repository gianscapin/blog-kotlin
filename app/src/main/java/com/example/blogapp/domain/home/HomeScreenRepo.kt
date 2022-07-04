package com.example.blogapp.domain.home

import com.example.blogapp.core.Result
import com.example.blogapp.data.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface HomeScreenRepo {
    suspend fun getLatestPosts(): Result<List<Post>>
    suspend fun registerLike(postId: String, liked: Boolean)
}