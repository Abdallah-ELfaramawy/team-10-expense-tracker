package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeById(id: Long): Flow<Expense?>

    // Upsert = Create (new id) + Update (existing id)
    @Upsert
    suspend fun upsert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
