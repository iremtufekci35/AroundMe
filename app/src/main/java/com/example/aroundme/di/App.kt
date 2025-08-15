package com.example.aroundme.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

/**
Created by Irem TUFEKCI on 14/08/2025
**/

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}