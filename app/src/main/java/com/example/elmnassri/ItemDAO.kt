package com.example.elmnassri

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    /**
     * Upsert is a mix of "update" and "insert".
     * If the item already exists, it's updated. If not, it's inserted.
     */
    @Upsert
    suspend fun upsertItem(item: Item)

    /**
     * Deletes a specific item from the database.
     */
    @Delete
    suspend fun deleteItem(item: Item)

    /**
     * Gets all items from the table, ordered by name.
     * Flow is used so the UI can automatically update when the data changes.
     */
    @Query("SELECT * FROM items_table WHERE item_name LIKE '%' || :searchQuery || '%' ORDER BY item_name ASC")
    fun getItems(searchQuery: String): Flow<List<Item>>

    /**
     * Finds a single item by its barcode.
     */
    @Query("SELECT * FROM items_table WHERE barcode_data = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): Item?

    /**
     * Clears the entire local database. Useful for re-syncing from the cloud.
     */
    @Query("DELETE FROM items_table")
    suspend fun clearAllItems()
}