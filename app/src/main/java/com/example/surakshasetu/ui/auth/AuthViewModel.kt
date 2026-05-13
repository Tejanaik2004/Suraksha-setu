package com.example.surakshasetu.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surakshasetu.data.model.User
import com.example.surakshasetu.data.model.TrustedContact
import com.example.surakshasetu.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    val currentUser = repository.currentUser

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                _userProfile.value = repository.getUserDetails(uid)
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signIn(email, pass)
                fetchUserProfile()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login Failed")
            }
        }
    }

    fun signup(email: String, pass: String, name: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signUp(email, pass, name, phone)
                fetchUserProfile()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup Failed")
            }
        }
    }

    fun updateProfile(user: User, imageUri: Uri? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.updateUserProfile(user, imageUri)
                fetchUserProfile()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Update Failed")
            }
        }
    }

    fun addContact(contact: TrustedContact) {
        viewModelScope.launch {
            try {
                repository.addTrustedContact(contact)
                fetchUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeContact(contact: TrustedContact) {
        viewModelScope.launch {
            try {
                repository.removeTrustedContact(contact)
                fetchUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        repository.signOut()
        _userProfile.value = null
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
