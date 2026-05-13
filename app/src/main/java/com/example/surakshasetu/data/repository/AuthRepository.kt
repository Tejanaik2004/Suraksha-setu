package com.example.surakshasetu.data.repository

import android.net.Uri
import com.example.surakshasetu.data.model.TrustedContact
import com.example.surakshasetu.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    val currentUser get() = auth.currentUser

    suspend fun signIn(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun signUp(email: String, pass: String, name: String, phone: String) {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val firebaseUser = result.user
        
        // Update Firebase Auth profile with the name
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        firebaseUser?.updateProfile(profileUpdates)?.await()

        val user = User(
            uid = firebaseUser?.uid ?: "",
            name = name,
            email = email,
            phone = phone
        )
        firebaseUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).set(user).await()
        }
    }

    suspend fun updateUserProfile(user: User, imageUri: Uri? = null) {
        val uid = auth.currentUser?.uid ?: return
        
        var finalUser = user
        
        imageUri?.let {
            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            ref.putFile(it).await()
            val url = ref.downloadUrl.await().toString()
            finalUser = user.copy(profilePictureUrl = url)
            
            val profileUpdates = userProfileChangeRequest {
                photoUri = Uri.parse(url)
            }
            auth.currentUser?.updateProfile(profileUpdates)?.await()
        }

        firestore.collection("users").document(uid).set(finalUser).await()
    }

    suspend fun addTrustedContact(contact: TrustedContact) {
        val uid = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(uid).get().await()
        val user = userDoc.toObject(User::class.java) ?: return
        
        val updatedContacts = user.trustedContacts + contact
        firestore.collection("users").document(uid).update("trustedContacts", updatedContacts).await()
    }

    suspend fun removeTrustedContact(contact: TrustedContact) {
        val uid = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(uid).get().await()
        val user = userDoc.toObject(User::class.java) ?: return
        
        val updatedContacts = user.trustedContacts - contact
        firestore.collection("users").document(uid).update("trustedContacts", updatedContacts).await()
    }

    fun signOut() = auth.signOut()

    suspend fun getUserDetails(uid: String): User? {
        return firestore.collection("users").document(uid).get().await().toObject(User::class.java)
    }
}
