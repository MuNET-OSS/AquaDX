package icu.samnyan.aqua.sega.allnet

import ext.*
import icu.samnyan.aqua.net.db.AquaNetUserRepo
import icu.samnyan.aqua.sega.allnet.AllNetBillingDecoder.decodeAllNet
import icu.samnyan.aqua.sega.util.AquaConst
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Configuration
@ConfigurationProperties(prefix = "allnet.server")
class AllNetProps {
    var host: String = ""
    var port: Int? = null
    var hidePort: Boolean = true
    val keychipSesExpire: Long = 172800000 // milliseconds
    var checkKeychip: Boolean = false
    var keychipPermissiveForTesting: Boolean = false
    var redirect: String = "web"

    var placeName: String = ""
    var placeId: String = "123"
    var region0: String = "1"
    var regionName0: String = "W"
    var regionName1: String = "X"
    var regionName2: String = "Y"
    var regionName3: String = "Z"
    var regionCountry: String = "JPN"

    // Java assumes every application.properties as ISO-8859-1 (wtf), so we need to "correctly" convert it to UTF-8
    // More better way to this is to use XML or yaml format as these treated as UTF-8
    // but I rather use hack than breaking backward compatibility.. for now
    // TODO: Fix this
    val normalizedPlaceName: String by lazy {
        String(placeName.toByteArray(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
    }

    val map: Map<String, String> by lazy { mapOf(
        "stat" to "1",
        "name" to "",
        "place_id" to placeId,
        "region0" to region0,
        "region_name0" to regionName0,
        "region_name1" to regionName1,
        "region_name2" to regionName2,
        "region_name3" to regionName3,
        "country" to regionCountry,
        "nickname" to normalizedPlaceName,
    ) }
}

@Suppress("HttpUrlsUsage")
@RestController
class AllNet(
    val userRepo: AquaNetUserRepo,
    val keychipSessionService: KeychipSessionService,
    val keychipRepo: KeyChipRepo,
    val props: AllNetProps
) {
    private fun switchUri(hereAddr: Str, localPort: Str, gameId: Str, ver: Str, session: Str?): Str {
        val addr = hereAddr + (if (props.hidePort) "" else ":${props.port ?: localPort}")

        // If keychip authentication is enabled, the game URLs will be set to /gs/{token}/{game}/...
        val base = if (session != null) "gs/$session" else "g"

        return "http://$addr/$base/" + when (gameId) {
            "SDBT" -> "chu2/$ver/$session/"
            "SDHD" -> "chu3/$ver/"
            "SDGS" -> "chu3/$ver/" // International (c3exp)
            "SBZV" -> "diva/"
            "SDDT" -> "ongeki/$ver/"
            "SDEY" -> "mai/"
            "SDGA" -> "mai2/" // International (Exp)
            "SDGB" -> "mai2/" // International (China) - TODO: Test it
            "SDEZ" -> "mai2/"
            "SDFE" -> "wacca" // Note: Wacca must not end with a trailing slash
            "SDED" -> "card/"
            else -> ""
        }
    }

    companion object {
        val logger = logger()
    }
}

