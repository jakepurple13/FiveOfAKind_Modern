package com.programmersbox.fiveofakind

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextInt

internal suspend fun randomNumberAnimation(
    newValue: Int,
    valueChange: (Int) -> Unit,
    delayAmountMs: Long = 50L,
    randomCount: Int = 5,
    range: IntRange = 1..6,
) {
    repeat(randomCount) {
        delay(delayAmountMs)
        valueChange(Random.nextInt(range))
    }
    valueChange(newValue)
}

@Composable
internal fun <T> randomItemAnimation(
    initialValue: T,
    newValue: () -> T,
    randomValue: (Int) -> T,
    randomCount: Int = 5,
    delayAmountMs: Long = 50L,
    vararg keys: Any
) = produceState(initialValue = initialValue, keys = keys) {
    val endValue = newValue()
    repeat(randomCount) {
        delay(delayAmountMs)
        value = randomValue(it)
    }
    value = endValue
}

@Composable
internal fun randomNumberAnimation(
    initialValue: Int,
    newValue: Int,
    randomCount: Int = 5,
    delayAmountMs: Long = 50L,
    range: IntRange = 1..6,
    vararg keys: Any,
) = randomItemAnimation(
    initialValue = initialValue,
    newValue = { newValue },
    randomValue = { Random.nextInt(range) },
    randomCount = randomCount,
    delayAmountMs = delayAmountMs,
    keys = keys
)
