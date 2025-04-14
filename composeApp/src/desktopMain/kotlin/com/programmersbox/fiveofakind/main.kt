package com.programmersbox.fiveofakind

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import java.io.File

fun main() = application {
    remember { Settings { "settings.preferences_pb" } }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Five Of A Kind",
    ) {
        DevelopmentEntryPoint {
            App(
                database = remember { YahtzeeDatabase(getDatabaseBuilder()) }
            )
        }
    }
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "yahtzee_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
}
