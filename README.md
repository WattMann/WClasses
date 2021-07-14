# WClasses
Warfare Classes, plugin made for warfaremc.eu. Allows the usage of playable classes with configurable bonuses.
<br><br>
Built for api-version `1.16`, paper `1.16.5`
## Commands
`[optional argument] <required argument> |permission|`
- `wcs help (query)`  wcs.admin
- `wcs reload` wcs.admin
- `wcs debug [boolean]` wcs.admin
- `wcs info <player>` wcs.admin
- `wcs infoUID <UUID>` wcs admin
- `wcs set <player>` wcs.admin
- `wcs setUID <UUID>` wcs.admin
## Classes & Default bonuses
- Warrior `-3% taken damage, +2% attack damage`
- Archer `+5% projectile damage, +25% movement speed`
- Paladin `Holy Smite ability, 7% chance grants damage +7% of target's current HP`
- Sniper `+3% projectile damage, +25% movement speed`
- Necromancer `Health Steal ability, 15% chance, steals 2HP`

## Default configuration
```yaml
# __        ______ _  
# \ \      / / ___| | __ _ ___ ___  ___  ___  
# \ \ /\ / / |   | |/ _` / __/ __|/ _ \/ __|  
# \ V  V /| |___| | (_| \__ \__ \  __/\__ \  
# \_/\_/  \____|_|\__,_|___/___/\___||___/  
  
# Database credentials  
database:  
  driver: "com.mysql.cj.jdbc.Driver"  
  server: "127.0.0.1"
  database: "minecraft"
  user: "root"  
  password: "root"  
  port: 3036  
  
# Bonus configuration  
bonuses:  
  passive_speed: 0.05  
  warrior:  
    damage: 0.02  
    protection: 0.03  
  paladin:  
    holysmite:  
      damage: 0.05  
      chance: 0.07  
  necromancer:  
    healthsteal:  
      chance: 0.15  
      health: 2  
  archer:  
    projectile:  
      damage: 0.05  
  sniper:  
    projectile:  
      damage: 0.03
```
