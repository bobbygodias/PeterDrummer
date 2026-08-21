package com.bobbydias.peterdrummer.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class RankingEntry(val name: String, val score: Int)

class RankingStore(context: Context) {
    private val preferences = context.getSharedPreferences("peter_drummer_ranking", Context.MODE_PRIVATE)

    fun topFive(): List<RankingEntry> {
        val encoded = preferences.getString(KEY_RANKING, "[]") ?: "[]"
        val array = runCatching { JSONArray(encoded) }.getOrElse { JSONArray() }
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(RankingEntry(item.optString("name", "Jogador"), item.optInt("score", 0)))
            }
        }.sortedByDescending { it.score }.take(5)
    }

    fun add(name: String, score: Int): List<RankingEntry> {
        val sanitizedName = name.trim().take(18).ifBlank { "Jogador" }
        val updated = (topFive() + RankingEntry(sanitizedName, score))
            .sortedByDescending { it.score }
            .take(5)
        val array = JSONArray()
        updated.forEach { entry ->
            array.put(JSONObject().put("name", entry.name).put("score", entry.score))
        }
        preferences.edit().putString(KEY_RANKING, array.toString()).apply()
        return updated
    }

    companion object {
        private const val KEY_RANKING = "top_five"
    }
}
