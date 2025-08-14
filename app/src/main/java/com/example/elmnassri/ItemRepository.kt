package com.example.elmnassri

import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    /**
     * This now passes the search query from the ViewModel to the DAO
     */
    fun getItems(searchQuery: String): Flow<List<Item>> {
        return itemDao.getItems(searchQuery)
    }

    /**
     * Saves or updates an item directly in the local Room database.
     */
    suspend fun upsertItem(item: Item) {
        itemDao.upsertItem(item)
    }

    /**
     * Deletes an item from the local Room database.
     */
    suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item)
    }

    /**
     * Finds a single item by its barcode in the local database.
     */
    suspend fun getItemByBarcode(barcode: String): Item? {
        return itemDao.getItemByBarcode(barcode)
    }
}