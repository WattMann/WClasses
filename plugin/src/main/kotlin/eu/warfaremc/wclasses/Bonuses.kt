package eu.warfaremc.wclasses

val passive_speed: Double
        get() = instance.config.getDouble("bonuses.passive_speed")
val warrior_damage_protection: Double
        get() = instance.config.getDouble("bonuses.warrior.damage.protection")
val warrior_melee_damage: Double
        get() = instance.config.getDouble("bonuses.warrior.damage")
val paladin_holysmite_damage: Double
        get() = instance.config.getDouble("bonuses.paladin.holysmite.damage")
val paladin_holysmite_chance: Double
        get() = instance.config.getDouble("bonuses.paladin.holysmite.chance")
val necromancer_healthsteal_chance: Double
        get() = instance.config.getDouble("bonuses.necromancer.healthsteal.chance")
val necromancer_healthsteal_health: Double
        get() = instance.config.getDouble("bonuses.necromancer.healthsteal.health")
val archer_projectile_damage_bonus: Double
        get() = instance.config.getDouble("bonuses.archer.projectile.damage")
val sniper_projectile_damage_bonus: Double
        get() = instance.config.getDouble("bonuses.sniper.projectile.damage")


