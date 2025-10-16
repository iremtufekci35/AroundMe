package com.example.aroundme.utils

import com.google.firebase.auth.FirebaseAuthException

object CommonUtils {

    fun getFirebaseErrorMessage(e: Exception): String {
        return when (e) {
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                "Geçersiz kimlik bilgisi"
            }
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                "Kullanıcı bulunamadı ya da devre dışı bırakıldı."
            }
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                "Şifre çok zayıf, lütfen daha güçlü bir şifre seçin."
            }
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Geçersiz e-posta adresi"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Bu e-posta adresi zaten kayıtlı"
                    "ERROR_WRONG_PASSWORD" -> "Hatalı şifre girdiniz"
                    "ERROR_USER_NOT_FOUND" -> "Bu e-posta ile kayıtlı kullanıcı bulunamadı"
                    "ERROR_WEAK_PASSWORD" -> "Şifre çok zayıf, lütfen daha güçlü bir şifre seçin"
                    else -> "Bilinmeyen hata: ${e.message}"
                }
            }
            else -> "Bir hata oluştu: ${e.message}"
        }
    }
    fun String.toTitleCase(): String {
        return this.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}