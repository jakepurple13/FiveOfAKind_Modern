package com.programmersbox.fiveofakind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.awt.Cursor
import java.io.File

fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Five Of A Kind",
        onKeyEvent = {
            keyEventFlow.tryEmit(it)
            false
        },
        state = windowState,
        undecorated = true,
        transparent = true,
    ) {
        MaterialTheme(
            colorScheme = buildColorScheme(isSystemInDarkTheme())
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CustomTitleBar(
                        title = "Five of a Kind",
                        onMinimizeClick = { windowState.isMinimized = true },
                        onCloseClick = ::exitApplication
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    remember { Settings { "settings.preferences_pb" } }
                    App(
                        database = remember { YahtzeeDatabase(getDatabaseBuilder()) }
                    )
                }
            }
        }
    }
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "yahtzee_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameWindowScope.CustomTitleBar(
    title: String,
    onMinimizeClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    WindowDraggableArea {
        TopAppBar(
            title = { Text(title) },
            actions = {
                IconButton(
                    onClick = onMinimizeClick,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Minimize",
                    )
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }
        )
    }
}
