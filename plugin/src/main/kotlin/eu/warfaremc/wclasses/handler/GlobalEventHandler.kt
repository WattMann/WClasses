package eu.warfaremc.wclasses.handler

import eu.warfaremc.wclasses.*
import eu.warfaremc.wclasses.api.WClassesAPI
import eu.warfaremc.wclasses.extensions.format
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
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

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
                    report(source, "§7Calculated warrior dmg protection: §a${damage * warrior_damage_protection}")
                    damage -= damage * warrior_damage_protection
                }
            }

            when (it.heroClass) {
                WClassesAPI.HeroObject.HeroClass.WARRIOR -> {
                    if(damager is Projectile)
                        return@ifPresent
                    report(source , "§7Calculated warrior dmg change: §a${(damage * warrior_melee_damage).format(2)}")
                    damage += (damage * warrior_melee_damage)
                }
                WClassesAPI.HeroObject.HeroClass.PALADIN -> {
                    if(damager is Projectile)
                        return@ifPresent
                    if(Math.random() <= paladin_holysmite_chance) {
                        report(source , "§7Calculated holy-smite dmg change from §a${(entity as LivingEntity).health.format(2)}§7HP -> §a${((entity as LivingEntity).health * paladin_holysmite_damage).format(2)}")
                        damage += (entity as LivingEntity).health * paladin_holysmite_damage //TODO cfg
                        source.sendActionBar(Component.text("§eHoly-smite §7aktivován!"))
                        source.playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    }
                }
                WClassesAPI.HeroObject.HeroClass.NECROMANCER -> {
                    if(damager is Projectile)
                        return@ifPresent
                    if(Math.random() <= necromancer_healthsteal_chance && entity is Player) {
                        val originalHp = (source as LivingEntity).health
                        (source as LivingEntity).health = if((source as LivingEntity).health + 2 >= 20) 20.0 else (source as LivingEntity).health + necromancer_healthsteal_health
                        source.sendActionBar(Component.text("§eHealth steal §7aktivován!"))
                        source.playSound(source.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                        report(source, "§7Health steal §e${originalHp.format(2)} §7-> §e ${(source as LivingEntity).health.format(2)}")
                    }
                }
                WClassesAPI.HeroObject.HeroClass.ARCHER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.BOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated archer dmg change: §a${(damage * archer_projectile_damage_bonus).format(2)}")
                        damage += (damage * archer_projectile_damage_bonus)
                    }
                }
                WClassesAPI.HeroObject.HeroClass.SNIPER -> {
                    if(cause == EntityDamageEvent.DamageCause.PROJECTILE && source.inventory.itemInMainHand.type == Material.CROSSBOW) {  //TODO: Off-hand check
                        report(source , "§7Calculated crossbow dmg change: §a${(damage * sniper_projectile_damage_bonus).format(2)}")
                        damage += (damage * sniper_projectile_damage_bonus)
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
                player.walkSpeed += passive_speed.toFloat()
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun PlayerQuitEvent.handle() {
        api.get(player.uniqueId).ifPresent {
            if (it.heroClass == WClassesAPI.HeroObject.HeroClass.ARCHER || it.heroClass == WClassesAPI.HeroObject.HeroClass.SNIPER)
                player.walkSpeed -= passive_speed.toFloat()
        }
    }
}