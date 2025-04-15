package com.programmersbox.fiveofakind

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Five Of A Kind",
        onKeyEvent = {
            keyEventFlow.tryEmit(it)
            false
        }
    ) {
        remember { Settings { "settings.preferences_pb" } }
        App(
            database = remember { YahtzeeDatabase(getDatabaseBuilder()) }
        )
    }
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "yahtzee_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
}
