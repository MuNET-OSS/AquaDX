package icu.samnyan.aqua.net

import ext.*
import icu.samnyan.aqua.net.components.JWT
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.games.GenericUserDataRepo
import icu.samnyan.aqua.net.games.IUserData
import icu.samnyan.aqua.net.utils.AquaNetProps
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.chusan.model.Chu3UserDataRepo
import icu.samnyan.aqua.sega.general.dao.CardRepository
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.general.service.CardService
import icu.samnyan.aqua.sega.maimai2.model.Mai2UserDataRepo
import icu.samnyan.aqua.sega.ongeki.OgkUserDataRepo
import icu.samnyan.aqua.sega.wacca.model.db.WcUserRepo
import jakarta.persistence.EntityManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

@RestController
@API("/api/v2/card")
class CardController(
    val jwt: JWT,
    val us: AquaUserServices,
    val cardService: CardService,
    val cardGameService: CardGameService,
    val props: AquaNetProps,
) {
    companion object {
        val log = logger()
    }

    @API("/user-games")
    @Doc("Get the game summary of the user, including the user's name, rating, and last login date.", "Summary of the user")
    suspend fun userGames(@RP username: Str) = us.cardByName(username) { card -> cardGameService.getSummary(card) }
}

suspend fun getSummaryFor(repo: GenericUserDataRepo<*>, card: Card): Map<Str, Any>? {
    val data = async { repo.findByCard(card) } ?: return null
    return mapOf(
        "name" to data.userName,
        "rating" to data.playerRating,
        "lastLogin" to data.lastPlayDate,
    )
}

@Service
class CardGameService(
    val maimai2: Mai2UserDataRepo,
    val chusan: Chu3UserDataRepo,
    val wacca: WcUserRepo,
    val ongeki: OgkUserDataRepo,
    val diva: icu.samnyan.aqua.sega.diva.dao.userdata.PlayerProfileRepository,
    val cardRepo: CardRepository,
    val em: EntityManager,
    val cardService: CardService
) {
    companion object {
        val log = logger()
    }

    suspend fun getSummary(card: Card) = async {
        mapOf(
            "mai2" to getSummaryFor(maimai2, card),
            "chu3" to getSummaryFor(chusan, card),
            "ongeki" to getSummaryFor(ongeki, card),
            "wacca" to getSummaryFor(wacca, card),
            "diva" to diva.findByPdId(card.extId).getOrNull()?.let {
                mapOf(
                    "name" to it.playerName,
                    "rating" to it.level,
                )
            },
        )
    }
}
