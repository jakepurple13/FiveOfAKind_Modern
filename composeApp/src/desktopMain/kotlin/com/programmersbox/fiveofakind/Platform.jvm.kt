package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme

class JVMPlatform: Platform {
    override val name: String = "Desktop Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    // Since dynamicColor is not used in JVM implementation, we can simplify this
    return rememberDynamicColorScheme(
        seedColor = Color.Blue,
        isDark = isDarkMode,
        isAmoled = false
    )
}
