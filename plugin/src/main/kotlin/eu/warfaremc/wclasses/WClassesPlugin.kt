package eu.warfaremc.wclasses

import eu.warfaremc.wclasses.api.WClassesAPI
import eu.warfaremc.wclasses.handler.GlobalCommandHandler
import eu.warfaremc.wclasses.handler.GlobalEventHandler
import eu.warfaremc.wclasses.implementation.WClassesAPIStdImpl
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion

import org.bukkit.plugin.java.JavaPlugin

import org.jetbrains.exposed.sql.Database

lateinit var instance: WClassesPlugin
lateinit var api: WClassesAPI
lateinit var database: Database

class WClassesPlugin : JavaPlugin() {

    override fun onDisable() {
        GlobalEventHandler.destroy()

        if(api.putAll())
            logger.info { "All data saved successfully" }
        else
            logger.warning { "Not all data saved successfully" }
    }

    override fun onLoad() {
        config.options().copyDefaults(true)
        saveDefaultConfig()

        instance = this
        database =
            Database.connect(
                url = "jdbc:mysql://${config.get("database.server")}/${config.get("database.database")}",
                driver = "${config.get("database.driver")}",
                user = "${config.get("database.user")}",
                password = "${config.get("database.password")}"
            )

        if (!::api.isInitialized)
            api = WClassesAPIStdImpl(database)
    }

    override fun onEnable() {
        GlobalCommandHandler.make()
        GlobalEventHandler.make()

        if(server.pluginManager.getPlugin("PlaceholderAPI") != null)
            WClassesPlaceholder.register()
    }
}