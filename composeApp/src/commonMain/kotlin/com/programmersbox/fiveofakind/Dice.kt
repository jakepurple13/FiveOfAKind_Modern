package com.programmersbox.fiveofakind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.random.nextInt

class Dice(value: Int = Random.nextInt(1..6), @Suppress("unused") val location: String) {
    var value by mutableIntStateOf(value)

    suspend fun roll(rollCount: Int = 5) {
        randomNumberAnimation(
            newValue = Random.nextInt(1..6),
            valueChange = { value = it },
            randomCount = rollCount
        )
    }

    @Composable
    fun ShowDice(
        useDots: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(7.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            enabled = value != 0,
            border = BorderStroke(1.dp, contentColorFor(MaterialTheme.colorScheme.surface)),
            modifier = modifier.size(56.dp)
        ) {
            if (useDots) {
                DiceDotsPattern(value)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (value == 0) "" else value.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Operator function as a shorthand for ShowDice
    @Composable
    operator fun invoke(
        useDots: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
    ) = ShowDice(useDots, modifier, onClick)

    override fun toString(): String {
        return "Dice(value=$value, location='$location')"
    }
}

@Composable
internal fun Dice(dice: Dice, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        enabled = dice.value != 0,
        border = BorderStroke(1.dp, contentColorFor(MaterialTheme.colorScheme.surface)),
        modifier = modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (dice.value == 0) "" else dice.value.toString(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun DiceDots(dice: Dice, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        enabled = dice.value != 0,
        border = BorderStroke(1.dp, contentColorFor(MaterialTheme.colorScheme.surface)),
        modifier = modifier.sizeIn(56.dp)
    ) { DiceDotsPattern(dice.value) }
}

@Composable
private fun DiceDotsPattern(diceValue: Int) {
    val fontSize = LocalTextStyle.current.fontSize
    val fontColor = LocalContentColor.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = fontSize.toPx() / 4

        // Define dot positions for each dice value
        val dotPositions = when (diceValue) {
            1 -> listOf(center) // Center
            2 -> listOf(
                Offset(size.width * 0.75f, size.height * 0.25f), // Top-right
                Offset(size.width * 0.25f, size.height * 0.75f)  // Bottom-left
            )

            3 -> listOf(
                Offset(size.width * 0.75f, size.height * 0.25f), // Top-right
                center, // Center
                Offset(size.width * 0.25f, size.height * 0.75f)  // Bottom-left
            )

            4 -> listOf(
                Offset(size.width * 0.25f, size.height * 0.25f), // Top-left
                Offset(size.width * 0.75f, size.height * 0.25f), // Top-right
                Offset(size.width * 0.25f, size.height * 0.75f), // Bottom-left
                Offset(size.width * 0.75f, size.height * 0.75f)  // Bottom-right
            )

            5 -> listOf(
                Offset(size.width * 0.25f, size.height * 0.25f), // Top-left
                Offset(size.width * 0.75f, size.height * 0.25f), // Top-right
                center, // Center
                Offset(size.width * 0.25f, size.height * 0.75f), // Bottom-left
                Offset(size.width * 0.75f, size.height * 0.75f)  // Bottom-right
            )

            6 -> listOf(
                Offset(size.width * 0.25f, size.height * 0.25f), // Top-left
                Offset(size.width * 0.75f, size.height * 0.25f), // Top-right
                Offset(size.width * 0.25f, size.height * 0.5f),  // Middle-left
                Offset(size.width * 0.75f, size.height * 0.5f),  // Middle-right
                Offset(size.width * 0.25f, size.height * 0.75f), // Bottom-left
                Offset(size.width * 0.75f, size.height * 0.75f)  // Bottom-right
            )

            else -> emptyList()
        }

        // Draw all dots in a single pass
        dotPositions.forEach { position ->
            drawCircle(
                color = fontColor,
                radius = radius,
                center = position
            )
        }
    }
}
