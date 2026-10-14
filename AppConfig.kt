package com.example.core.common

/**
 * پیکربندی و اطلاعات سراسری برنامه مانند نسخه برنامه
 */
object AppConfig {
    const val APP_VERSION = "1.0.0"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    fun getFormattedVersion(): String = "نسخه $APP_VERSION"
}
