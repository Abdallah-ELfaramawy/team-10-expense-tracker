package com.example.expensetracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Stores the user's preferred currency with Preferences DataStore. */
class SettingsStore(private val context: Context) {
    private val currencyKey = stringPreferencesKey("currency")

    val currency: Flow<String> =
        context.dataStore.data.map { it[currencyKey] ?: "EGP" }

    suspend fun setCurrency(value: String) {
        context.dataStore.edit { it[currencyKey] = value }
    }
}

val currencies = listOf("EGP", "USD", "EUR", "SAR")
