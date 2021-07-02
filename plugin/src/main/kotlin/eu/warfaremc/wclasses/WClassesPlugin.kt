package eu.warfaremc.wclasses

import eu.warfaremc.wclasses.handler.GlobalEventHandler
import eu.warfaremc.wclasses.implementation.WClassesAPIStdImpl
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

lateinit var instance: WClassesPlugin
lateinit var api: WClassesAPI

class WClassesPlugin : JavaPlugin() {

    override fun onDisable() {
        GlobalEventHandler.destroy()
    }

    override fun onLoad() {
        instance = this
        if(!::api.isInitialized)
            api = WClassesAPIStdImpl()
    }

    override fun onEnable() {
        GlobalEventHandler.make()
    }
}