package icu.samnyan.aqua.net.games

import ext.*
import icu.samnyan.aqua.net.db.AquaNetUser
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.utils.AquaNetProps
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.general.service.CardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass

data class ExportOptions(
    val playlogSince: String? = null
)

// Import class with renaming
data class ImportClass<T : Any>(
    val type: KClass<T>,
    val renames: Map<String, String?>? = null,
    val name: String = type.simpleName!!.removePrefix("Mai2").removePrefix("Chu3").lowercase()
)

interface IUserEntity<UserModel: IUserData> {
    var id: Long
    var user: UserModel
}

interface IExportClass<UserModel: IUserData> {
    var gameId: String
    var userData: UserModel
}

@NoRepositoryBean
interface IUserRepo<UserModel, ThisModel>: JpaRepository<ThisModel, Long> {
    fun findByUser(user: UserModel): List<ThisModel>
    fun findSingleByUser(user: UserModel): Optional<ThisModel>
}

/**
 * Import controller for a game
 *
 * @param game: 4-letter Game ID
 * @param gameName: mai2/chu3/ongeki
 * @param exportFields: Mapping of type names to variables in the export model
 *      (e.g. "Mai2UserCharacter" -> Mai2DataExport::userCharacterList)
 * @param exportRepos: Mapping of variables to repositories that can be used to find the data
 * @param artemisRenames: Mapping of Artemis table names to import classes
 */
abstract class ImportController<ExportModel: IExportClass<UserModel>, UserModel: IUserData>(
    val game: String,
    val gameName: String,
    val exportClass: KClass<ExportModel>,
    val exportFields: Map<String, Var<ExportModel, Any>>,
    val exportRepos: Map<Var<ExportModel, Any>, IUserRepo<UserModel, *>>,
    val artemisRenames: Map<String, ImportClass<*>>,
    val customExporters: Map<Var<ExportModel, Any>, (UserModel, ExportOptions) -> Any?> = emptyMap(),
    val customImporters: Map<Var<ExportModel, Any>, (ExportModel, UserModel) -> Unit> = emptyMap()
) {
    abstract fun createEmpty(): ExportModel
    abstract val userDataRepo: GenericUserDataRepo<UserModel>

    @Autowired lateinit var us: AquaUserServices
    @Autowired lateinit var netProps: AquaNetProps
    @Autowired lateinit var transManager: PlatformTransactionManager
    val trans by lazy { TransactionTemplate(transManager) }
    @Autowired lateinit var cardService: CardService

    init {
        artemisRenames.values.forEach {
            if (it.name !in exportFields) error("Code error! Export fields incomplete: missing ${it.name}")
        }
    }

    val listRepos = exportRepos.filter { it.key returns List::class }
    val singleRepos = exportRepos.filter { !(it.key returns List::class) }

    fun export(u: AquaNetUser): ExportModel = export(u.ghostCard, ExportOptions())

    fun export(c: Card, options: ExportOptions) = createEmpty().apply {
        gameId = game
        userData = userDataRepo.findByCard(c) ?: (404 - "User not found")
        exportRepos.forEach { (f, u) ->
            if (f returns List::class) f.set(this, u.findByUser(userData))
            else u.findSingleByUser(userData)()?.let { f.set(this, it) }
        }
        customExporters.forEach { (f, exporter) ->
            exporter(userData, options)?.let { f.set(this, it) }
        }
    }

    @API("export")
    fun exportUserData(@RP token: Str) = us.jwt.auth(token) { u ->
        log.info("Exporting user data for ${u.auId}")
        export(u)
    }

    companion object
    {
        // Map a dictionary to a class
        fun <T : Any> ImportClass<T>.mapTo(rawDict: Map<String, String>): T {
            // Process renaming
            var dict = renames?.let { rawDict
                .filter { (k, _) -> if (k in it) it[k] != null else true }
                .mapKeys { (k, _) -> it[k] ?: k } } ?: rawDict

            // Process Nones
            dict = dict.filterValues { it != "None" }

            return JACKSON_ARTEMIS.convertValue(dict, type.java)
        }

        val log = logger()
    }
}
