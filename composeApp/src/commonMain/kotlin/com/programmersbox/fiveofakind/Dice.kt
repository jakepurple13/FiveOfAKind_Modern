package com.programmersbox.fiveofakind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.random.nextInt

internal class Dice(value: Int = Random.nextInt(1..6), @Suppress("unused") val location: String) {
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
    ) = if (useDots) DiceDots(this, modifier, onClick) else Dice(this, modifier, onClick)

    // Operator function as a shorthand for ShowDice
    @Composable
    operator fun invoke(
        useDots: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
    ) = ShowDice(useDots, modifier, onClick)
}

@Composable
internal fun Dice(dice: Dice, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        tonalElevation = 4.dp,
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
        enabled = dice.value != 0,
        border = BorderStroke(1.dp, contentColorFor(MaterialTheme.colorScheme.surface)),
        modifier = modifier.size(56.dp)
    ) {
        when (dice.value) {
            1 -> {
                val fontSize = LocalTextStyle.current.fontSize
                val fontColor = LocalContentColor.current
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = fontSize.toPx() / 4

                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = center
                    )
                }
            }

            2 -> {
                val fontSize = LocalTextStyle.current.fontSize
                val fontColor = LocalContentColor.current
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = fontSize.toPx() / 4

                    // Top-right dot
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = Offset(size.width * 0.75f, size.height * 0.25f)
                    )

                    // Bottom-left dot
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = Offset(size.width * 0.25f, size.height * 0.75f)
                    )
                }
            }

            3 -> {
                val fontSize = LocalTextStyle.current.fontSize
                val fontColor = LocalContentColor.current
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = fontSize.toPx() / 4

                    // Top-right dot
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = Offset(size.width * 0.75f, size.height * 0.25f)
                    )

                    // Center dot
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = center
                    )

                    // Bottom-left dot
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = Offset(size.width * 0.25f, size.height * 0.75f)
                    )
                }
            }

            4 -> {
                CanvasDice(
                    diceValue = 4,
                    spaceBetweenHeightDivider = 3
                )
            }

            5 -> {
                CanvasDice(
                    diceValue = 4,
                    spaceBetweenHeightDivider = 3,
                ) { fontColor, radius ->
                    drawCircle(
                        color = fontColor,
                        radius = radius,
                        center = center
                    )
                }
            }

            6 -> {
                CanvasDice(
                    diceValue = 6,
                    spaceBetweenHeightDivider = 4
                )
            }
        }
    }
}

@Composable
private fun CanvasDice(
    diceValue: Int,
    spaceBetweenHeightDivider: Int,
    spaceBetweenWidthDivider: Int = 3,
    canvasContent: DrawScope.(fontColor: Color, radius: Float) -> Unit = { _, _ -> },
) {
    // Use Canvas for more efficient rendering of 6 dots
    val fontSize = LocalTextStyle.current.fontSize
    val fontColor = LocalContentColor.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spaceBetweenWidthDots = size.width / spaceBetweenWidthDivider
        val spaceBetweenHeightDots = size.height / spaceBetweenHeightDivider
        val radius = fontSize.toPx() / 4

        // Draw all 6 dots in a single pass
        for (i in 0 until diceValue) {
            val x = spaceBetweenWidthDots * (i % 2 + 1)
            val y = spaceBetweenHeightDots * (i / 2 + 1)
            drawCircle(
                color = fontColor,
                radius = radius,
                center = Offset(x, y)
            )
        }

        canvasContent(fontColor, radius)
    }
}
