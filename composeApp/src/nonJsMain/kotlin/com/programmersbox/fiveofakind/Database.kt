package com.programmersbox.fiveofakind

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

internal const val HIGHSCORE_LIMIT = 15

actual class YahtzeeDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
) {
    private val db = getRoomDatabase(builder)
    private val dao = db.getDao()

    actual fun getHighScores(): Flow<List<ActualYahtzeeScoreItem>> = dao
        .getYahtzeeHighScores()
        .map { list ->
            list.map {
                ActualYahtzeeScoreItem(
                    time = it.time,
                    ones = it.ones,
                    twos = it.twos,
                    threes = it.threes,
                    fours = it.fours,
                    fives = it.fives,
                    sixes = it.sixes,
                    threeKind = it.threeKind,
                    fourKind = it.fourKind,
                    fullHouse = it.fullHouse,
                    smallStraight = it.smallStraight,
                    largeStraight = it.largeStraight,
                    yahtzee = it.yahtzee,
                    chance = it.chance,
                )
            }
        }

    actual suspend fun addHighScore(scoreItem: ActualYahtzeeScoreItem) {
        dao.newHighScore(
            YahtzeeScoreItem(
                time = scoreItem.time,
                ones = scoreItem.ones,
                twos = scoreItem.twos,
                threes = scoreItem.threes,
                fours = scoreItem.fours,
                fives = scoreItem.fives,
                sixes = scoreItem.sixes,
                threeKind = scoreItem.threeKind,
                fourKind = scoreItem.fourKind,
                fullHouse = scoreItem.fullHouse,
                smallStraight = scoreItem.smallStraight,
                largeStraight = scoreItem.largeStraight,
                yahtzee = scoreItem.yahtzee,
                chance = scoreItem.chance,
            )
        )
    }

    actual suspend fun removeHighScore(scoreItem: ActualYahtzeeScoreItem) {
        dao.removeHighScore(scoreItem.time)
    }

    actual fun getHighScoreStats(): Flow<List<ActualYahtzeeScoreStat>> = dao
        .getYahtzeeStats()
        .map { stats ->
            stats.mapNotNull {
                val handType = runCatching { HandType.valueOf(it.handType) }
                    .getOrNull()
                    ?.displayName
                    ?: return@mapNotNull null

                ActualYahtzeeScoreStat(
                    handType = handType,
                    numberOfTimes = it.numberOfTimes,
                    totalPoints = it.totalPoints,
                )
            }
        }
}

@Database(
    entities = [YahtzeeScoreItem::class, YahtzeeScoreStat::class],
    version = 2,
    exportSchema = true,
    autoMigrations = []
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): YahtzeeDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        //.setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Dao
interface YahtzeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHighScore(scoreItem: YahtzeeScoreItem)

    @Query("DELETE FROM YahtzeeScoreItem WHERE time = :time")
    suspend fun removeHighScore(time: Long)

    @Query("SELECT * FROM YahtzeeScoreItem ORDER BY totalScore DESC")
    fun getYahtzeeHighScores(): Flow<List<YahtzeeScoreItem>>

    @Query("SELECT * FROM YahtzeeScoreItem ORDER BY totalScore DESC")
    suspend fun yahtzeeHighScores(): List<YahtzeeScoreItem>

    @Query("SELECT * FROM YahtzeeScoreStat")
    fun getYahtzeeStats(): Flow<List<YahtzeeScoreStat>>

    @Query("SELECT * FROM YahtzeeScoreStat")
    suspend fun yahtzeeStats(): List<YahtzeeScoreStat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStats(stats: YahtzeeScoreStat)

    @Ignore
    suspend fun newHighScore(scoreItem: YahtzeeScoreItem) {
        addHighScore(scoreItem)

        val scores = yahtzeeHighScores()
        if (scores.size > HIGHSCORE_LIMIT) {
            scores
                .drop(HIGHSCORE_LIMIT)
                .forEach { removeHighScore(it.time) }
        }
        updateScores(scoreItem)
    }

    @Ignore
    suspend fun updateScores(yahtzeeScoreItem: YahtzeeScoreItem) {
        val stats = yahtzeeStats()

        val list = mapOf(
            HandType.Ones to yahtzeeScoreItem::ones,
            HandType.Twos to yahtzeeScoreItem::twos,
            HandType.Threes to yahtzeeScoreItem::threes,
            HandType.Fours to yahtzeeScoreItem::fours,
            HandType.Fives to yahtzeeScoreItem::fives,
            HandType.Sixes to yahtzeeScoreItem::sixes,
            HandType.ThreeOfAKind to yahtzeeScoreItem::threeKind,
            HandType.FourOfAKind to yahtzeeScoreItem::fourKind,
            HandType.FullHouse to yahtzeeScoreItem::fullHouse,
            HandType.SmallStraight to yahtzeeScoreItem::smallStraight,
            HandType.LargeStraight to yahtzeeScoreItem::largeStraight,
            HandType.Chance to yahtzeeScoreItem::chance,
            HandType.FiveOfAKind to yahtzeeScoreItem::yahtzee,
        )

        for (i in list) {
            if (i.value.get() == 0) continue

            val newStat = stats
                .find { stat -> stat.handType == i.key.name }
                ?.let { stat ->
                    stat.copy(
                        numberOfTimes = stat.numberOfTimes + 1,
                        totalPoints = stat.totalPoints + i.value.get(),
                    )
                } ?: YahtzeeScoreStat(
                handType = i.key.name,
                numberOfTimes = 1,
                totalPoints = i.value.get().toLong(),
            )

            addStats(newStat)
        }
    }
}

@Entity(tableName = "YahtzeeScoreStat")
data class YahtzeeScoreStat(
    @PrimaryKey
    val handType: String,
    val numberOfTimes: Int = 0,
    val totalPoints: Long = 0L,
)

@Entity(tableName = "YahtzeeScoreItem")
data class YahtzeeScoreItem(
    @PrimaryKey
    val time: Long = Clock.System.now().toEpochMilliseconds(),
    val ones: Int = 0,
    val twos: Int = 0,
    val threes: Int = 0,
    val fours: Int = 0,
    val fives: Int = 0,
    val sixes: Int = 0,
    val threeKind: Int = 0,
    val fourKind: Int = 0,
    val fullHouse: Int = 0,
    val smallStraight: Int = 0,
    val largeStraight: Int = 0,
    val yahtzee: Int = 0,
    val chance: Int = 0,
    val smallScore: Int = ones + twos + threes + fours + fives + sixes,
    val largeScore: Int = threeKind + fourKind + fullHouse + smallStraight + largeStraight + yahtzee + chance,
    val totalScore: Int = largeScore + smallScore + if (smallScore >= 63) 35 else 0,
)
