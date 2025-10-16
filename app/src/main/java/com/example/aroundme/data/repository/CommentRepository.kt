package com.example.aroundme.data.repository

import com.example.aroundme.data.model.CommentItem
import com.google.firebase.database.FirebaseDatabase

class CommentRepository {

    private val database = FirebaseDatabase.getInstance()
        .getReference("comments")

    fun addComment(
        userId: String,
        placeId: String,
        placeName: String,
        userName: String,
        userComment: String,
        rating: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val userCommentData = CommentItem(
            userId = userId,
            placeId = placeId,
            placeName = placeName,
            userComment = userComment,
            userName = userName,
            rating = rating,
            timestamp = System.currentTimeMillis()
        )

        database.child(userId)
            .push()
            .setValue(userCommentData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    fun getComments(placeId: String, callback: (List<CommentItem>) -> Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<CommentItem>()
            snapshot.children.forEach { userNode ->
                userNode.children.forEach { commentNode ->
                    val comment = commentNode.getValue(CommentItem::class.java)
                    if (comment?.placeId == placeId) {
                        list.add(comment)
                    }
                }
            }
            callback(list)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }
}
