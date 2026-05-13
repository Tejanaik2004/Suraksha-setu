package com.example.surakshasetu.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun triggerSos(uid: String, name: String, lat: Double, lng: Double) {
        val alert = hashMapOf(
            "uid" to uid,
            "userName" to name,
            "lat" to lat,
            "lng" to lng,
            "timestamp" to System.currentTimeMillis(),
            "status" to "SENT"
        )
        firestore.collection("alerts").add(alert).await()
    }

    suspend fun uploadSosAudio(uid: String, audioFile: File) {
        val ref = storage.reference.child("sos_audio/$uid/${System.currentTimeMillis()}.3gp")
        ref.putFile(android.net.Uri.fromFile(audioFile)).await()
    }
}
