package eu.warfaremc.wclasses.implementation

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import eu.warfaremc.wclasses.WClassesAPI
import eu.warfaremc.wclasses.database
import eu.warfaremc.wclasses.instance
import org.jetbrains.annotations.NotNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.util.*

var cache: Cache<UUID, WClassesAPI.HeroObject> = CacheBuilder.newBuilder().maximumSize(150).build()

class WClassesAPIStdImpl(database: Database) : WClassesAPI {

    init {
        transaction(database) {
            try {
                SchemaUtils.create(PlayerProfiles)
            } catch (ex: Exception) {
                instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
            }
        }
    }

    override fun get(uid: UUID?): @NotNull Optional<WClassesAPI.HeroObject> {
        if (uid == null)
            return Optional.empty()

        val cached = cache.getIfPresent(uid)
        if (cached != null)
            return Optional.of(cached)

        val profile = transaction(database) {
            try {
                PlayerProfiles.select { PlayerProfiles.uid eq uid.toString() }.firstOrNull()
            } catch (ex: Exception) {
                instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
                return@transaction null
            }
        } ?: return Optional.empty()

        return Optional.of(
            WClassesAPI.HeroObject(uid, profile[PlayerProfiles.`class`])
        ).also { cache.put(uid, it.get()) }
    }

    override fun put(uid: UUID?): Boolean {
        val record = cache.getIfPresent(uid ?: return false) ?: return false
        transaction(database) {
            try {
                PlayerProfiles.update {
                    it[PlayerProfiles.uid] = record.uid.toString()
                    it[PlayerProfiles.`class`] = record.heroClass
                }
            } catch (ex: Exception) {
                instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
            }
        }
        return true
    }

    override fun putAll(): Boolean {
        val copy = cache.asMap();
        cache.invalidateAll()
        copy.forEach { record ->
            transaction(database) {
                try {
                    PlayerProfiles.update {
                        it[PlayerProfiles.uid] = record.key.toString()
                        it[PlayerProfiles.`class`] = record.value.heroClass
                    }
                } catch (ex: Exception) {
                    instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
                }

            }
        }
        return true //FIXME java.lang.NoClassDefFoundError: kotlin/jvm/internal/Ref$BooleanRef
    }

    override fun getAll(): MutableList<WClassesAPI.HeroObject> {
        return cache.asMap().entries.stream().map { it.value }.toList()
    }

    private object PlayerProfiles : Table("wcs_player_profiles") {
        val uid = varchar("uid", 36)
        val `class` = enumeration("class", WClassesAPI.HeroObject.HeroClass::class)
        override val primaryKey = PrimaryKey(uid, name = "PK_wcs_player_profiles_uid")
    }
}