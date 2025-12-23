package icu.samnyan.aqua.net

import ext.*
import icu.samnyan.aqua.net.components.*
import icu.samnyan.aqua.net.db.*
import icu.samnyan.aqua.net.utils.PathProps
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.general.dao.CardRepository
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
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
        async {
            validator.update(u, key, value)

            // Save the user
            userRepo.save(u)
        }

        SUCCESS
    }
}
