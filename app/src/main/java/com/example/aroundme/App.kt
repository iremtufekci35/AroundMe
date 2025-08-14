package com.example.aroundme

import android.app.Application
import org.osmdroid.config.Configuration

/**
* Created by İrem TÜFEKCİ on 14/08/2025.
**/

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}
