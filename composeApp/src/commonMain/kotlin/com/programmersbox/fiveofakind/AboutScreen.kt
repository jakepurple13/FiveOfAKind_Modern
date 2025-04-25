package com.programmersbox.fiveofakind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import fiveofakind.composeapp.generated.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val libraries by rememberLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }

    DrawerHandler(enabled = true, onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding
        ) {
            items(libraries?.libraries.orEmpty()) { Library(it) }
        }
    }
}

@Composable
private fun Library(
    item: Library,
) {
    OutlinedCard {
        ListItem(
            headlineContent = { Text(item.name) },
            supportingContent = { Text(item.description.orEmpty()) },
            overlineContent = { Text(item.artifactId) },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            )
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            item.tag?.let { Text(it) }
            FlowRow {
                item.licenses.forEach {
                    OutlinedCard {
                        Text(
                            it.name,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}