package com.example.blogapp.data.remote.home

import com.example.blogapp.core.Result
import com.example.blogapp.data.model.Post
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HomeScreenDataSource {

    suspend fun getLastestPosts(): Flow<Result<List<Post>>> = callbackFlow {
        val postList = mutableListOf<Post>()

        var postReference: Query? = null

        try {
            postReference = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await().query

        } catch (e: Throwable) {
            close(e)
        }

        val suscription = postReference?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener

            try {
                postList.clear()
                for (post in value.documents) {
                    post.toObject(Post::class.java)?.let { postFirebase ->
                        postFirebase.apply {
                            createdAt = post.getTimestamp(
                                "createdAt",
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                            )
                                ?.toDate()
                        }
                        postList.add(postFirebase)
                    }
                }
            } catch (e: Exception) {
                close(e)
            }

            trySend(Result.Success(postList)).isSuccess
        }

        awaitClose { suscription?.remove() }


    }
}