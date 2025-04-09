package com.programmersbox.fiveofakind

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
            stats.map {
                ActualYahtzeeScoreStat(
                    handType = it.handType,
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

    @Query("SELECT * FROM YahtzeeScoreItem ORDER BY totalScore")
    fun getYahtzeeHighScores(): Flow<List<YahtzeeScoreItem>>

    @Query("SELECT * FROM YahtzeeScoreItem ORDER BY totalScore")
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
                .chunked(HIGHSCORE_LIMIT)
                .drop(1)
                .flatten()
                .forEach { removeHighScore(it.time) }
        }
        updateScores(scoreItem)
    }

    @Ignore
    suspend fun updateScores(yahtzeeScoreItem: YahtzeeScoreItem) {
        val stats = yahtzeeStats()

        val list = listOf(
            yahtzeeScoreItem::ones,
            yahtzeeScoreItem::twos,
            yahtzeeScoreItem::threes,
            yahtzeeScoreItem::fours,
            yahtzeeScoreItem::fives,
            yahtzeeScoreItem::sixes,
            yahtzeeScoreItem::threeKind,
            yahtzeeScoreItem::fourKind,
            yahtzeeScoreItem::fullHouse,
            yahtzeeScoreItem::smallStraight,
            yahtzeeScoreItem::largeStraight,
            yahtzeeScoreItem::chance,
        )

        for(i in list) {
            if(i.get() == 0) continue

            val newStat = stats
                .find { stat -> stat.handType == i.name }
                ?.let { stat ->
                    stat.copy(
                        numberOfTimes = stat.numberOfTimes + 1,
                        totalPoints = stat.totalPoints + i.get(),
                    )
                } ?: YahtzeeScoreStat(
                handType = i.name,
                numberOfTimes = 1,
                totalPoints = i.get().toLong(),
            )

            addStats(newStat)
        }
    }
}

/*internal class YahtzeeDatabase(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    YahtzeeHighScores::class,
                    YahtzeeScoreItem::class,
                    YahtzeeScoreStat::class
                )
            )
                .schemaVersion(27)
                .name(name)
                .migration({ })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val yahtzeeHighScores: YahtzeeHighScores = realm.initDbBlocking { YahtzeeHighScores() }

    suspend fun addHighScore(scoreItem: YahtzeeScoreItem) {
        scoreItem.setScores()
        realm.write {
            copyToRealm(scoreItem)

            val scores = query<YahtzeeScoreItem>()
                .sort("totalScore", Sort.DESCENDING)
                .find()

            if (scores.size > HIGHSCORE_LIMIT) {
                scores.chunked(HIGHSCORE_LIMIT)
                    .drop(1)
                    .flatten()
                    .mapNotNull { findLatest(it) }
                    .forEach { delete(it) }
            }

            query<YahtzeeHighScores>()
                .first()
                .find()
                ?.let { updateScoreStats(it, scoreItem) }
        }
    }

    private fun updateScoreStats(yahtzeeHighScores: YahtzeeHighScores, scoreItem: YahtzeeScoreItem) {
        val scoreToStatMap = mapOf(
            scoreItem::ones to yahtzeeHighScores::ones,
            scoreItem::twos to yahtzeeHighScores::twos,
            scoreItem::threes to yahtzeeHighScores::threes,
            scoreItem::fours to yahtzeeHighScores::fours,
            scoreItem::fives to yahtzeeHighScores::fives,
            scoreItem::sixes to yahtzeeHighScores::sixes,
            scoreItem::threeKind to yahtzeeHighScores::threeKind,
            scoreItem::fourKind to yahtzeeHighScores::fourKind,
            scoreItem::fullHouse to yahtzeeHighScores::fullHouse,
            scoreItem::smallStraight to yahtzeeHighScores::smallStraight,
            scoreItem::largeStraight to yahtzeeHighScores::largeStraight,
            scoreItem::chance to yahtzeeHighScores::chance,
        )

        for ((scoreProperty, statProperty) in scoreToStatMap) {
            statProperty.get()?.let { updateStatIfNonZero(scoreProperty.get(), it) }
        }

        if (yahtzeeHighScores.yahtzee != null) {
            if (scoreItem.yahtzee != 0) {
                val yahtzeeScore = (scoreItem.yahtzee - 50) / 100
                yahtzeeHighScores.yahtzee!!.numberOfTimes++
                repeat(yahtzeeScore) {
                    yahtzeeHighScores.yahtzee!!.numberOfTimes++
                }
                yahtzeeHighScores.yahtzee!!.totalPoints += scoreItem.yahtzee
            }
        }
    }

    private fun updateStatIfNonZero(score: Int, stat: YahtzeeScoreStat) {
        if (score != 0) {
            stat.numberOfTimes++
            stat.totalPoints += score
        }
    }

    suspend fun removeHighScore(scoreItem: YahtzeeScoreItem) {
        realm.write { findLatest(scoreItem)?.let { delete(it) } }
    }

    fun getYahtzeeHighScores() = realm.query<YahtzeeScoreItem>()
        .sort("totalScore", Sort.DESCENDING)
        .asFlow()
        .mapNotNull { it.list }

    fun getYahtzeeStats() = yahtzeeHighScores
        .asFlow()
        .mapNotNull { it.obj }

    suspend fun updateScores() {
        realm.write {
            realm.query<YahtzeeScoreItem>()
                .sort("totalScore", Sort.DESCENDING)
                .find()
                .forEach { it.setScores() }
        }
    }
}*/

/*internal class YahtzeeHighScores : RealmObject {
    var ones: YahtzeeScoreStat? = YahtzeeScoreStat()
    var twos: YahtzeeScoreStat? = YahtzeeScoreStat()
    var threes: YahtzeeScoreStat? = YahtzeeScoreStat()
    var fours: YahtzeeScoreStat? = YahtzeeScoreStat()
    var fives: YahtzeeScoreStat? = YahtzeeScoreStat()
    var sixes: YahtzeeScoreStat? = YahtzeeScoreStat()

    var threeKind: YahtzeeScoreStat? = YahtzeeScoreStat()
    var fourKind: YahtzeeScoreStat? = YahtzeeScoreStat()
    var fullHouse: YahtzeeScoreStat? = YahtzeeScoreStat()
    var smallStraight: YahtzeeScoreStat? = YahtzeeScoreStat()
    var largeStraight: YahtzeeScoreStat? = YahtzeeScoreStat()
    var yahtzee: YahtzeeScoreStat? = YahtzeeScoreStat()
    var chance: YahtzeeScoreStat? = YahtzeeScoreStat()
}*/

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

/*
fun RealmInstant.toInstant(): Instant {
    val sec: Long = this.epochSeconds
    // The value always lies in the range `-999_999_999..999_999_999`.
    // minus for timestamps before epoch, positive for after
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) { // For positive timestamps, conversion can happen directly
        Instant.fromEpochSeconds(sec, nano.toLong())
    } else {
        // For negative timestamps, RealmInstant starts from the higher value with negative
        // nanoseconds, while Instant starts from the lower value with positive nanoseconds
        // TODO This probably breaks at edge cases like MIN/MAX
        Instant.fromEpochSeconds(sec - 1, 1_000_000 + nano.toLong())
    }
}

fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSeconds
    // The value is always positive and lies in the range `0..999_999_999`.
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) { // For positive timestamps, conversion can happen directly
        RealmInstant.from(sec, nano)
    } else {
        // For negative timestamps, RealmInstant starts from the higher value with negative
        // nanoseconds, while Instant starts from the lower value with positive nanoseconds
        // TODO This probably breaks at edge cases like MIN/MAX
        RealmInstant.from(sec + 1, -1_000_000 + nano)
    }
}*/
