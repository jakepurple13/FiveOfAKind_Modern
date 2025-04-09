package com.programmersbox.fiveofakind

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import androidx.room.RoomDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            remember {  Settings { filesDir.resolve("settings.preferences_pb").absolutePath } }
            App(
                database = remember { YahtzeeDatabase(getDatabaseBuilder(this@MainActivity)) }
            )
        }
    }
}

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(YahtzeeDatabase(getDatabaseBuilder(LocalContext.current)))
}