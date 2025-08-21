package com.example.livechat.data

import android.content.Context
import android.content.SharedPreferences
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class FavoritesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveFavorites(favorites: List<String>) {
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString("favorites", json).apply()
    }

    fun loadFavorites(): List<String> {
        val json = sharedPreferences.getString("favorites", null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
}