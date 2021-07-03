package eu.warfaremc.wclasses.extensions

import org.bukkit.Material

fun Material.isSword(): Boolean {
    return this.name.endsWith("_SWORD")
}