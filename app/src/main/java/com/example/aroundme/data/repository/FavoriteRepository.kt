package com.example.aroundme.data.repository

import com.example.aroundme.data.model.FavoriteItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteRepository {

    private val database = FirebaseDatabase.getInstance()
        .getReference("favorites")

    fun addFavorite(userId: String, placeId: String, name: String, onComplete: (Boolean) -> Unit) {
        val favoriteData = mapOf(
            "placeId" to placeId,
            "name" to name,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(userId)
            .push()
            .setValue(favoriteData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getFavoritesByUserId(
        userId: String,
        onResult: (List<FavoriteItem>) -> Unit,
        onError: (DatabaseError) -> Unit = {}
    ) {
        database.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FavoriteItem>()
                    for (child in snapshot.children) {
                        child.getValue(FavoriteItem::class.java)?.let { list.add(it) }
                    }
                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error)
                }
            })
    }
}
