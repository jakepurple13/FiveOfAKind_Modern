package com.programmersbox.fiveofakind

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
fun main() {
    window.onkeydown = {
        keyEventFlow.tryEmit(
            KeyEvent(
                key = Key(keyCode = it.keyCode.toLong()),
                type = KeyEventType.KeyDown
            )
        )
    }

    ComposeViewport(document.body!!) {
        App(
            database = remember { YahtzeeDatabase() }
        )
    }
}