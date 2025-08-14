package com.example.elmnassri

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ItemRepository(private val itemDao: ItemDao) {

    // Get a reference to the online Firestore database (modern way)
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val itemsCollection = firestoreDb.collection("items")

    // Get a reference to Firebase Cloud Storage (modern way)
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun startRealtimeSync() {
        itemsCollection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e)
                return@addSnapshotListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                for (doc in snapshots!!.documents) {
                    val item = doc.toObject(Item::class.java)
                    item?.let {
                        itemDao.upsertItem(it)
                    }
                }
            }
        }
    }

    fun getItems(searchQuery: String): Flow<List<Item>> {
        return itemDao.getItems(searchQuery)
    }

    suspend fun upsertItem(item: Item, imageUri: Uri?) {
        var itemToSave = item

        if (imageUri != null) {
            val imageRef = storageRef.child("images/${item.barcode}_${System.currentTimeMillis()}.jpg")
            try {
                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await()
                itemToSave = item.copy(imageUri = downloadUrl.toString())
                Log.d("Firebase", "Image uploaded successfully: $downloadUrl")
            } catch (e: Exception) {
                Log.e("Firebase", "Image upload failed", e)
            }
        }

        itemsCollection.document(itemToSave.barcode).set(itemToSave)
            .addOnSuccessListener { Log.d("Firebase", "Item successfully written online!") }
            .addOnFailureListener { e -> Log.w("Firebase", "Error writing item online", e) }
    }

    suspend fun deleteItem(item: Item) {
        itemsCollection.document(item.barcode).delete()
        itemDao.deleteItem(item)
    }

    suspend fun getItemByBarcode(barcode: String): Item? {
        return itemDao.getItemByBarcode(barcode)
    }
}