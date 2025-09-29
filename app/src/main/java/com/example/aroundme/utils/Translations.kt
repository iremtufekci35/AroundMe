package com.example.aroundme.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object Translations {

    fun translateText(input: String, onResult: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.TURKISH)
            .build()

        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(input)
                    .addOnSuccessListener { translated ->
                        onResult(translated)
                    }
                    .addOnFailureListener { e ->
                        onResult("Çeviri hatası: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onResult("Model indirilemedi: ${e.message}")
            }
    }
}
