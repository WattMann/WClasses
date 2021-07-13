package eu.warfaremc.wclasses

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

object WClassesPlaceholder : PlaceholderExpansion() {

    override fun getIdentifier(): String
        = "wclasses"

    override fun getAuthor(): String
        = instance.description.authors.joinToString(", ")

    override fun getVersion(): String
        = instance.description.version

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if(player == null)
            return "N/A"

        return api.get(player.uniqueId).get().heroClass?.name
            ?.let { it.lowercase().replaceFirstChar { first -> first.uppercase() } } ?: "N/A"
    }
}