package com.programmersbox.fiveofakind

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FarkleScreen(
    vm: FarkleViewModel = viewModel { FarkleViewModel() },
    database: YahtzeeDatabase,
    onAboutClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var diceLook by rememberShowDotsOnDice()
    val scope = rememberCoroutineScope()

    var newGameDialog by remember { mutableStateOf(false) }

    if (newGameDialog) {
        AlertDialog(
            onDismissRequest = { newGameDialog = false },
            title = { Text("Want to start a new game?") },
            text = { Text("You have ${vm.scores.totalScore} points. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetGame()
                        newGameDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { newGameDialog = false }) { Text("No") } }
        )
    }

    var instructions by remember { mutableStateOf(false) }

    if (instructions) {
        AlertDialog(
            onDismissRequest = { instructions = false },
            title = { Text("Farkle Rules") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "Farkle is a dice game where players roll six dice and score points based on certain combinations.",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { Text("Scoring:", fontWeight = FontWeight.Bold) }
                    item { Text("• Single 1: 100 points") }
                    item { Text("• Single 5: 50 points") }
                    item { Text("• Three 1s: 1000 points") }
                    item { Text("• Three of a kind (except 1s): 100 × the number") }
                    item { Text("• Four of a kind: 2 × three of a kind") }
                    item { Text("• Five of a kind: 3 × three of a kind") }
                    item { Text("• Six of a kind: 4 × three of a kind") }
                    item { Text("• Straight (1-2-3-4-5-6): 1500 points") }
                    item { Text("• Three pairs: 1500 points") }
                    item { Text("• Two triplets: 2500 points") }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { Text("How to Play:", fontWeight = FontWeight.Bold) }
                    item { Text("1. Roll all six dice") }
                    item { Text("2. Select at least one scoring die") }
                    item { Text("3. Either bank your points or roll the remaining dice") }
                    item { Text("4. If you roll and get no scoring dice, you 'Farkle' and lose all points for that turn") }
                    item { Text("5. First player to reach 10,000 points wins") }
                }
            },
            confirmButton = { TextButton(onClick = { instructions = false }) { Text("Got it") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farkle") },
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() }
                    ) { Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home") }
                },
                actions = {
                    IconButton(
                        onClick = { instructions = true },
                    ) { Icon(Icons.Default.Info, null) }

                    TextButton(onClick = { newGameDialog = true }) { Text("New Game") }

                    Spacer(Modifier.width(12.dp))

                    Dice(1, "").ShowDice(
                        useDots = diceLook,
                        onClick = { diceLook = !diceLook },
                        modifier = Modifier.size(40.dp)
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = BottomAppBarDefaults.containerColor,
                contentPadding = BottomAppBarDefaults.ContentPadding.let {
                    PaddingValues(
                        bottom = it.calculateTopPadding(),
                        start = it.calculateStartPadding(LayoutDirection.Ltr),
                        end = it.calculateEndPadding(LayoutDirection.Ltr),
                        top = it.calculateTopPadding(),
                    )
                },
                actions = {
                    // Bank button
                    Button(
                        onClick = { vm.bankScore() },
                        enabled = vm.state == FarkleState.Selecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bank ${vm.scores.currentTurnScore} Points")
                    }

                    Spacer(Modifier.width(8.dp))

                    // Reset button
                    Button(
                        onClick = { vm.resetTurn() },
                        enabled = vm.state == FarkleState.Banked || vm.state == FarkleState.Selecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Turn")
                    }

                    Spacer(Modifier.width(8.dp))

                    // Roll button
                    Button(
                        onClick = {
                            if (vm.state == FarkleState.Banked) {
                                vm.startNewTurn()
                            } else {
                                vm.rollDice()
                            }
                        },
                        enabled = when (vm.state) {
                            FarkleState.Banked -> true
                            FarkleState.Selecting -> vm.isValidSelection() && !vm.rolling
                            FarkleState.Rolling -> false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when (vm.state) {
                                FarkleState.Banked -> "Start Turn"
                                else -> "Roll Again"
                            }
                        )
                    }
                }
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(p),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score display
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Score: ${animateIntAsState(vm.scores.totalScore).value}",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    AnimatedVisibility(vm.scores.currentTurnScore > 0) {
                        Text(
                            "Current Turn: ${animateIntAsState(vm.scores.currentTurnScore).value}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Dice grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..2) {
                    val dice = vm.hand[i]
                    val isSelected = dice in vm.selectedDice
                    val isScored = dice in vm.scoredDice
                    val isHoldable = vm.isDiceHoldable(dice)

                    dice(
                        useDots = diceLook,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(70.dp)
                            .border(
                                width = animateDpAsState(
                                    targetValue = when {
                                        isScored -> 4.dp
                                        isSelected -> 4.dp
                                        isHoldable && vm.state == FarkleState.Selecting -> 2.dp
                                        else -> 0.dp
                                    }
                                ).value,
                                color = animateColorAsState(
                                    targetValue = when {
                                        isScored -> Alizarin
                                        isSelected -> Emerald
                                        isHoldable && vm.state == FarkleState.Selecting -> Sunflower
                                        else -> Color.Transparent
                                    }
                                ).value,
                                shape = RoundedCornerShape(7.dp)
                            )
                    ) {
                        if (vm.state == FarkleState.Selecting && !isScored) {
                            vm.toggleDieSelection(dice)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 3..5) {
                    val dice = vm.hand[i]
                    val isSelected = dice in vm.selectedDice
                    val isScored = dice in vm.scoredDice
                    val isHoldable = vm.isDiceHoldable(dice)

                    dice(
                        useDots = diceLook,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(70.dp)
                            .border(
                                width = animateDpAsState(
                                    targetValue = when {
                                        isScored -> 4.dp
                                        isSelected -> 4.dp
                                        isHoldable && vm.state == FarkleState.Selecting -> 2.dp
                                        else -> 0.dp
                                    }
                                ).value,
                                color = animateColorAsState(
                                    targetValue = when {
                                        isScored -> Alizarin
                                        isSelected -> Emerald
                                        isHoldable && vm.state == FarkleState.Selecting -> Sunflower
                                        else -> Color.Transparent
                                    }
                                ).value,
                                shape = RoundedCornerShape(7.dp)
                            )
                    ) {
                        if (vm.state == FarkleState.Selecting && !isScored) {
                            vm.toggleDieSelection(dice)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Available hand types
            AnimatedVisibility(
                visible = vm.state == FarkleState.Selecting && vm.availableHandTypes.isNotEmpty() && vm.selectedDice.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Available Hand Types:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        vm.availableHandTypes.forEach { handTypeInfo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    handTypeInfo.handType.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    "${vm.scores.calculateScore(handTypeInfo.dice)} points",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Selection score
            AnimatedVisibility(vm.selectedDice.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        vm.currentHandType?.let { handTypeInfo ->
                            Text(
                                "Hand Type: ${handTypeInfo.handType.displayName}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(8.dp))
                        }

                        Text(
                            "Selection Score: ${vm.scores.calculateScore(vm.selectedDice)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    // Game over dialog
    if (vm.scores.totalScore >= vm.targetScore && vm.showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { vm.showGameOverDialog = false },
            title = { Text("Game Over") },
            text = { Text("Congratulations! You reached ${vm.scores.totalScore} points!") },
            confirmButton = { TextButton(onClick = vm::resetGame) { Text("Play Again") } },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.showGameOverDialog = false
                    }
                ) { Text("Close") }
            }
        )
    }
}
