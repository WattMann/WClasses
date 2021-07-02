package eu.warfaremc.wclasses.implementation

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import eu.warfaremc.wclasses.WClassesAPI
import org.jetbrains.annotations.NotNull
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

var cache: Cache<UUID, WClassesAPI.HeroObject> = CacheBuilder.newBuilder().maximumSize(150).build()

class WClassesAPIStdImpl : WClassesAPI {
    override fun get(uid: UUID?): @NotNull Optional<WClassesAPI.HeroObject> {
        if (uid == null)
            return Optional.empty()

        val cached = cache.getIfPresent(uid)
        if (cached != null)
            return Optional.of(cached)

        val profile = transaction {
            PlayerProfiles.select { PlayerProfiles.uid eq uid.toString() }.firstOrNull()
        } ?: return Optional.empty()

        return Optional.of(
            WClassesAPI.HeroObject(uid, profile[PlayerProfiles.`class`])
        ).also { cache.put(uid, it.get()) }
    }

    override fun put(uid: UUID?): Boolean {
        val record = cache.getIfPresent(uid ?: return false) ?: return false
        transaction {
            PlayerProfiles.insert {
                it[PlayerProfiles.uid] = record.uid.toString()
                it[PlayerProfiles.`class`] = record.heroClass
            }
        }
        return true
    }

    private object PlayerProfiles : Table("wcs_player_profiles") {
        val uid = varchar("uid", 36)
        val `class` = enumeration("class", WClassesAPI.HeroObject.HeroClass::class)
        override val primaryKey = PrimaryKey(uid, name = "PK_wcs_player_profiles_uid")
    }
}