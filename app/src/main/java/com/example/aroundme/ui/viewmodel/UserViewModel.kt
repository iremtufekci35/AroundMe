package com.example.aroundme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aroundme.data.model.User
import com.example.aroundme.utils.CommonUtils
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _signUpUser = MutableStateFlow<User?>(null)
    private val _signUpStatus = MutableStateFlow<String?>(null)
    val signUpStatus: StateFlow<String?> = _signUpStatus

    private val _loginUser = MutableStateFlow<User?>(null)
    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus: StateFlow<String?> = _loginStatus

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isUserLoggedIn.value = firebaseAuth.currentUser != null
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun loginUser(user: User) {
        viewModelScope.launch {
            try {
                if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
                    val result = auth.signInWithEmailAndPassword(user.email, user.password).await()
                    _loginUser.value = user
                    _loginStatus.value = "Giriş başarılı: ${result.user?.email}"
                    _isUserLoggedIn.value = true
                } else {
                    _loginStatus.value = "Email ve şifre boş olamaz"
                }
            } catch (e: Exception) {
                _loginStatus.value = CommonUtils.getFirebaseErrorMessage(e)
            }
        }
    }

    fun signUpUser(user: User) {
        viewModelScope.launch {
            try {
                if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
                    val result = auth.createUserWithEmailAndPassword(user.email, user.password).await()
                    _signUpUser.value = user
                    _signUpStatus.value = "Kayıt başarılı: ${result.user?.email}"
                    _isUserLoggedIn.value = true
                } else {
                    _signUpStatus.value = "Email ve şifre boş olamaz"
                }
            } catch (e: Exception) {
                _signUpStatus.value = CommonUtils.getFirebaseErrorMessage(e)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _loginUser.value = null
                _signUpUser.value = null
                _loginStatus.value = "Çıkış yapıldı"
                _isUserLoggedIn.value = false
            } catch (e: Exception) {
                _loginStatus.value = CommonUtils.getFirebaseErrorMessage(e)
            }
        }
    }

    fun clearLoginStatus() { _loginStatus.value = null }
    fun clearSignUpStatus() { _signUpStatus.value = null }
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
