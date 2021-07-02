package eu.warfaremc.wclasses.handler

import eu.warfaremc.wclasses.WClassesAPI
import eu.warfaremc.wclasses.api
import eu.warfaremc.wclasses.extensions.isSword
import eu.warfaremc.wclasses.instance
import eu.warfaremc.wclasses.logging.report
import eu.warfaremc.wclasses.misc.format
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class GlobalEventHandler : Listener {
    companion object {
        private val handlerInstance = GlobalEventHandler()
        fun make() {
            Bukkit.getPluginManager().registerEvents(handlerInstance, instance)
        }

        fun destroy() {
            EntityDamageEvent.getHandlerList().unregister(handlerInstance)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun EntityDamageByEntityEvent.handle() {
        val source = if(damager is Projectile) {
            if((damager as Projectile).shooter is Player)
                ((damager as Projectile).shooter as Player)
            else
                return
        } else
              (damager as Player)

        api.get(source.uniqueId).ifPresent {
            val original = finalDamage

            when (it.heroClass) {
                WClassesAPI.HeroObject.HeroClass.WARRIOR -> {
                    if(source.inventory.itemInMainHand.type.isSword()) {
                        report(source , "§7Calculated warrior dmg change: §a${(damage * 0.03).format(2)}")
                        damage += (damage * 0.03) //TODO cfg
                    }
                }
                WClassesAPI.HeroObject.HeroClass.ARCHER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.BOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated archer dmg change: §a${(damage * 0.1).format(2)}")
                        damage += (damage * 0.1) // TODO cfg
                    }
                }
                WClassesAPI.HeroObject.HeroClass.SNIPER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.CROSSBOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated crossbow dmg change: §a${(damage * 0.07).format(2)}")
                        damage += (damage * 0.07) //TODO cfg
                    }
                }
                WClassesAPI.HeroObject.HeroClass.PALADIN -> {
                    if(Math.random() <= 0.15) { //TODO cfg
                        report(source , "§7Calculated holy-smite dmg change from §a${(entity as LivingEntity).health.format(2)}§7HP -> §a${((entity as LivingEntity).health * 0.05).format(2)}")
                        damage += (entity as LivingEntity).health * 0.05 //TODO cfg
                        source.sendActionBar(Component.text("§eHoly-smite §7aktivován!"))
                        (source ).playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    }
                }
                WClassesAPI.HeroObject.HeroClass.NECROMANCER -> {
                    if(Math.random() <= 0.15 && entity is Player) { //TODO cfg
                        val original = (source as LivingEntity).health
                        (source as LivingEntity).health = if((source as LivingEntity).health + 2 >= 20) 20.0 else (source as LivingEntity).health + 2
                        source.sendActionBar(Component.text("§eHealth steal §7aktivován!"))
                        source.playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        report(source, "§7Health steal §e${original.format(2)} §7-> §e ${(source as LivingEntity).health.format(2)}")
                    }
                }
                null -> {
                    report(source, "NULL hero class")
                }
            }

            report(source, "§7Damage §a${original.format(2)} §7-> §a${finalDamage.format(2)}")
        }
    }
}