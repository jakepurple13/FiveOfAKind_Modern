package com.programmersbox.fiveofakind

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
enum class HandType(val isSmall: Boolean, val displayName: String) {
    Ones(true, "Ones"),
    Twos(true, "Twos"),
    Threes(true, "Threes"),
    Fours(true, "Fours"),
    Fives(true, "Fives"),
    Sixes(true, "Sixes"),
    ThreeOfAKind(false, "Three of a Kind") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) dice.sumOf { it.value } else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean {
            val values = dice.groupingBy { it.value }.eachCount().values
            return 3 in values || 4 in values || 5 in values
        }
    },
    FourOfAKind(false, "Four of a Kind") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) dice.sumOf { it.value } else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean {
            val values = dice.groupingBy { it.value }.eachCount().values
            return 4 in values || 5 in values
        }
    },
    FullHouse(false, "Full House") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) 25 else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean {
            val values = dice.groupingBy { it.value }.eachCount().values
            return 3 in values && 2 in values
        }
    },
    SmallStraight(false, "Small Straight") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) 30 else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean {
            val filteredDice = dice.sortedBy { it.value }
            return longestSequence(filteredDice.toTypedArray()) in 3..4
        }
    },
    LargeStraight(false, "Large Straight") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) 40 else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean {
            val filteredDice = dice.sortedBy { it.value }
            return longestSequence(filteredDice.toTypedArray()) == 4
        }
    },
    FiveOfAKind(false, "Five of a Kind") {
        override fun getScoreValue(dice: Collection<Dice>): Int = if (canGetScore(dice)) 50 else 0
        override fun canGetScore(dice: Collection<Dice>): Boolean = 5 in dice.groupingBy { it.value }.eachCount().values
    },
    Chance(false, "Chance") {
        override fun getScoreValue(dice: Collection<Dice>): Int = dice.sumOf { it.value }
        override fun canGetScore(dice: Collection<Dice>): Boolean = true
    }
    ;

    open fun getScoreValue(dice: Collection<Dice>): Int = if (isSmall)
        getSmallNum(dice, ordinal + 1)
    else
        error("This should not be called. This is only for small dice. Try using the getScoreValue(Collection<Dice>, HandType) method instead.")

    open fun canGetScore(dice: Collection<Dice>): Boolean = isSmall
}

internal class YahtzeeScores {
    val scoreList = mutableStateMapOf<HandType, Int>()

    // Helper method to calculate and cache dice value counts
    private fun getDiceValueCounts(dice: Collection<Dice>): Map<Int, Int> {
        return dice.groupingBy { it.value }.eachCount()
    }

    // Helper method to get the counts of each dice value occurrence
    private fun getValueOccurrences(dice: Collection<Dice>): Collection<Int> {
        return getDiceValueCounts(dice).values
    }

    val smallScore by derivedStateOf {
        scoreList
            .filter { it.key.isSmall }
            .values
            .sum()
    }

    val hasBonus by derivedStateOf { smallScore >= 63 }

    val largeScore by derivedStateOf {
        scoreList
            .filterNot { it.key.isSmall }
            .values
            .sum()
    }

    val totalScore by derivedStateOf {
        smallScore + largeScore + if (hasBonus) 35 else 0
    }

    private val placedThreeOfKind by derivedStateOf { scoreList.containsKey(HandType.ThreeOfAKind) }
    private val placedFourOfKind by derivedStateOf { scoreList.containsKey(HandType.FourOfAKind) }
    private val placedFullHouse by derivedStateOf { scoreList.containsKey(HandType.FullHouse) }
    private val placedSmallStraight by derivedStateOf { scoreList.containsKey(HandType.SmallStraight) }
    private val placedLargeStraight by derivedStateOf { scoreList.containsKey(HandType.LargeStraight) }
    private val placedFiveOfAKind by derivedStateOf { scoreList.containsKey(HandType.FiveOfAKind) }
    private val placedChance by derivedStateOf { scoreList.containsKey(HandType.Chance) }

    private val placedOnes by derivedStateOf { scoreList.containsKey(HandType.Ones) }
    private val placedTwos by derivedStateOf { scoreList.containsKey(HandType.Twos) }
    private val placedThrees by derivedStateOf { scoreList.containsKey(HandType.Threes) }
    private val placedFours by derivedStateOf { scoreList.containsKey(HandType.Fours) }
    private val placedFives by derivedStateOf { scoreList.containsKey(HandType.Fives) }
    private val placedSixes by derivedStateOf { scoreList.containsKey(HandType.Sixes) }

    val isGameOver by derivedStateOf {
        placedFiveOfAKind && placedChance &&
                placedLargeStraight && placedSmallStraight &&
                placedFullHouse &&
                placedFourOfKind && placedThreeOfKind &&
                placedOnes && placedTwos && placedThrees && placedFours && placedFives && placedSixes
    }

    fun getSmall(dice: Collection<Dice>, type: HandType) = getSmallNum(dice, type.ordinal + 1).apply {
        scoreList[type] = this
    }

    fun getThreeOfAKind(dice: Collection<Dice>): Int = HandType.ThreeOfAKind.getScoreValue(dice).apply {
        scoreList[HandType.ThreeOfAKind] = this
    }

    fun getFourOfAKind(dice: Collection<Dice>): Int = HandType.FourOfAKind.getScoreValue(dice).apply {
        scoreList[HandType.FourOfAKind] = this
    }

    fun getYahtzee(dice: Collection<Dice>): Int {
        val num = if (HandType.FiveOfAKind.canGetScore(dice)) {
            if (scoreList.containsKey(HandType.FiveOfAKind)) 100 else 50
        } else 0
        return num.apply {
            scoreList[HandType.FiveOfAKind] = scoreList.getOrElse(HandType.FiveOfAKind) { 0 } + this
        }
    }

    fun getFullHouse(dice: Collection<Dice>): Int = HandType.FullHouse.getScoreValue(dice).apply {
        scoreList[HandType.FullHouse] = this
    }

    fun getLargeStraight(dice: Collection<Dice>): Int = HandType.LargeStraight.getScoreValue(dice).apply {
        scoreList[HandType.LargeStraight] = this
    }

    fun getSmallStraight(dice: Collection<Dice>): Int = HandType.SmallStraight.getScoreValue(dice).apply {
        scoreList[HandType.SmallStraight] = this
    }

    fun getChance(dice: Collection<Dice>): Int = HandType.Chance.getScoreValue(dice).apply {
        scoreList[HandType.Chance] = this
    }

    fun resetScores() {
        scoreList.clear()
    }
}

private fun getSmallNum(dice: Collection<Dice>, num: Int): Int = dice.filter { it.value == num }.sumOf { it.value }

private fun longestSequence(a: Array<Dice>): Int {
    val sorted = a.sortedBy { it.value }
    var longest = 0
    var sequence = 0
    for (i in 1 until sorted.size) {
        when (sorted[i].value - sorted[i - 1].value) {
            0 -> Unit/*ignore duplicates*/
            1 -> sequence += 1
            else -> if (sequence > longest) {
                longest = sequence
                sequence = 0
            }
        }
    }
    return max(longest, sequence)
}