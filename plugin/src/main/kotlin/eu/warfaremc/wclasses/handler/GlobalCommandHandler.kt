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
import eu.warfaremc.wclasses.api.WClassesAPI
import eu.warfaremc.wclasses.api
import eu.warfaremc.wclasses.extensions.format
import eu.warfaremc.wclasses.instance
import eu.warfaremc.wclasses.logging.disableReporting
import eu.warfaremc.wclasses.logging.enableReporting
import eu.warfaremc.wclasses.logging.report
import eu.warfaremc.wclasses.passive_speed
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.function.Function

class GlobalCommandHandler {
    companion object {

        lateinit var manager: PaperCommandManager<CommandSender>
        lateinit var commandHelp: MinecraftHelp<CommandSender>
        lateinit var audiences: BukkitAudiences
        lateinit var commandAnnotation: AnnotationParser<CommandSender>

        @Throws(java.lang.Exception::class)
        fun make() {
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

    @CommandMethod("wcs|wclasses reload")
    @CommandDescription("Reloads the configuration")
    @CommandPermission("wcs.admin")
    fun reloadCommand(
        sender: Player
    ) {
        instance.reloadConfig()
        sender.sendMessage("§aDone")
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
    @CommandDescription("Shows player information")
    @CommandPermission("wcs.admin")
    fun infoCommand(
        sender: Player,
        @Nullable @Argument("target" ) target: Player?
    ) {
        val player: Player = target ?: sender

        api.get(player.uniqueId).ifPresentOrElse( {
            sender.sendMessage("""
            §7############## Player info ################    
            §fName: §a${player.name}
            §fUUID: §a${player.uniqueId}
            §fHeroClass: §a${it.heroClass ?: "null" }
            §7###########################################  
        """.trimIndent())
        }, {
            sender.sendMessage("§fNo records found")
        })
    }

    @CommandMethod("wcs|wclasses infoUID <uuid>")
    @CommandDescription("Shows UUID information")
    @CommandPermission("wcs.admin")
    fun infoUIDCommand(
        sender: Player,
        @Nullable @Argument("uuid") @Regex("\b[0-9a-f]{8}\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\b[0-9a-f]{12}\b") uuid: String?
    ) {
        //FIXME
        if(uuid == null) {
            sender.sendMessage("Invalid UUID")
            return
        }
        api.get(UUID.fromString(uuid)).ifPresentOrElse( {
            sender.sendMessage("""
            §7############## UUID info ##################
            §fName: §a${Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name ?: "N/A"}}
            §fUUID: §a${uuid}
            §fHeroClass: §a${it.heroClass ?: "null" }
            §7###########################################  
        """.trimIndent())
        }, {
            sender.sendMessage("§fNo records found")
        })
    }

    @CommandMethod("wcs|wclasses set <target> <class>")
    @CommandDescription("Sets player class")
    @CommandPermission("wcs.admin")
    fun setClassCommand(
        sender: Player,
        @NotNull @Argument("target") target: Player,
        @NotNull @Argument("class") targetClass: WClassesAPI.HeroObject.HeroClass
    ) {
         api.get(target.uniqueId).ifPresent {
            try {
                // if changing from archer or paladin to other class removing passive speed
                if((it.heroClass == WClassesAPI.HeroObject.HeroClass.ARCHER
                            || it.heroClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                    && (targetClass != WClassesAPI.HeroObject.HeroClass.ARCHER
                            && targetClass != WClassesAPI.HeroObject.HeroClass.SNIPER)) {
                    target.walkSpeed = 0.2f // Default value
                    report(sender, "Reset walk speed of ${target.name} to ${target.walkSpeed.format(2)}")
                }
                // if changing to archer or paladin from other class adding passive speed
                if((targetClass == WClassesAPI.HeroObject.HeroClass.ARCHER
                            || targetClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                    && (it.heroClass != WClassesAPI.HeroObject.HeroClass.ARCHER
                            && it.heroClass != WClassesAPI.HeroObject.HeroClass.SNIPER)) {
                    target.walkSpeed += passive_speed.toFloat()
                    report(sender, "Set walk of ${target.name} speed to ${target.walkSpeed.format(2)}")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (api.put(WClassesAPI.HeroObject(target.uniqueId, targetClass)))
            sender.sendMessage("§aDone")
        else
            sender.sendMessage("§4Failed")
    }


    @CommandMethod("wcs|wclasses setUID <uuid> <class>")
    @CommandDescription("Shows UUID information")
    @CommandPermission("wcs.admin")
    fun setClassUIDCommand(
        sender: Player,
        @Nullable @Argument("uuid") @Regex("\b[0-9a-f]{8}\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\b[0-9a-f]{12}\b") target: String?,
        @NotNull @Argument("class") targetClass: WClassesAPI.HeroObject.HeroClass
    ) {
        //FIXME
        if(target == null) {
            sender.sendMessage("Invalid UUID")
            return
        }
        Bukkit.getPlayer(target)?.let {
            setClassCommand(sender, it, targetClass)
        } ?: run {
            if (api.put(WClassesAPI.HeroObject(UUID.fromString(target), targetClass)))
                sender.sendMessage("§aDone")
            else
                sender.sendMessage("§4Failed");
        }
    }
}