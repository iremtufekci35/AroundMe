package com.example.aroundme.data.model

data class CommentItem(
    val userId: String? = null,
    val placeId: String? = null,
    val placeName: String? = null,
    val userComment: String? = null,
    val userName: String? = null,
    val rating: Int? = 0 ,
    val timestamp: Long? = null
)