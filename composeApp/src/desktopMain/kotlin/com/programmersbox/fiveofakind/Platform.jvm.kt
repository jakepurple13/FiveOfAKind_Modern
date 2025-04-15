package com.programmersbox.fiveofakind

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.lifecycle.viewModelScope
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

val keyEventFlow = MutableSharedFlow<KeyEvent>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

internal actual fun YahtzeeViewModel.setup() {
    keyEventFlow
        .onEach { keyEvent(it) }
        .launchIn(viewModelScope)
}