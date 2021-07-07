package eu.warfaremc.wclasses

import eu.warfaremc.wclasses.api.WClassesAPI
import eu.warfaremc.wclasses.handler.GlobalCommandHandler
import eu.warfaremc.wclasses.handler.GlobalEventHandler
import eu.warfaremc.wclasses.implementation.WClassesAPIStdImpl

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

import org.jetbrains.exposed.sql.Database

import java.util.concurrent.CompletableFuture

lateinit var instance: WClassesPlugin
lateinit var api: WClassesAPI
lateinit var database: Database

class WClassesPlugin : JavaPlugin() {

    companion object {
        @JvmStatic val initComplete = CompletableFuture<Void>()
    }

    override fun onDisable() {
        GlobalEventHandler.destroy()

        if(api.putAll())
            logger.info { "All data saved successfully" }
        else
            logger.warning { "Not all data saved successfully" }
    }

    override fun onLoad() {
        instance = this

        config.options().copyDefaults(true)
        saveDefaultConfig()
    }

    override fun onEnable() {
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            logger.info { "Asynchronous initialization started" }
            val beginTimestamp = System.currentTimeMillis()
            database =
                Database.connect(
                    url = "jdbc:mysql://${config.get("database.server")}/${config.get("database.database")}",
                    driver = "${config.get("database.driver")}",
                    user = "${config.get("database.user")}",
                    password = "${config.get("database.password")}"
                )
            logger.info { "Connected to database in ${System.currentTimeMillis() - beginTimestamp}ms"}

            if (!::api.isInitialized)
                api = WClassesAPIStdImpl(database)

            finalize()
        })
    }

    private fun finalize() {
        GlobalCommandHandler.init()
        GlobalEventHandler.make()

        initComplete.complete(null)
        logger.info { "Asynchronous initialization finished" }
    }
}