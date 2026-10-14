package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.presentation.ui.ChequeApp
import com.example.presentation.viewmodel.ChequeViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.clearFocusOnTap

class MainActivity : ComponentActivity() {

    private val viewModel: ChequeViewModel by viewModels {
        val appContainer = (application as ChequeManagerApplication).container
        ChequeViewModel.provideFactory(
            appContainer.chequeRepository,
            appContainer.chequeBookRepository,
            appContainer.bankAccountRepository,
            appContainer.counterpartyRepository
        )
    }

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val fixedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(fixedContext)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.fontScale = 1.0f
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun getResources(): android.content.res.Resources {
        val res = super.getResources()
        if (res.configuration.fontScale != 1.0f) {
            val config = Configuration(res.configuration)
            config.fontScale = 1.0f
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            return createConfigurationContext(config).resources
        }
        return res
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val config = Configuration(newConfig)
        config.fontScale = 1.0f
        super.onConfigurationChanged(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val config = resources.configuration
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentDensity = LocalDensity.current
            val fixedFontDensity = remember(currentDensity.density) {
                Density(density = currentDensity.density, fontScale = 1.0f)
            }
            val currentConfig = LocalConfiguration.current
            val fixedConfig = remember(currentConfig) {
                Configuration(currentConfig).apply { fontScale = 1.0f }
            }
            CompositionLocalProvider(
                LocalConfiguration provides fixedConfig,
                LocalDensity provides fixedFontDensity,
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                MyApplicationTheme {
                    val focusManager = LocalFocusManager.current
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearFocusOnTap(focusManager)
                    ) {
                        ChequeApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
