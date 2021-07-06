package eu.warfaremc.wclasses.logging

import org.bukkit.entity.Player

import java.util.*

private val debuggers = ArrayList<UUID>()

fun report(player: Player, msg: String) {
    if(shouldReport(player))
        player.sendMessage(msg)
}

fun shouldReport(player: Player): Boolean {
    return debuggers.contains(player.uniqueId)
}

fun enableReporting(player: Player)
    = debuggers.add(player.uniqueId)

fun disableReporting(player: Player)
        = debuggers.add(player.uniqueId)