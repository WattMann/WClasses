package eu.warfaremc.wclasses.handler

import eu.warfaremc.wclasses.WClassesAPI
import eu.warfaremc.wclasses.api
import eu.warfaremc.wclasses.extensions.format
import eu.warfaremc.wclasses.instance
import eu.warfaremc.wclasses.logging.report

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
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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
            if(damager is Player)
                (damager as Player)
            else
                return

        api.get(source.uniqueId).ifPresent {
            val original = finalDamage

            api.get(entity.uniqueId).ifPresent { receiver ->
                if(receiver.heroClass == WClassesAPI.HeroObject.HeroClass.PALADIN) {
                    report(source, "§7Calculated warrior dmg protection: §a${damage * 0.02}")
                    damage -= damage * 0.03
                }
            }

            when (it.heroClass) {
                WClassesAPI.HeroObject.HeroClass.WARRIOR -> {
                    report(source , "§7Calculated warrior dmg change: §a${(damage * 0.03).format(2)}")
                    damage += (damage * 0.03)
                }
                WClassesAPI.HeroObject.HeroClass.ARCHER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.BOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated archer dmg change: §a${(damage * 0.1).format(2)}")
                        damage += (damage * 0.05)
                    }
                }
                WClassesAPI.HeroObject.HeroClass.SNIPER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.CROSSBOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated crossbow dmg change: §a${(damage * 0.07).format(2)}")
                        damage += (damage * 0.03)
                    }
                }
                WClassesAPI.HeroObject.HeroClass.PALADIN -> {
                    if(Math.random() <= 0.07) {
                        report(source , "§7Calculated holy-smite dmg change from §a${(entity as LivingEntity).health.format(2)}§7HP -> §a${((entity as LivingEntity).health * 0.05).format(2)}")
                        damage += (entity as LivingEntity).health * 0.05 //TODO cfg
                        source.sendActionBar(Component.text("§eHoly-smite §7aktivován!"))
                        (source ).playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    }
                }
                WClassesAPI.HeroObject.HeroClass.NECROMANCER -> {
                    if(Math.random() <= 0.15 && entity is Player) {
                        val originalHp = (source as LivingEntity).health
                        (source as LivingEntity).health = if((source as LivingEntity).health + 2 >= 20) 20.0 else (source as LivingEntity).health + 2
                        source.sendActionBar(Component.text("§eHealth steal §7aktivován!"))
                        source.playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                        report(source, "§7Health steal §e${originalHp.format(2)} §7-> §e ${(source as LivingEntity).health.format(2)}")
                    }
                }
                null -> {
                    report(source, "NULL hero class")
                }
            }

            report(source, "§7Damage §a${original.format(2)} §7-> §a${finalDamage.format(2)}")
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerJoinEvent.handle() {
        api.get(player.uniqueId).ifPresent {
            if (it.heroClass == WClassesAPI.HeroObject.HeroClass.ARCHER || it.heroClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, false, false, false))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerQuitEvent.handle() {
        api.get(player.uniqueId).ifPresent {
            if (it.heroClass == WClassesAPI.HeroObject.HeroClass.ARCHER || it.heroClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                player.removePotionEffect(PotionEffectType.SPEED)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerItemConsumeEvent.handle() {
        if(item.type == Material.MILK_BUCKET)
            api.get(player.uniqueId).ifPresent {
                if (it.heroClass == WClassesAPI.HeroObject.HeroClass.ARCHER || it.heroClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                    Bukkit.getScheduler().runTaskLater(instance, Runnable { player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, false, false, false)) }, 25)
            }
    }
}