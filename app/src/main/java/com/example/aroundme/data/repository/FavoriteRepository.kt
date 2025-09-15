package com.example.aroundme.data.repository

import com.example.aroundme.data.model.FavoriteItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class FavoriteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("favorites")

    fun addFavorite(
        userId: String,
        itemId: String,
        itemName: String,
        onComplete: (Boolean) -> Unit
    ) {
        val favorite = hashMapOf(
            "userId" to userId,
            "itemId" to itemId,
            "name" to itemName,
            "createdAt" to FieldValue.serverTimestamp()
        )

        collection.document("${userId}_$itemId")
            .set(favorite)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeFavorite(userId: String, itemId: String, onComplete: (Boolean) -> Unit) {
        collection.document("${userId}_$itemId")
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getFavorites(userId: String, onResult: (List<FavoriteItem>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val favorites = result.map { doc ->
                    FavoriteItem(
                        id = doc.getString("itemId") ?: "",
                        name = doc.getString("name") ?: ""
                    )
                }
                onResult(favorites)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun isFavorite(userId: String, itemId: String, onResult: (Boolean) -> Unit) {
        collection.document("${userId}_$itemId")
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.exists())
            }
            .addOnFailureListener { onResult(false) }
    }
}
