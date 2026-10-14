package com.example

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

class ChequeManagerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: Context) {
        val config = Configuration(base.resources.configuration)
        config.fontScale = 1.0f
        val fixedContext = base.createConfigurationContext(config)
        super.attachBaseContext(fixedContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val config = Configuration(newConfig)
        config.fontScale = 1.0f
        super.onConfigurationChanged(config)
    }

    override fun onCreate() {
        val config = resources.configuration
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        super.onCreate()
        container = DefaultAppContainer(this)
        com.example.data.local.DatabaseSeeder.seedIfEmpty(container.database)
    }
}
