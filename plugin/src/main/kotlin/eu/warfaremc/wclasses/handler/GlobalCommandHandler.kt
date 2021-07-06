package eu.warfaremc.wclasses.handler

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Greedy
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import eu.warfaremc.wclasses.WClassesAPI
import eu.warfaremc.wclasses.api
import eu.warfaremc.wclasses.instance
import eu.warfaremc.wclasses.logging.disableReporting
import eu.warfaremc.wclasses.logging.enableReporting
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.function.Function

class GlobalCommandHandler {
    companion object {

        lateinit var manager: PaperCommandManager<CommandSender>
        lateinit var commandHelp: MinecraftHelp<CommandSender>
        lateinit var audiences: BukkitAudiences
        lateinit var commandAnnotation: AnnotationParser<CommandSender>

        @Throws(java.lang.Exception::class)
        fun init() {
            val executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.newBuilder<CommandSender>()
                    .withSynchronousParsing()
                    .build()
            try {
                manager = PaperCommandManager(
                    instance,
                    executionCoordinatorFunction,
                    Function.identity(),
                    Function.identity()
                )
            } catch (exception: Exception) {
                throw exception
            } finally {
                audiences = BukkitAudiences.create(instance)
                commandHelp = MinecraftHelp("/wcs help", audiences::sender, manager)
                if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER))
                    manager.registerBrigadier()
                if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
                    manager.registerAsynchronousCompletions()
                val commandMetaFunction: java.util.function.Function<ParserParameters, CommandMeta> =
                    java.util.function.Function<ParserParameters, CommandMeta> { parser ->
                        CommandMeta.simple()
                            .with(CommandMeta.DESCRIPTION, parser.get(StandardParameters.DESCRIPTION, "No description"))
                            .build()
                    }
                commandAnnotation = AnnotationParser(
                    manager,
                    CommandSender::class.java,
                    commandMetaFunction
                )
                commandAnnotation.parse(GlobalCommandHandler())
            }
        }
    }

    @CommandMethod("wcs|wclasses help")
    @CommandDescription("Shows help dialog")
    @CommandPermission("wcs.admin")
    fun commandHelp(
        sender: Player
    ) {
        commandHelp.queryCommands("", sender)
    }

    @CommandMethod("wcs|wclasses help [query]")
    @CommandDescription("Shows help dialog for specified query")
    @CommandPermission("wcs.admin")
    fun commandHelp(
        sender: Player,
        @Nullable @Argument("query") @Greedy query: String?
    ) {
        commandHelp.queryCommands(query ?: "", sender)
    }


    @CommandMethod("wcs|wclasses debug [boolean]")
    @CommandDescription("Enables/disables debug mode")
    @CommandPermission("wcs.admin")
    fun debugCommand(
        sender: Player,
        @Nullable @Argument("boolean") bool: Boolean?
    ) {
        if (bool == null || bool) {
            enableReporting(sender)
            sender.sendMessage("Debug reports enabled")
        } else {
            disableReporting(sender)
            sender.sendMessage("Debug reports disabled")
        }
    }

    @CommandMethod("wcs|wclasses info [target]")
    @CommandDescription("Enables/disables debug mode")
    @CommandPermission("wcs.admin")
    fun infoCommand(
        sender: Player,
        @Nullable @Argument("target" ) target: Player?
    ) {
        val player: Player = target ?: sender

        api.get(player.uniqueId).ifPresent {
            sender.sendMessage("""
            §7############## Player info ################    
            §fName: §a${player.name}
            §fUUID: §a${player.uniqueId}
            §fHeroClass: §a${it.heroClass ?: "null" }
            §7###########################################  
        """.trimIndent())
        }
    }

    @CommandMethod("wcs|wclasses setClass <target> <class>")
    @CommandDescription("Sets player class")
    @CommandPermission("wcs.admin")
    fun setClassCommand(
        sender: Player,
        @NotNull @Argument("target") target: Player,
        @NotNull @Argument("class") `class`: String
    ) {
        if(api.put(WClassesAPI.HeroObject(target.uniqueId, WClassesAPI.HeroObject.HeroClass.valueOf(`class`))))
            sender.sendMessage("§fSuccess")
        else
            sender.sendMessage("§fFailed to update")
    }

}