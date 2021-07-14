package eu.warfaremc.wclasses.logging

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import java.util.*

private val debuggers = ArrayList<String>()

fun report(player: CommandSender, msg: String) {
    if(shouldReport(player))
        player.sendMessage(msg)
}

fun shouldReport(player: CommandSender): Boolean {
    return debuggers.contains(player.name)
}

fun enableReporting(player: Player)
    = debuggers.add(player.name)

fun disableReporting(player: Player)
    = debuggers.add(player.name)