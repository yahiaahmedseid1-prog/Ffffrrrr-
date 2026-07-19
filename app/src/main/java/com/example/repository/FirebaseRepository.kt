package com.example.repository

import android.util.Log
import com.example.model.Anime
import com.example.model.Episode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance()
    private val animeRef = database.getReference("Anime")

    fun getAnimesFlow(): Flow<List<Anime>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val animeList = mutableListOf<Anime>()
                for (child in snapshot.children) {
                    val id = child.key ?: ""
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val image = child.child("image").getValue(String::class.java) ?: ""
                    val category = child.child("category").getValue(String::class.java) ?: ""
                    val year = when (val rawYear = child.child("year").value) {
                        is String -> rawYear
                        is Long -> rawYear.toString()
                        is Double -> rawYear.toLong().toString()
                        else -> ""
                    }
                    val status = child.child("status").getValue(String::class.java) ?: "مستمر"

                    // Parse episodes map
                    val episodesMap = mutableMapOf<String, Episode>()
                    val episodesSnapshot = child.child("episodes")
                    for (epChild in episodesSnapshot.children) {
                        val epId = epChild.key ?: ""
                        val epNum = when (val rawNum = epChild.child("number").value) {
                            is String -> rawNum
                            is Long -> rawNum.toString()
                            is Double -> rawNum.toLong().toString()
                            else -> ""
                        }
                        val epTitle = epChild.child("title").getValue(String::class.java) ?: ""
                        val epVideo = epChild.child("video").getValue(String::class.java) ?: ""
                        val epDuration = epChild.child("duration").getValue(String::class.java) ?: ""
                        
                        episodesMap[epId] = Episode(
                            id = epId,
                            number = epNum,
                            title = epTitle,
                            video = epVideo,
                            duration = epDuration
                        )
                    }

                    animeList.add(
                        Anime(
                            id = id,
                            title = title,
                            description = description,
                            image = image,
                            category = category,
                            year = year,
                            status = status,
                            episodes = episodesMap
                        )
                    )
                }
                trySend(animeList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Database error: ${error.message}")
            }
        }

        animeRef.addValueEventListener(listener)
        awaitClose { animeRef.removeEventListener(listener) }
    }
}
