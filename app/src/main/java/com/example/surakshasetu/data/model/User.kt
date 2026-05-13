package com.example.surakshasetu.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "",
    val bloodGroup: String = "",
    val height: String = "",
    val weight: String = "",
    val emergencyNotes: String = "",
    val profilePictureUrl: String = "",
    val isResponder: Boolean = false,
    val trustedContacts: List<TrustedContact> = emptyList()
)

data class TrustedContact(
    val name: String = "",
    val phone: String = ""
)
