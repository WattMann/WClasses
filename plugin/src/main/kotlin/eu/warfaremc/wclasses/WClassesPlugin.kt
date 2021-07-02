package eu.warfaremc.wclasses

import eu.warfaremc.wclasses.handler.GlobalEventHandler
import eu.warfaremc.wclasses.implementation.WClassesAPIStdImpl
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database

lateinit var instance: WClassesPlugin
lateinit var api: WClassesAPI
lateinit var database: Database

class WClassesPlugin : JavaPlugin() {

    override fun onDisable() {
        GlobalEventHandler.destroy()
    }

    override fun onLoad() {
        instance = this

        logger.info { "Connecting to database" }
        database =
            Database.connect(
                url = "jdbc:mysql://${config.get("database.server")}",
                driver = "${config.get("database.driver")}",
                user = "${config.get("database.user")}",
                password = "${config.get("database.password")}"
            )

        if (!::api.isInitialized)
            api = WClassesAPIStdImpl()
    }

    override fun onEnable() {
        GlobalEventHandler.make()
    }
}