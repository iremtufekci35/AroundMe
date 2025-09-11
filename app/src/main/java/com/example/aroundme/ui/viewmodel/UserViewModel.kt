package com.example.aroundme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aroundme.data.model.User
import com.example.aroundme.utils.CommonUtils
import com.example.aroundme.utils.CommonUtils.TAG
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _signUpUser = MutableStateFlow<User?>(null)
    val signUpUser: StateFlow<User?> = _signUpUser

    private val _signUpStatus = MutableStateFlow<String?>(null)
    val signUpStatus: StateFlow<String?> = _signUpStatus
    private val _loginUser = MutableStateFlow<User?>(null)
    val loginUser: StateFlow<User?> = _loginUser

    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus: StateFlow<String?> = _loginStatus

    fun loginUser(user: User) {
        viewModelScope.launch {
            try {
                if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
                    Log.d(TAG, "Login email and password not empty")
                    val result = withContext(Dispatchers.IO) {
                        auth.signInWithEmailAndPassword(user.email, user.password).await()
                    }
                    _loginUser.value = user
                    _loginStatus.value = "Giriş başarılı: ${result.user?.email}"
                } else {
                    _loginStatus.value = "Email ve şifre boş olamaz"
                }
            } catch (e: Exception) {
                Log.d(TAG, "login exception $e")
                _loginStatus.value = CommonUtils.getFirebaseErrorMessage(e)
            }
        }
    }

    fun signUpUser(user: User) {
        viewModelScope.launch {
            try {
                if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
                    Log.d(TAG, "Sign up email and password not empty")
                    val result = withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(user.email, user.password).await()
                    }
                    _signUpUser.value = user
                    _signUpStatus.value = "Kayıt başarılı: ${result.user?.email}"
                } else {
                    _signUpStatus.value = "Email ve şifre boş olamaz"
                }
            } catch (e: Exception) {
                Log.d(TAG, "Sign up exception $e")
                _signUpStatus.value = CommonUtils.getFirebaseErrorMessage(e)
            }
        }
    }
    fun clearLoginStatus() {
        _loginStatus.value = null
    }

    fun clearSignUpStatus() {
        _signUpStatus.value = null
    }
}