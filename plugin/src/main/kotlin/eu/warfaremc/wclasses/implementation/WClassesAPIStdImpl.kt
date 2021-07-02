package eu.warfaremc.wclasses.implementation

import eu.warfaremc.wclasses.WClassesAPI
import org.jetbrains.annotations.NotNull
import java.util.*

class WClassesAPIStdImpl : WClassesAPI {
    override fun get(uid: UUID?): @NotNull Optional<WClassesAPI.HeroObject> {
        return Optional.of(WClassesAPI.HeroObject(uid ?: return Optional.empty(), WClassesAPI.HeroObject.HeroClass.SNIPER))
    }

    override fun put(uid: UUID?): Boolean {
        TODO("Not yet implemented")
    }
}