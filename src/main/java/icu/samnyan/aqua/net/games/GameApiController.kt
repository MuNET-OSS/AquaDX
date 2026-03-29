package icu.samnyan.aqua.net.games

import ext.*
import icu.samnyan.aqua.net.BotProps
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.general.service.CardService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

abstract class GameApiController<T : IUserData>(val name: String, userDataClass: KClass<T>) {
    val musicMapping = resJson<Map<String, GenericMusicMeta>>("/meta/$name/music.json")
        ?.mapKeys { it.key.toInt() } ?: emptyMap()
    val logger = LoggerFactory.getLogger(javaClass)

    val itemMapping = resJson<Map<String, Map<String, GenericItemMeta>>>("/meta/$name/items.json") ?: emptyMap()

    abstract val us: AquaUserServices
    abstract val userDataRepo: GenericUserDataRepo<T>
    abstract val playlogRepo: GenericPlaylogRepo<*>
    abstract val userMusicRepo: GenericUserMusicRepo<*>
    abstract val shownRanks: List<Pair<Int, String>>

    abstract val settableFields: Map<String, (T, String) -> Unit>
    open val gettableFields: Set<String> = setOf()

    @Autowired lateinit var cardService: CardService

    @API("trend")
    abstract suspend fun trend(@RP username: String): List<TrendOut>
    @API("user-summary")
    abstract suspend fun userSummary(@RP username: String, @RP token: String?): GenericGameSummary

    @API("recent")
    suspend fun recent(@RP username: String): List<IGenericGamePlaylog> = us.cardByName(username) { card ->
        playlogRepo.findByUserCardExtId(card.extId)
    }

    @API("playlog")
    fun playlog(@RP id: Long): IGenericGamePlaylog = playlogRepo.findById(id).getOrNull() ?: (404 - "Playlog not found")

    val userDetailFields by lazy { userDataClass.gettersMap().let { vm ->
        (settableFields.keys.toSet() + gettableFields)
            .associateWith { k -> (vm[k] ?: error("Field $k not found")) }
    } }
    @API("user-detail")
    suspend fun userDetail(@RP username: String) = us.cardByName(username) { card ->
        val u = userDataRepo.findByCard(card) ?: (404 - "User not found")
        userDetailFields.toList().associate { (k, f) -> k to f.invoke(u) }
    }
    @API("user-detail-set")
    suspend fun userDetailSet(@RP token: String, @RP field: String, @RP value: String): Any {
        val prop = settableFields[field] ?: (400 - "Invalid field $field")

        return us.jwt.auth(token) { u ->
            val user = async { userDataRepo.findByCard(u.ghostCard) } ?: (404 - "User not found")
            prop(user, value)
            async { userDataRepo.save(user) }
            cardService.updateCardTimestamp(u.ghostCard, name)
            SUCCESS
        }
    }

    @API("user-option")
    open suspend fun userOption(@RP token: String): Any? = 400 - "Unsupported by this game"
    @API("user-option-set")
    open suspend fun userOptionSet(@RP token: String, @RP field: String, @RP value: Int): Any = 400 - "Unsupported by this game"

    @API("user-music-from-list")
    suspend fun userMusicFromList(@RP username: Str, @RB musicList: List<Int>) = us.cardByName(username) { card ->
        userMusicRepo.findByUser_Card_ExtIdAndMusicIdIn(card.extId, musicList)
    }

    fun genericUserSummary(card: Card, ratingComp: Map<String, String>, rival: Boolean? = null, favorites: List<Int>? = null): GenericGameSummary {
        // Summary values: total plays, player rating, server-wide ranking
        // number of each rank, max combo, number of full combo, number of all perfect
        val user = userDataRepo.findByCard(card) ?: (404 - "Game data not found")
        val plays = playlogRepo.findByUserCardExtId(card.extId)

        // Detailed ranks: Find the number of each rank in each level category
        // map<level, map<rank, count>>
        val rankMap = shownRanks.associate { (_, v) -> v to 0 }
        val detailedRanks = HashMap<Int, MutableMap<String, Int>>()
        plays.forEach { play ->
            val lvl = musicMapping[play.musicId]?.notes?.getOrNull(if (play.level == 10) 0 else play.level)?.lv ?: return@forEach
            shownRanks.find { (s, _) -> play.achievement > s }?.let { (_, v) ->
                val ranks = detailedRanks.getOrPut(lvl.toInt()) { rankMap.mut }
                ranks[v] = ranks[v]!! + 1
            }
        }

        // Collapse detailed ranks to get non-detailed ranks map<rank, count>
        val ranks = shownRanks.associate { (_, v) -> v to 0 }.mut.also { ranks ->
            plays.forEach { play ->
                shownRanks.find { (s, _) -> play.achievement > s }?.let { (_, v) -> ranks[v] = ranks[v]!! + 1 }
            }
        }

        // Server rank is now computed by Minato.Net's RankingService
        val serverRank = userDataRepo.findAllNonBanned().count { it.playerRating > user.playerRating }.long

        return GenericGameSummary(
            name = user.userName,
            aquaUser = card.aquaUser?.publicFields,
            serverRank = serverRank.long,
            accuracy = plays.acc(),
            rating = user.playerRating,
            ratingHighest = user.highestRating,
            ranks = ranks.map { (k, v) -> RankCount(k, v) },
            detailedRanks = detailedRanks,
            maxCombo = plays.maxOfOrNull { it.maxCombo } ?: 0,
            fullCombo = plays.count { it.isFullCombo },
            allPerfect = plays.count { it.isAllPerfect },
            totalScore = user.totalScore,
            plays = plays.size,
            totalPlayTime = plays.count() * 3L, // TODO: Give a better estimate
            joined = user.firstPlayDate.toString(),
            lastSeen = user.lastPlayDate.toString(),
            lastVersion = user.lastRomVersion,
            ratingComposition = ratingComp,
            recent = plays.sortedBy { it.userPlayDate.toString() }.takeLast(100).reversed(),
            lastPlayedHost = user.lastClientId?.let { us.userRepo.findByKeychip(it)?.username },
            rival = rival,
            favorites = favorites
        )
    }

    // Recommender System Integration
    @Autowired lateinit var botProps: BotProps
    // Map<userId, List<musicId>>
    var recommendedMusic: Map<Long, List<Int>> = emptyMap()
    private val tableName = when (name) { "mai2" -> "maimai2"; "chu3" -> "chusan"; else -> name }

    @API("recommender-fetch")
    fun recommenderFetchPlays(@RP botSecret: String) = run {
        if (botSecret != botProps.secret) 403 - "Invalid Secret"

        us.em.createNativeQuery("""
            SELECT user_id, music_id, count(*) as count
            FROM ${tableName}_user_playlog_view
            GROUP BY user_id, music_id;
        """.trimIndent()).exec.also {
            logger.info("Recommender fetched ${it.size} plays")
        }.numCsv("user_id", "music_id", "count")
    }

    @API("recommender-update")
    fun recommenderUpdate(@RP botSecret: String, @RB data: Map<Long, List<Int>>) {
        if (botSecret != botProps.secret) 403 - "Invalid Secret"
        recommendedMusic = data
        logger.info("Recommender updated with ${data.size} users")
    }

    @API("song-pop")
    fun songPopRanking() = us.pop.ranking[tableName]
}
