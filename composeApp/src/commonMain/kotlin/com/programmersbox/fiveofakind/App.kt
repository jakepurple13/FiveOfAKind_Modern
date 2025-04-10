package com.programmersbox.fiveofakind

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.ktx.from
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@Composable
@Preview
fun App(
    database: YahtzeeDatabase,
) {
    MaterialTheme(
        colorScheme = buildColorScheme(isSystemInDarkTheme())
    ) {
        YahtzeeScreen(
            database = database
        )
    }
}

internal typealias ScoreClick = () -> Unit

internal val Emerald = Color(0xFF2ecc71)
internal val Sunflower = Color(0xFFf1c40f)
internal val Alizarin = Color(0xFFe74c3c)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun YahtzeeScreen(
    vm: YahtzeeViewModel = viewModel { YahtzeeViewModel() },
    database: YahtzeeDatabase,
) {
    var diceLook by rememberShowDotsOnDice()
    var isUsing24HourTime by rememberUse24HourTime()
    val scope = rememberCoroutineScope()

    val highScores by database
        .getHighScores()
        .collectAsStateWithLifecycle(emptyList())

    val stats by database
        .getHighScoreStats()
        .collectAsStateWithLifecycle(emptyList())

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

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var statsDialog by remember { mutableStateOf(false) }

    if (statsDialog) {
        ModalBottomSheet(
            onDismissRequest = { statsDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) { BottomSheetContent(stats) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("High Scores") },
                            actions = {
                                Text(
                                    highScores.size.toString(),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            },
                            navigationIcon = {
                                TextButton(
                                    onClick = { statsDialog = true }
                                ) { Text("Stats") }
                            },
                        )
                    }
                ) { p ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = p
                    ) {
                        ThemeChange()

                        items(highScores) {
                            HighScoreItem(
                                item = it,
                                scaffoldState = drawerState,
                                isUsing24HourTime = isUsing24HourTime,
                                onDelete = { scope.launch { database.removeHighScore(it) } },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Five Dice") },
                    actions = {
                        if (!IS_NOT_DEBUG) {
                            TextButton(
                                onClick = {
                                    HandType.entries.forEach {
                                        vm.scores.scoreList[it] = 10
                                    }
                                }
                            ) { Text("Finish") }
                        }
                        TextButton(onClick = { newGameDialog = true }) { Text("New Game") }
                        TextButton(
                            onClick = { isUsing24HourTime = !isUsing24HourTime },
                        ) {
                            Crossfade(isUsing24HourTime) { target ->
                                Text(if (target) "24H" else "12H")
                            }
                        }
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
                BottomBarDiceRow(
                    vm = vm,
                    diceLooks = diceLook,
                )
            },
        ) { p ->
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(p),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    SmallScores(
                        smallScore = vm.scores.smallScore,
                        hand = vm.hand,
                        hasBonus = vm.scores.hasBonus,
                        isRolling = vm.rolling,
                        containsCheck = { vm.scores.scoreList.containsKey(it) },
                        scoreGet = { vm.scores.scoreList.getOrElse(it) { 0 } },
                        onOnesClick = vm::placeOnes,
                        onTwosClick = vm::placeTwos,
                        onThreesClick = vm::placeThrees,
                        onFoursClick = vm::placeFours,
                        onFivesClick = vm::placeFives,
                        onSixesClick = vm::placeSixes,
                        modifier = Modifier.weight(1f)
                    )
                    LargeScores(
                        largeScore = vm.scores.largeScore,
                        isRolling = vm.rolling,
                        hand = vm.hand,
                        isNotRollOneState = vm.state != YahtzeeState.RollOne,
                        containsCheck = { vm.scores.scoreList.containsKey(it) },
                        scoreGet = { vm.scores.scoreList.getOrElse(it) { 0 } },
                        canGetHand = {
                            when (it) {
                                HandType.ThreeOfAKind -> vm.scores.canGetThreeKind(vm.hand)
                                HandType.FourOfAKind -> vm.scores.canGetFourKind(vm.hand)
                                HandType.FullHouse -> vm.scores.canGetFullHouse(vm.hand)
                                HandType.SmallStraight -> vm.scores.canGetSmallStraight(vm.hand)
                                HandType.LargeStraight -> vm.scores.canGetLargeStraight(vm.hand)
                                HandType.FiveOfAKind -> vm.scores.canGetYahtzee(vm.hand)
                                else -> false
                            }
                        },
                        onThreeKindClick = vm::placeThreeOfKind,
                        onFourKindClick = vm::placeFourOfKind,
                        onFullHouseClick = vm::placeFullHouse,
                        onSmallStraightClick = vm::placeSmallStraight,
                        onLargeStraightClick = vm::placeLargeStraight,
                        onYahtzeeClick = vm::placeYahtzee,
                        onChanceClick = vm::placeChance,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Total Score: ${animateIntAsState(vm.scores.totalScore).value}")

                Text(
                    getPlatform().name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }

    if (vm.scores.isGameOver && vm.showGameOverDialog) {
        LaunchedEffect(Unit) {
            database.addHighScore(
                ActualYahtzeeScoreItem(
                    ones = vm.scores.scoreList.getOrElse(HandType.Ones) { 0 },
                    twos = vm.scores.scoreList.getOrElse(HandType.Twos) { 0 },
                    threes = vm.scores.scoreList.getOrElse(HandType.Threes) { 0 },
                    fours = vm.scores.scoreList.getOrElse(HandType.Fours) { 0 },
                    fives = vm.scores.scoreList.getOrElse(HandType.Fives) { 0 },
                    sixes = vm.scores.scoreList.getOrElse(HandType.Sixes) { 0 },
                    threeKind = vm.scores.scoreList.getOrElse(HandType.ThreeOfAKind) { 0 },
                    fourKind = vm.scores.scoreList.getOrElse(HandType.FourOfAKind) { 0 },
                    fullHouse = vm.scores.scoreList.getOrElse(HandType.FullHouse) { 0 },
                    smallStraight = vm.scores.scoreList.getOrElse(HandType.SmallStraight) { 0 },
                    largeStraight = vm.scores.scoreList.getOrElse(HandType.LargeStraight) { 0 },
                    yahtzee = vm.scores.scoreList.getOrElse(HandType.FiveOfAKind) { 0 },
                    chance = vm.scores.scoreList.getOrElse(HandType.Chance) { 0 },
                )
            )
        }

        AlertDialog(
            onDismissRequest = { vm.showGameOverDialog = false },
            title = { Text("Game Over") },
            text = { Text("You got a score of ${vm.scores.totalScore}") },
            confirmButton = { TextButton(onClick = vm::resetGame) { Text("Play Again") } },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.showGameOverDialog = false
                    }
                ) { Text("Stop Playing") }
            }
        )
    }
}

@Composable
internal fun BottomBarDiceRow(vm: YahtzeeViewModel, diceLooks: Boolean) {
    val isAmoled by rememberIsAmoled()
    BottomAppBar(
        containerColor = if (isAmoled) MaterialTheme.colorScheme.surface else BottomAppBarDefaults.containerColor,
    ) {
        vm.hand.forEach { dice ->
            dice(
                useDots = diceLooks,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(1f)
                    .aspectRatio(1f, true)
                    .border(
                        width = animateDpAsState(targetValue = if (dice in vm.hold) 4.dp else 0.dp).value,
                        color = animateColorAsState(targetValue = if (dice in vm.hold) Emerald else Color.Transparent).value,
                        shape = RoundedCornerShape(7.dp)
                    )
            ) { if (dice in vm.hold) vm.hold.remove(dice) else vm.hold.add(dice) }
        }

        IconButton(
            onClick = vm::reroll,
            //TODO: && !vm.rolling fixes the double tap feature
            enabled = vm.state != YahtzeeState.Stop && (!vm.rolling || !IS_NOT_DEBUG),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = animateColorAsState(
                    when (vm.state) {
                        YahtzeeState.RollOne -> Emerald
                        YahtzeeState.RollTwo -> Sunflower
                        YahtzeeState.RollThree -> Alizarin
                        YahtzeeState.Stop -> LocalContentColor.current
                    }
                ).value
            )
        }
    }
}

@Composable
internal fun SmallScores(
    smallScore: Int,
    hand: List<Dice>,
    hasBonus: Boolean,
    isRolling: Boolean,
    containsCheck: (HandType) -> Boolean,
    scoreGet: (HandType) -> Int,
    onOnesClick: ScoreClick,
    onTwosClick: ScoreClick,
    onThreesClick: ScoreClick,
    onFoursClick: ScoreClick,
    onFivesClick: ScoreClick,
    onSixesClick: ScoreClick,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        val groupedCheck by remember {
            derivedStateOf {
                hand.groupingBy { it.value }
                    .eachCount()
                    .toList()
                    .sortedWith(compareBy({ it.second }, { it.first }))
                    .reversed()
                    .map { it.first }
            }
        }

        val highest = groupedCheck.elementAtOrNull(0)
        val medium = groupedCheck.elementAtOrNull(1)
        val lowest = groupedCheck.elementAtOrNull(2)

        fun canScore(value: Int) = highest == value || medium == value || lowest == value
        fun scoreColor(value: Int) = when {
            highest == value -> Emerald
            medium == value -> Sunflower
            lowest == value -> Alizarin
            else -> Color.Transparent
        }

        ScoreButton(
            category = "Ones",
            enabled = !containsCheck(HandType.Ones),
            score = scoreGet(HandType.Ones),
            canScore = canScore(1) && !isRolling,
            customBorderColor = scoreColor(1),
            onClick = onOnesClick
        )

        ScoreButton(
            category = "Twos",
            enabled = !containsCheck(HandType.Twos),
            score = scoreGet(HandType.Twos),
            canScore = canScore(2) && !isRolling,
            customBorderColor = scoreColor(2),
            onClick = onTwosClick
        )

        ScoreButton(
            category = "Threes",
            enabled = !containsCheck(HandType.Threes),
            score = scoreGet(HandType.Threes),
            canScore = canScore(3) && !isRolling,
            customBorderColor = scoreColor(3),
            onClick = onThreesClick
        )

        ScoreButton(
            category = "Fours",
            enabled = !containsCheck(HandType.Fours),
            score = scoreGet(HandType.Fours),
            canScore = canScore(4) && !isRolling,
            customBorderColor = scoreColor(4),
            onClick = onFoursClick
        )

        ScoreButton(
            category = "Fives",
            enabled = !containsCheck(HandType.Fives),
            score = scoreGet(HandType.Fives),
            canScore = canScore(5) && !isRolling,
            customBorderColor = scoreColor(5),
            onClick = onFivesClick
        )

        ScoreButton(
            category = "Sixes",
            enabled = !containsCheck(HandType.Sixes),
            score = scoreGet(HandType.Sixes),
            canScore = canScore(6) && !isRolling,
            customBorderColor = scoreColor(6),
            onClick = onSixesClick
        )

        AnimatedVisibility(hasBonus) {
            Text("+35 for >= 63")
        }

        if (smallScore >= 63) {
            val score by animateIntAsState(targetValue = smallScore)
            Text("Small Score: ${score + 35} ($score)")
        } else {
            Text("Small Score: ${animateIntAsState(smallScore).value}")
        }
    }
}

@Composable
internal fun LargeScores(
    largeScore: Int,
    isRolling: Boolean,
    hand: List<Dice>,
    isNotRollOneState: Boolean,
    containsCheck: (HandType) -> Boolean,
    scoreGet: (HandType) -> Int,
    canGetHand: (HandType) -> Boolean,
    onThreeKindClick: ScoreClick,
    onFourKindClick: ScoreClick,
    onFullHouseClick: ScoreClick,
    onSmallStraightClick: ScoreClick,
    onLargeStraightClick: ScoreClick,
    onYahtzeeClick: ScoreClick,
    onChanceClick: ScoreClick,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        ScoreButton(
            category = "Three of a Kind",
            enabled = !containsCheck(HandType.ThreeOfAKind),
            score = scoreGet(HandType.ThreeOfAKind),
            canScore = canGetHand(HandType.ThreeOfAKind) && isNotRollOneState && !isRolling,
            onClick = onThreeKindClick
        )

        ScoreButton(
            category = "Four of a Kind",
            enabled = !containsCheck(HandType.FourOfAKind),
            score = scoreGet(HandType.FourOfAKind),
            canScore = canGetHand(HandType.FourOfAKind) && isNotRollOneState && !isRolling,
            onClick = onFourKindClick
        )

        ScoreButton(
            category = "Full House",
            enabled = !containsCheck(HandType.FullHouse),
            score = scoreGet(HandType.FullHouse),
            canScore = canGetHand(HandType.FullHouse) && isNotRollOneState && !isRolling,
            onClick = onFullHouseClick
        )

        ScoreButton(
            category = "Small Straight",
            enabled = !containsCheck(HandType.SmallStraight),
            score = scoreGet(HandType.SmallStraight),
            canScore = canGetHand(HandType.SmallStraight) && isNotRollOneState && !isRolling,
            onClick = onSmallStraightClick
        )

        ScoreButton(
            category = "Large Straight",
            enabled = !containsCheck(HandType.LargeStraight),
            score = scoreGet(HandType.LargeStraight),
            canScore = canGetHand(HandType.LargeStraight) && isNotRollOneState && !isRolling,
            onClick = onLargeStraightClick
        )

        ScoreButton(
            category = "Five of a Kind",
            enabled = !containsCheck(HandType.FiveOfAKind) ||
                    canGetHand(HandType.FiveOfAKind) &&
                    hand.none { it.value == 0 },
            score = scoreGet(HandType.FiveOfAKind),
            canScore = canGetHand(HandType.FiveOfAKind) && isNotRollOneState && !isRolling,
            onClick = onYahtzeeClick
        )

        ScoreButton(
            category = "Chance",
            enabled = !containsCheck(HandType.Chance),
            score = scoreGet(HandType.Chance),
            onClick = onChanceClick
        )

        Text("Large Score: ${animateIntAsState(largeScore).value}")
    }
}

@Composable
internal fun ScoreButton(
    category: String,
    enabled: Boolean,
    canScore: Boolean = false,
    customBorderColor: Color = Emerald,
    score: Int,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(
            width = ButtonDefaults.outlinedButtonBorder.width,
            color = animateColorAsState(
                when {
                    canScore && enabled -> customBorderColor
                    enabled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
            ).value
        )
    ) { Text("$category: ${animateIntAsState(score).value}") }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HighScoreItem(
    item: ActualYahtzeeScoreItem,
    scaffoldState: DrawerState,
    onDelete: () -> Unit,
    isUsing24HourTime: Boolean,
    modifier: Modifier = Modifier,
) {
    var deleteDialog by remember { mutableStateOf(false) }

    val time = remember(isUsing24HourTime) {
        val d = Instant.fromEpochMilliseconds(item.time).toLocalDateTime(TimeZone.currentSystemDefault())
        d.format(
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_FULL)
                char(' ')
                dayOfMonth()
                char(' ')
                year()
                chars(", ")
                if (isUsing24HourTime) {
                    hour()
                    char(':')
                    minute()
                } else {
                    amPmHour()
                    char(':')
                    minute()
                    char(' ')
                    amPmMarker("AM", "PM")
                }
            }
        )
    }

    val smallScore = item.smallScore
    val largeScore = item.largeScore
    val totalScore = item.totalScore

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete $totalScore at $time") },
            text = { Text("Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        deleteDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text("No") } }
        )
    }

    var showMore by remember(scaffoldState.targetValue) { mutableStateOf(false) }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
    ) {
        ListItem(
            leadingContent = { IconButton(onClick = { deleteDialog = true }) { Icon(Icons.Default.Close, null) } },
            headlineContent = { Text("Score: $totalScore") },
            overlineContent = { Text("Time: $time") },
            trailingContent = {
                IconButton(
                    onClick = { showMore = !showMore },
                    modifier = Modifier.rotate(animateFloatAsState(targetValue = if (showMore) 180f else 0f).value)
                ) { Icon(Icons.Default.ArrowDropDown, null) }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
        AnimatedVisibility(visible = showMore) {
            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Ones: ${item.ones}")
                    Text("Twos: ${item.twos}")
                    Text("Threes: ${item.threes}")
                    Text("Fours: ${item.fours}")
                    Text("Fives: ${item.fives}")
                    Text("Sixes: ${item.sixes}")
                    if (smallScore >= 63) {
                        Text("+35 for >= 63")
                    }
                    val originalScore = if (smallScore >= 63) " ($smallScore)" else ""
                    Text("Small Score: ${if (smallScore >= 63) smallScore + 35 else smallScore}$originalScore")
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Three of a Kind: ${item.threeKind}")
                    Text("Four of a Kind: ${item.fourKind}")
                    Text("Full House: ${item.fullHouse}")
                    Text("Small Straight: ${item.smallStraight}")
                    Text("Large Straight: ${item.largeStraight}")
                    Text("Yahtzee: ${item.yahtzee}")
                    Text("Chance: ${item.chance}")
                    Text("Large Score: $largeScore")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    scores: List<ActualYahtzeeScoreStat>,
) {
    TopAppBar(
        title = { Text("Stats") }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(scores) {
            ListItem(
                headlineContent = { Text(it.handType) },
                supportingContent = {
                    Column {
                        Text("Times Counted: ${it.numberOfTimes}")
                        Text("Total Points: ${it.totalPoints}")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun LazyListScope.ThemeChange() = item {
    var showThemes by remember { mutableStateOf(false) }
    var themeColor by rememberThemeColor()
    var isAmoled by rememberIsAmoled()

    if (showThemes) {
        ModalBottomSheet(
            onDismissRequest = { showThemes = false },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Card(
                    onClick = { isAmoled = !isAmoled },
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    ListItem(
                        headlineContent = { Text("Is Amoled") },
                        trailingContent = {
                            Switch(
                                checked = isAmoled,
                                onCheckedChange = { isAmoled = it }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        )
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ThemeColor
                        .entries
                        .filter { it != ThemeColor.Custom }
                        .forEach {
                            ThemeItem(
                                onClick = { themeColor = it },
                                selected = themeColor == it,
                                themeColor = it,
                                colorScheme = if (it == ThemeColor.Dynamic)
                                    MaterialTheme.colorScheme
                                else
                                    rememberDynamicColorScheme(
                                        seedColor = it.seedColor,
                                        isAmoled = isAmoled,
                                        isDark = isSystemInDarkTheme()
                                    )
                            )
                        }
                }
            }
        }
    }

    OutlinedCard(
        onClick = { showThemes = !showThemes },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text("Theme") },
            supportingContent = { Text(themeColor.name) },
        )
    }
}

@Composable
private fun ThemeItem(
    onClick: () -> Unit,
    selected: Boolean,
    themeColor: ThemeColor,
    colorScheme: ColorScheme,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            SelectableMiniPalette(
                selected = selected,
                colorScheme = colorScheme
            )

            Text(themeColor.name)
        }
    }
}

@Composable
private fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    colorScheme: ColorScheme,
) {
    SelectableMiniPalette(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        accents = remember(colorScheme) {
            listOf(
                TonalPalette.from(colorScheme.primary),
                TonalPalette.from(colorScheme.secondary),
                TonalPalette.from(colorScheme.tertiary)
            )
        }
    )
}

@Composable
private fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    accents: List<TonalPalette>,
) {
    val content: @Composable () -> Unit = {
        Box {
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset((-25).dp, 25.dp),
                color = Color(accents[1].tone(85)),
            ) {}
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset(25.dp, 25.dp),
                color = Color(accents[2].tone(75)),
            ) {}
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(animationSpec) + fadeIn(animationSpec),
                exit = scaleOut(animationSpec) + fadeOut(animationSpec),
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
    onClick?.let {
        Surface(
            onClick = onClick,
            modifier = modifier
                .padding(12.dp)
                .size(50.dp),
            shape = CircleShape,
            color = Color(accents[0].tone(60)),
        ) { content() }
    } ?: Surface(
        modifier = modifier
            .padding(12.dp)
            .size(50.dp),
        shape = CircleShape,
        color = Color(accents[0].tone(60)),
    ) { content() }
}
