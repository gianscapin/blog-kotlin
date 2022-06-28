package com.example.blogapp.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class Post(
    @ServerTimestamp
    var createdAt: Date? = null,
    val postImage: String = "",
    val postDescription: String = "",
    @Exclude @JvmField
    var liked: Boolean = false,
    val likes: Long = 0,
    @Exclude @JvmField
    var id: String = "",
    val poster: Poster? = null

)

data class Poster(
    val username: String = "",
    val uid: String? = null,
    val profilePicture: String = ""
)