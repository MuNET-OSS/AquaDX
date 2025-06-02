package icu.samnyan.aqua.net

import ext.*
import icu.samnyan.aqua.net.components.*
import icu.samnyan.aqua.net.db.*
import icu.samnyan.aqua.net.db.AquaUserServices.Companion.SETTING_FIELDS
import icu.samnyan.aqua.net.utils.PathProps
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.general.dao.CardRepository
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.general.model.CardStatus
import icu.samnyan.aqua.sega.general.service.CardService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.LocalDateTime
import kotlin.io.path.writeBytes

@RestController
@API("/api/v2/user")
class UserRegistrar(
    val userRepo: AquaNetUserRepo,
    val jwt: JWT,
    val cardRepo: CardRepository,
    val validator: AquaUserServices,
    final val paths: PathProps
) {
    val portraitPath = paths.aquaNetPortrait.path()

    companion object {
        // Random long with length 9-10
        // We chose 1e9 as the start because normal cards took 0...1e9-1
        // This is because games can only take uint32 for card ID, which is at max 10 digits (4294967295)
        const val cardExtIdStart = 1e9.toLong()
        // Actually, let's not use the UInt32 max but use signed int32 max instead, because Wacca doesn't support uint32
        // const val cardExtIdEnd = 4294967295
        // This range already gives us 1147483647 users, which is more than enough for now
        const val cardExtIdEnd = Int.MAX_VALUE.toLong()

        val log = LoggerFactory.getLogger(UserRegistrar::class.java)
    }

    @API("/me")
    @Doc("Get the information of the current logged-in user.", "User information")
    suspend fun getUser(@RP token: Str) = jwt.auth(token)

    @API("/user-info")
    @Doc("Get the information of a user by username.", "User information")
    fun getUserInfo(@RP username: Str) =
        userRepo.findByUsernameIgnoreCase(username)?.publicFields ?: (404 - "User not found")

    @API("/setting")
    @Doc("Validate and set a user setting field.", "Success message")
    suspend fun setting(@RP token: Str, @RP key: Str, @RP value: Str) = jwt.auth(token) { u ->
        // Check if the key is a settable field
        val field = SETTING_FIELDS.find { it.name == key } ?: (400 - "Invalid setting")

        async {
            // Set the validated field
            field.setter.call(u, field.checker.call(validator, value))

            // Save the user
            userRepo.save(u)
        }

        SUCCESS
    }

    val keychipRange = 1e9.toULong()..1e10.toULong() - 1UL

    @API("/keychip")
    @Doc("Get a Keychip ID so that the user can connect to the server.", "Success message")
    suspend fun setupConnection(@RP token: Str) = jwt.auth(token) { u ->
        u.keychip?.let { return mapOf("keychip" to it) }
        log.info("Net: /user/keychip setup: ${u.auId} for ${u.username}")

        // Generate a keychip id with 10 digits (e.g. A1234567890)
        var new = "A" + keychipRange.random()
        while (async { userRepo.findByKeychip(new) != null }) new = "A" + keychipRange.random()
        async { userRepo.save(u.apply { keychip = new }) }

        mapOf("keychip" to new)
    }

    @API("/upload-pfp", consumes = ["multipart/form-data"])
    @Doc("Upload a profile picture for the user.", "Success message")
    suspend fun uploadPfp(@RP token: Str, @RP file: MultipartFile) = jwt.auth(token) { u ->
        // Processing the image would lead to many open factors for attack
        // (e.g. the JFIF Pixel Flood attack that ImageIO is vulnerable to)
        // So we check file magic, then store the image without any processing
        val bytes = file.bytes
        val mime = TIKA.detect(bytes) ?: (400 - "Invalid file type")

        // Check if the file is an image
        if (!mime.startsWith("image/")) 400 - "Invalid file type"

        // Save the image
        val name = "${u.auId}${MIMES.forName(mime)?.extension ?: ".jpg"}"
        async {
            (portraitPath / name).writeBytes(bytes)
            userRepo.save(u.apply { profilePicture = name })
        }

        SUCCESS
    }
}
