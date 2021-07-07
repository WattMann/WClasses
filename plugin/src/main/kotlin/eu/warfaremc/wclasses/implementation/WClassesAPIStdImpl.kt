package eu.warfaremc.wclasses.implementation

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

import eu.warfaremc.wclasses.api.WClassesAPI
import eu.warfaremc.wclasses.database
import eu.warfaremc.wclasses.instance

import org.jetbrains.annotations.NotNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import java.lang.Exception
import java.lang.IndexOutOfBoundsException
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
            WClassesAPI.HeroObject(uid, profile[PlayerProfiles.heroClass])
        ).also { cache.put(uid, it.get()) }
    }

    override fun put(uid: UUID?): Boolean {
        val record = cache.getIfPresent(uid ?: return false) ?: return false
        put(WClassesAPI.HeroObject(uid, record.heroClass))
        return true
    }

    override fun put(obj: WClassesAPI.HeroObject?): Boolean {
        if(obj == null)
            return false

        cache.put(obj.uid, obj);

        transaction(database) {
            try {
                val ref = PlayerProfiles.select { PlayerProfiles.uid eq obj.uid.toString() }.firstOrNull()
                when(ref == null) {
                    true -> {
                        PlayerProfiles.insert {
                            it[uid] = obj.uid.toString()
                            it[heroClass] = obj.heroClass
                        }
                    }
                    false -> {
                        PlayerProfiles.update ({ PlayerProfiles.uid eq obj.uid.toString()}) {
                            it[heroClass] = obj.heroClass
                        }
                    }
                }
            } catch (ex: Exception) {
                instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
                return@transaction false
            }
        }
        return true
    }

    override fun putAll(): Boolean {
        val copy = cache.asMap();
        cache.invalidateAll()

        for (mutableEntry in copy) {
            transaction(database) {
                try {
                    PlayerProfiles.update {
                        it[uid] = mutableEntry.key.toString()
                        it[heroClass] = mutableEntry.value.heroClass
                    }
                } catch (ex: Exception) {
                    instance.logger.severe("Failed to execute transaction: ${ex.javaClass.canonicalName}: ${ex.message}")
                    return@transaction false
                }
            }
        }

        return true
    }

    override fun getAll(): List<WClassesAPI.HeroObject> {
        return transaction (database) {
            return@transaction PlayerProfiles.selectAll().asSequence()
                .filter {
                    try {
                        UUID.fromString(it[PlayerProfiles.uid])
                        true
                    } catch (ex: IllegalArgumentException) {
                        false
                    }
                }.map {
                    try {
                        WClassesAPI.HeroObject(UUID.fromString(it[PlayerProfiles.uid]), it[PlayerProfiles.heroClass])
                    } catch (ex: IndexOutOfBoundsException){
                        WClassesAPI.HeroObject(UUID.fromString(it[PlayerProfiles.uid]), null)
                    }
                }.toList()
        }
    }

    private object PlayerProfiles : Table("wcs_player_profiles") {
        val uid = varchar("uid", 36)
        val heroClass = enumeration("class", WClassesAPI.HeroObject.HeroClass::class).nullable()

        override val primaryKey = PrimaryKey(uid, name = "PK_wcs_player_profiles_uid")
    }
}