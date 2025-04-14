package com.programmersbox.fiveofakind

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// Farkle scoring categories
internal enum class FarkleHandType(val displayName: String) {
    SingleOne("Single 1"),
    SingleFive("Single 5"),
    ThreeOnes("Three 1s"),
    ThreeOfAKind("Three of a Kind"),
    FourOfAKind("Four of a Kind"),
    FiveOfAKind("Five of a Kind"),
    SixOfAKind("Six of a Kind"),
    Straight("Straight (1-2-3-4-5-6)"),
    ThreePairs("Three Pairs"),
    TwoTriplets("Two Triplets")
}

internal class FarkleScores {
    // Current turn score
    var currentTurnScore by mutableIntStateOf(0)

    // Total banked score
    var totalScore by mutableIntStateOf(0)

    // Data class to hold hand type and the dice that contribute to it
    data class HandTypeInfo(
        val handType: FarkleHandType,
        val dice: List<Dice>,
    )

    // Check if a set of dice contains scoring combinations
    fun hasScoringDice(dice: Collection<Dice>): Boolean {
        if (dice.isEmpty()) return false

        val valueCounts = dice.groupingBy { it.value }.eachCount()

        // Check for 1s or 5s
        if (valueCounts.containsKey(1) || valueCounts.containsKey(5)) return true

        // Check for three or more of a kind
        if (valueCounts.values.any { it >= 3 }) return true

        // Check for straight
        if (valueCounts.size == 6 && valueCounts.keys.containsAll((1..6).toList())) return true

        // Check for three pairs
        if (valueCounts.size == 3 && valueCounts.values.all { it == 2 }) return true

        // Check for two triplets
        if (valueCounts.size == 2 && valueCounts.values.all { it == 3 }) return true

        return false
    }

    // Identify all possible hand types in a set of dice
    fun identifyHandTypes(dice: Collection<Dice>): List<HandTypeInfo> {
        if (dice.isEmpty()) return emptyList()

        val result = mutableListOf<HandTypeInfo>()
        val valueCounts = dice.groupingBy { it.value }.eachCount()
        val diceByValue = dice.groupBy { it.value }

        // Check for straight (1-2-3-4-5-6)
        if (valueCounts.size == 6 && valueCounts.keys.containsAll((1..6).toList())) {
            result.add(HandTypeInfo(FarkleHandType.Straight, dice.toList()))
            return result // Straight uses all dice, no need to check other combinations
        }

        // Check for three pairs
        if (valueCounts.size == 3 && valueCounts.values.all { it == 2 }) {
            result.add(HandTypeInfo(FarkleHandType.ThreePairs, dice.toList()))
            return result // Three pairs uses all dice, no need to check other combinations
        }

        // Check for two triplets
        if (valueCounts.size == 2 && valueCounts.values.all { it == 3 }) {
            result.add(HandTypeInfo(FarkleHandType.TwoTriplets, dice.toList()))
            return result // Two triplets uses all dice, no need to check other combinations
        }

        // Check for six of a kind
        valueCounts.forEach { (value, count) ->
            if (count == 6) {
                result.add(HandTypeInfo(FarkleHandType.SixOfAKind, diceByValue[value]!!))
                return result // Six of a kind uses all dice, no need to check other combinations
            }
        }

        // Check for five of a kind
        valueCounts.forEach { (value, count) ->
            if (count == 5) {
                result.add(HandTypeInfo(FarkleHandType.FiveOfAKind, diceByValue[value]!!.take(5)))

                // Check remaining dice for singles
                val remainingDice = dice.filter { it.value != value }
                checkSingleDice(remainingDice, result)

                return result
            }
        }

        // Check for four of a kind
        valueCounts.forEach { (value, count) ->
            if (count == 4) {
                result.add(HandTypeInfo(FarkleHandType.FourOfAKind, diceByValue[value]!!.take(4)))

                // Check remaining dice for singles or pairs
                val remainingDice = dice.filter { it.value != value }
                checkSingleDice(remainingDice, result)

                return result
            }
        }

        // Check for three of a kind (multiple possible)
        valueCounts.forEach { (value, count) ->
            if (count >= 3) {
                if (value == 1) {
                    result.add(HandTypeInfo(FarkleHandType.ThreeOnes, diceByValue[value]!!.take(3)))
                } else {
                    result.add(HandTypeInfo(FarkleHandType.ThreeOfAKind, diceByValue[value]!!.take(3)))
                }
            }
        }

        // Check for single 1s and 5s
        checkSingleDice(dice, result)

        return result
    }

    // Helper method to check for single 1s and 5s
    private fun checkSingleDice(dice: Collection<Dice>, result: MutableList<HandTypeInfo>) {
        val ones = dice.filter { it.value == 1 }
        val fives = dice.filter { it.value == 5 }

        if (ones.isNotEmpty()) {
            result.add(HandTypeInfo(FarkleHandType.SingleOne, ones))
        }

        if (fives.isNotEmpty()) {
            result.add(HandTypeInfo(FarkleHandType.SingleFive, fives))
        }
    }

    // Calculate score for selected dice
    fun calculateScore(selectedDice: Collection<Dice>): Int {
        if (selectedDice.isEmpty()) return 0

        val valueCounts = selectedDice.groupingBy { it.value }.eachCount()
        var score = 0

        // Check for straight (1-2-3-4-5-6)
        if (valueCounts.size == 6 && valueCounts.keys.containsAll((1..6).toList())) {
            return 1500
        }

        // Check for three pairs
        if (valueCounts.size == 3 && valueCounts.values.all { it == 2 }) {
            return 1500
        }

        // Check for two triplets
        if (valueCounts.size == 2 && valueCounts.values.all { it == 3 }) {
            return 2500
        }

        // Process each dice value
        valueCounts.forEach { (value, count) ->
            when {
                // Six of a kind
                count == 6 -> score += when (value) {
                    1 -> 4000 // 4 × 1000
                    else -> 4 * 100 * value
                }

                // Five of a kind
                count == 5 -> score += when (value) {
                    1 -> 3000 // 3 × 1000
                    else -> 3 * 100 * value
                }

                // Four of a kind
                count == 4 -> score += when (value) {
                    1 -> 2000 // 2 × 1000
                    else -> 2 * 100 * value
                }

                // Three of a kind
                count == 3 -> score += when (value) {
                    1 -> 1000
                    else -> 100 * value
                }

                // Single 1s and 5s
                count < 3 -> {
                    if (value == 1) score += count * 100
                    else if (value == 5) score += count * 50
                }
            }
        }

        return score
    }

    // Add current turn score to total score
    fun bankScore() {
        totalScore += currentTurnScore
        currentTurnScore = 0
    }

    // Reset current turn score (farkle)
    fun farkle() {
        currentTurnScore = 0
    }

    // Reset all scores for a new game
    fun resetScores() {
        currentTurnScore = 0
        totalScore = 0
    }
}
