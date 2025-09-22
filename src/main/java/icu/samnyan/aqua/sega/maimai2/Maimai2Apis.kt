@file:Suppress("UNCHECKED_CAST")

package icu.samnyan.aqua.sega.maimai2

import ext.*
import icu.samnyan.aqua.sega.allnet.TokenChecker.Companion.getCurrentSession
import icu.samnyan.aqua.sega.general.model.CardStatus
import icu.samnyan.aqua.sega.maimai2.model.UserRivalMusic
import icu.samnyan.aqua.sega.maimai2.model.UserRivalMusicDetail
import icu.samnyan.aqua.sega.maimai2.model.userdata.Mai2UserKaleidx
import java.time.LocalDate

fun Maimai2ServletController.initApis() {
    "GetUserExtend" { mapOf(
        "userId" to uid,
        "userExtend" to (db.userExtend.findSingleByUser_Card_ExtId(uid)() ?: (404 - "User not found"))
    ) }

    "GetUserData" { mapOf(
        "userId" to uid,
        "userData" to (db.userData.findByCardExtId(uid)() ?: (404 - "User not found")),
        "banState" to 0
    ) }

    "GetUserLoginBonus".unpaged { db.userLoginBonus.findByUser_Card_ExtId(uid) }
    "GetUserMap".unpaged { db.userMap.findByUser_Card_ExtId(uid) }
    "GetUserCard".unpaged { db.userCard.findByUser_Card_ExtId(uid) }
    "GetUserCharge".unpaged { db.userCharge.findByUser_Card_ExtId(uid) }
    "GetUserFriendSeasonRanking".unpaged { db.userFriendSeasonRanking.findByUser_Card_ExtId(uid) }
    "GetUserCourse".unpaged { db.userCourse.findByUser_Card_ExtId(uid) }

    "GetUserMusic".unpaged {
        ls(mapOf("userMusicDetailList" to db.userMusicDetail.findByUser_Card_ExtId(uid)))
    }

    "GetUserFavorite" { mapOf(
        "userId" to uid,
        "userFavorite" to db.userFavorite.findByUser_Card_ExtIdAndItemKind(uid, data["itemKind"] as Int)
    ) }

    "GetUserActivity" {
        db.userAct.findByUser_Card_ExtId(uid).let { act -> mapOf(
            "userActivity" to mapOf(
                "playList" to act.filter { it.kind == 1 }.take(200),
                "musicList" to act.filter { it.kind == 2 }.take(200)
            )
        ) }
    }

    // Maimai only request for event type 1
    "GetGameEvent" static { mapOf("type" to 1, "gameEventList" to db.gameEvent.findByEnable(true)) }
    "GetGameCharge" static { db.gameCharge.findAll().let { mapOf("length" to it.size, "gameChargeList" to it) } }

    "GetUserOption" { mapOf(
        "userId" to uid,
        "userOption" to (db.userOption.findSingleByUser_Card_ExtId(uid)() ?: (404 - "User not found"))
    ) }

    "CreateToken" static { """{"Bearer":"meow"}""" }

    "CMUpsertUserPrintlog" static { """{"returnCode":1,"orderId":"0","serialId":"FAKECARDIMAG12345678"}""" }

    "CMGetSellingCard" static { db.gameSellingCard.findAll().let {
        mapOf("length" to it.size, "sellingCardList" to it)
    } }

    "CMGetUserCharacter" { db.userCharacter.findByUser_Card_ExtId(uid).let {
        mapOf(
            "returnCode" to 1,
            "length" to it.size,
            "userCharacterList" to it
        )
    } }

    "CMGetUserPreview" { db.userData.findByCardExtId(uid)()?.let {
        mapOf(
            "userId" to uid,
            "userName" to it.userName,
            "rating" to it.playerRating,
            "lastDataVersion" to it.lastDataVersion,
            "isLogin" to false,
            "isExistSellingCard" to false
        )
    } ?: (404 - "User not found") }

    "GetUserPreview" {
        val d = db.userData.findByCardExtId(uid)() ?: (404 - "User not found")
        val option = db.userOption.findSingleByUser_Card_ExtId(uid)()

        val res = mutableMapOf(
            "userId" to uid,
            "userName" to d.userName,
            "isLogin" to false,
            "lastGameId" to d.lastGameId,
            "lastDataVersion" to d.lastDataVersion,
            "lastRomVersion" to d.lastRomVersion,
            "lastLoginDate" to d.lastPlayDate,
            "lastPlayDate" to d.lastPlayDate,
            "playerRating" to d.playerRating,
            "nameplateId" to d.plateId,
            "iconId" to d.iconId,
            "trophyId" to 0,
            "partnerId" to d.partnerId,
            "frameId" to d.frameId,
            "totalAwake" to d.totalAwake,
            "isNetMember" to d.isNetMember,
            "dailyBonusDate" to d.dailyBonusDate,
            "headPhoneVolume" to (option?.headPhoneVolume ?: 0),
            "dispRate" to (option?.dispRate ?: 0),
            "isInherit" to false,
            "banState" to d.banState
        )

        if (d.card?.status == CardStatus.MIGRATED_TO_MINATO) {
            res["userName"] = "Migrated"
            res["dispRate"] = 1
            res["playerRating"] = 66564
            res["totalAwake"] = 7114
        }

        res
    }

    "UserLogin" {
        val d = db.userData.findByCardExtId(uid)()

        val res = mutableMapOf(
            "returnCode" to 1, "loginCount" to 1,
            "lastLoginDate" to "2020-01-01 00:00:00.0",
            "consecutiveLoginCount" to 0, "loginId" to 1,
            "Bearer" to "meow", "bearer" to "meow"
        )

        if (d?.card?.status == CardStatus.MIGRATED_TO_MINATO) {
            res["returnCode"] = 0
        }

        res
    }

    "GetUserShopStock" {
        val shopItemIdList = data["shopItemIdList"] as List<*>

        mapOf(
            "userId" to uid,
            "userShopStockList" to shopItemIdList.map { mapOf(
                "shopItemId" to it,
                "tradeCount" to 0
            ) }
        )
    }

    "GetUserRivalData" {
        val rivalId = parsing { data["rivalId"]!!.long }

        // rivalId should store and fetch with the id column of table rather than card_ext_id
        // or user will be able to get others' ext_id by setting them as rival
        mapOf(
            "userId" to uid,
            "userRivalData" to mapOf(
                "rivalId" to rivalId,
                "rivalName" to (db.userData.findById(rivalId)()?.userName ?: "")
            )
        )
    }

    "GetUserRivalMusic" {
        val rivalId = parsing { data["rivalId"]!!.long }

        val lst = db.userMusicDetail.findByUserId(rivalId)
        val res = lst.associate { it.musicId to UserRivalMusic(it.musicId) }

        lst.forEach {
            res[it.musicId]!!.userRivalMusicDetailList.add(
                UserRivalMusicDetail(it.level, it.achievement, it.deluxscoreMax)
            )
        }

        mapOf("userId" to uid, "rivalId" to rivalId, "nextIndex" to 0, "userRivalMusicList" to res.values)
    }

    "GetUserIntimate".unpaged {
        val u = db.userData.findByCardExtId(uid)() ?: (404 - "User not found")
        db.userIntimate.findByUser(u)
    }

    // Empty List Handlers
    "GetUserRegion".unpaged { empty }
    "GetUserGhost".unpaged { empty }
    "GetUserFriendBonus" { mapOf("userId" to uid, "returnCode" to 0, "getMiles" to 0) }
    "GetTransferFriend" { mapOf("userId" to uid, "transferFriendList" to empty) }
    "GetUserNewItem" { mapOf("userId" to uid, "itemKind" to 0, "itemId" to 0) }

    "GetUserCardPrintError" static { mapOf("length" to 0, "userPrintDetailList" to empty) }
    "GetUserFriendCheck" static { mapOf("returnCode" to 0) }
    "UserFriendRegist" static { mapOf("returnCode1" to 0, "returnCode2" to 0) }
    "GetGameNgMusicId" static { mapOf("length" to 0, "musicIdList" to empty) }
    "GetGameTournamentInfo" static { mapOf("length" to 0, "gameTournamentInfoList" to empty) }

    // Kaleidoscope, added on 1.50
    // [{gateId, phaseId}]
    "GetGameKaleidxScope" { mapOf("gameKaleidxScopeList" to ls(
        mapOf("gateId" to 1, "phaseId" to 5),
        mapOf("gateId" to 2, "phaseId" to 5),
        mapOf("gateId" to 3, "phaseId" to 5),
        mapOf("gateId" to 4, "phaseId" to 5),
        mapOf("gateId" to 5, "phaseId" to 5),
        mapOf("gateId" to 6, "phaseId" to 5),
        mapOf("gateId" to 7, "phaseId" to 5),
        mapOf("gateId" to 8, "phaseId" to 5),
        mapOf("gateId" to 9, "phaseId" to 5),
        mapOf("gateId" to 10, "phaseId" to 7),
    )) }
    // Request: {userId}
    // Response: {userId, userKaleidxScopeList}
    "GetUserKaleidxScope".unpaged {
        val u = db.userData.findByCardExtId(uid)() ?: (404 - "User not found")
        db.userKaleidx.findByUser(u)
    }
    // Request: {userId, version, userData: [UserDetail], userPlaylogList: [UserPlaylog]}
    // Response: {userId, userItemList: [UserItem]}
    // Added on 1.50
    "GetUserNewItemList" { mapOf("userId" to uid, "userItemList" to empty) }

    // Added on 1.50 (export only)
    "UploadUserPlaylogList" {
        val lst = parsing { data["userPlaylogList"] as List<JDict> }
        if (lst.size > 40) (400 - "Too many playlogs")
        lst.forEach {
            uploadUserPlaylog.handle(mapOf("userId" to uid, "userPlaylog" to it))
        }
        """{"returnCode":1,"apiName":"com.sega.maimai2servlet.api.UploadUserPlaylogListApi"}"""
    }

    "GetGameSetting" api@{
        // The client-side implementation for reboot time is extremely cursed.
        // Only hour and minute are used, date is discarded and second is set to 0.
        // The time is adjusted to the next day if it's 12 hours or more from now.
        // And it's using local timezone instead of treating it as UTC.
        // The official maimai cabs will reboot every day, but we don't want that
        // So, we need to return the hour and minute 5 hours ago
        // val rebootStart = Instant.now().atZone(ZoneId.of("Asia/Tokyo")).minusHours(5)
        // val rebootEnd = rebootStart.plusSeconds(60)
        // Nope that didn't work

        val defaultSettings = mapOf(
            "isAouAccession" to true,
            "gameSetting" to mapOf(
//                "rebootStartTime" to GAME_SETTING_DATE_FMT.format(rebootStart),
//                "rebootEndTime" to GAME_SETTING_DATE_FMT.format(rebootEnd),
                "rebootStartTime" to "2020-01-01 23:59:00.0",
                "rebootEndTime" to "2020-01-01 23:59:00.0",
                "rebootInterval" to 0,

                // Fields below doesn't seem to be used by the client at all
                "isMaintenance" to false,
                "requestInterval" to 10,
                "movieUploadLimit" to 0,
                "movieStatus" to 0,
                "movieServerUri" to "",
                "deliverServerUri" to "",
                "oldServerUri" to "",
                "usbDlServerUri" to "",

                // Fields below are SDGB-specific settings (not present in SDEZ)
                "pingDisable" to true,
                "packetTimeout" to 20_000,
                "packetTimeoutLong" to 60_000,
                "packetRetryCount" to 5,
                "userDataDlErrTimeout" to 300_000,
                "userDataDlErrRetryCount" to 5,
                "userDataDlErrSamePacketRetryCount" to 5,
                "userDataUpSkipTimeout" to 0,
                "userDataUpSkipRetryCount" to 0,
                "iconPhotoDisable" to true,
                "uploadPhotoDisable" to false,
                "maxCountMusic" to 0,
                "maxCountItem" to 0
            ),
        )

        try {
            val file = java.io.File("/app/GameSettingBlackListAuids.json")
            if (file.exists()) {
                val jsonSettings: List<Number> = file.readText().json() ?: emptyList()
                val currentAuId = getCurrentSession()?.user?.auId
                if (currentAuId != null && jsonSettings.any { it.toLong() == currentAuId }) {
                    return@api defaultSettings
                }
            }
        } catch (e: Exception) {
            // 如果读取失败，继续使用默认设置
            e.printStackTrace()
        }

        try {
            val file = java.io.File("/app/GameSetting.json")
            if (file.exists()) {
                val jsonSettings = file.readText().jsonMap()
                return@api defaultSettings + jsonSettings
            }
        } catch (e: Exception) {
            // 如果读取失败，继续使用默认设置
            e.printStackTrace()
        }

        defaultSettings
    }

    "GetServerAnnouncement" static { mapOf(
        "title" to "",
        "announcement" to "",
        "showOnIdle" to false,
        "showOnUserLogin" to false,
        "imageUrl" to "",
    ) }

    "GetGameWeeklyData" static { mapOf(
        "gameWeeklyData" to mapOf(
            "missionCategory" to 0,
            "updateDate" to "2024-01-01 00:00:00.0",
            "beforeDate" to "2077-01-01 00:00:00.0"
        )
    ) }

    "GetUserMissionData" { mapOf(
        "userId" to uid,
        "userWeeklyData" to mapOf (
            "lastLoginWeek" to "",
            "beforeLoginWeek" to "",
            "friendBonusFlag" to false
        ),
        "userMissionDataList" to empty
    ) }

    // Added on 1.50
    // This unfortunately cannot be used for custom charts, because sound data is not present.
    "GetGameMusicScore" { mapOf(
        "gameMusicScore" to mapOf(
            "musicId" to data["musicId"],
            "level" to data["level"],
            "type" to data["type"],
            "scoreData" to ""
        )
    ) }

    // Recommendation
    "GetUserRecommendRateMusic" { mapOf(
        "userId" to uid,
        "userRecommendRateMusicIdList" to empty // TODO
    ) }

    "GetUserRecommendSelectMusic" {
        val user = db.userData.findByCard_ExtId(uid)() ?: (404 - "User not found")
        mapOf(
            "userId" to uid,
            "userRecommendSelectionMusicIdList" to (net.recommendedMusic[user.id] ?: empty)
        )
    }

    // CIRCLE
    "GetGameFesta" static { mapOf(
        "gameFestaData" to mapOf(
            "eventId" to 0,
            "isRallyPeriod" to false,
            "isCircleJoinNotAllowed" to true,
            "jackingFestaSideId" to 0,
            "festaSideDataList" to empty
        ),
        "gameResultFestaData" to mapOf(
            "eventId" to 0,
            "resultFestaSideDataList" to empty
        )
    ) }

    "GetPlaceCircleData" static { mapOf(
        "returnCode" to 0,
        "circleId" to 0,
        "aggrDate" to ""
    ) }

    "GetUserCircleData" static { mapOf(
        "circleId" to 0,
        "circleName" to "",
        "isPlace" to false,
        "circleClass" to 0,
        "lastLoginDate" to "",
        "circlePointRankingList" to empty
    ) }

    "GetUserCirclePointData" static { mapOf(
        "userId" to 0,
        "aggrDate" to "",
        "userCirclePointDataList" to empty
    ) }

    "GetUserCirclePointRanking" static { mapOf(
        "circleId" to 0,
        "lastMonthCircleRank" to 0,
        "lastMonthPoint" to 0,
        "circleName" to "",
        "aggrDate" to ""
    ) }

    "GetUserFesta" static { mapOf(
        "userFestaData" to mapOf(
            "eventId" to 0,
            "circleId" to 0L,
            "festaSideId" to 0,
            "circleTotalFestaPoint" to 0,
            "currentTotalFestaPoint" to 0,
            "circleRankInFestaSide" to 0,
            "circleRecordDate" to "",
            "isDailyBonus" to false,
            "participationRewardGet" to false,
            "receivedRewardBorder" to 0
        ),
        "userResultFestaData" to mapOf(
            "eventId" to 0,
            "circleId" to 0L,
            "circleName" to "",
            "festaSideId" to 0,
            "circleRankInFestaSide" to 0,
            "receivedRewardBorder" to 0,
            "circleTotalFestaPoint" to 0,
            "resultRewardGet" to false
        )
    ) }

}