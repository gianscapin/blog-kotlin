package com.example.blogapp.data.remote.home

import com.example.blogapp.core.Result
import com.example.blogapp.data.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.Exception

class HomeScreenDataSource {

    suspend fun getLastestPosts(): Result<List<Post>>  {
        val postList = mutableListOf<Post>()

        withContext(Dispatchers.IO){
            val querySnapshot = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await()

            for (post in querySnapshot.documents) {
                post.toObject(Post::class.java)?.let { postFirebase ->

                    val isLiked = FirebaseAuth.getInstance().currentUser?.let { user ->
                        isPostLiked(post.id, user.uid)
                    }
                    postFirebase.apply {
                        createdAt = post.getTimestamp(
                            "createdAt",
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                            ?.toDate()
                        id = post.id
                        if(isLiked != null){
                            liked = isLiked
                        }
                    }
                    postList.add(postFirebase)

                }
            }
        }

        return Result.Success(postList)
    }

    /**
     * Método para el estado de los likes
     * @param postId es el id del post al que se va a likear/deslikear
     * @param liked si es positivo significa que le di like, de lo contrario dislike
     *
     * En la función runTransaction lo que ocurre es que se obtiene la cantidad de likes de la colección "posts"
     * Si es mayor a 0 y se likea, primero se verifica que haya una colección de likes del mismo post en "postLikes"
     * De ser así, se le agrega a esa colección el id del usuario que le dio like, de lo contrario
     * se crea la colección de likes.
     * A su vez se le aumenta en 1 la longitud de likes del post de la colección "posts".
     * Si es un dislike, se disminuye en 1 la longitud de likes de la colección "posts" y se remueve el id del usuario
     * que likeó el post de la colección "postsLikes"
     */
    fun registerLike(postId: String, liked: Boolean) {

        val increment = FieldValue.increment(1)
        val decrement = FieldValue.increment(-1)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
        val postsLikesRef = FirebaseFirestore.getInstance().collection("postsLikes").document(postId)

        val db = FirebaseFirestore.getInstance()

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likeCount = snapshot.getLong("likes")
            if (likeCount != null) {
                if(likeCount >= 0){
                    if(liked){
                        if(transaction.get(postsLikesRef).exists()){
                            transaction.update(postsLikesRef, "likes", FieldValue.arrayUnion(uid))
                        }else{
                            transaction.set(postsLikesRef, hashMapOf("likes" to arrayListOf(uid)), SetOptions.merge())
                        }
                        transaction.update(postRef, "likes", increment)
                    }else{
                        transaction.update(postRef, "likes", decrement)
                        transaction.update(postsLikesRef, "likes", FieldValue.arrayRemove(uid))
                    }
                }
            }
        }.addOnFailureListener {
            throw Exception(it.message)
        }
    }

    private suspend fun isPostLiked(postId: String, uid: String): Boolean{
        val posts = FirebaseFirestore.getInstance().collection("postsLikes").document(postId).get().await()
        if(!posts.exists()) return false
        val likeArray: List<String> = posts.get("likes") as List<String>

        return likeArray.contains(uid)
    }
}