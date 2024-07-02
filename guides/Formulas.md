# Formulas

Listed below are many of the formulas used on Ultima to determine various costs/rewards, from kingdom claims to Cindersmith results. They have been somewhat converted from the configuration files to a more human readable format.
- Conditions format: `[condition] ? [result if true] : [result if false]`
- RNG format: `rng(chance)` - returns true if a randomly chosen number between 0 and 1 is less than `chance`

### Kingdoms
- Passive income: `kingdoms_pacifist ? 0 : kingdoms_online_members * floor(sqrt(kingdoms_lands))`
    - `kingdoms_pacifist`: `true` if the kingdom is pacifist.
    - `kingdoms_online_members`: The members in your kingdom that are currently online.
    - `kingdoms_lands`: The current amount of land claims you have.
- Might: `kingdoms_resource_points + floor(sqrt(kingdoms_lands)) * (champion_upgrade_equipment * 5 + champion_upgrade_health + powerup_upgrade_damage_boost + powerup_upgrade_damage_reduction + powerup_upgrade_regeneration_boost)`
    - `kingdoms_resource_points`: The amount of resource points your kingdom currently has.
    - `champion_upgrade_equipment`: The upgrade level of your champion's equipment.
    - `champion_upgrade_health`: The upgrade level of your champion's health.
    - `powerup_upgrade_damage_boost`: Your damage boost upgrade level.
    - `powerup_upgrade_damage_reduction`: Your damage reduction upgrade level.
    - `powerup_upgrade_regeneration_boost`: Your regeneration boost upgrade level.
    - Might is the calculation used to determine the rank of top kingdoms in `/k top`. Pacifist kingdoms are excluded from this list.
- Maximum claims: `kingdoms_members * (kingdoms_pacifist ? max_claims + 1 : max_claims * 2 + 5)`
    - `kingdoms_members`: The total amount of members in your kingdom.
    - `max_claims`: The level of your "Max Claims" misc upgrade.
    - This formula essentially means that when maxed, kingdoms get 25 claims per player if they are aggressors, and 11 claims per player if they are pacifist.
- Land claim cost: `kingdoms_lands - 3`
- War declaration cost: `kingdoms_lands`

### Skills
- XP required per level: `50 * (level - 1)^2 + 50`
    - `level`: The current level of the skill.
- Kept items on death: `rng(abiding_level * 0.05)`
    - This amount is calculated for each item in your inventory, including for each item in a stack.
    
### Cindersmith
- Minimum dust for enchantment: `max(-max_level + 5, 2)`
    - `max_level`: The maximum level of this enchantment.
    - Example: Fire Aspect, an enchantment with max level 2, requires 3 dust for Fire Aspect I.
- Chance for a higher level: `(max_level - 1) / (6 - min_dust)`
    - `min_dust`: The minimum amount of dust to get this enchantment, specified above.
    - This formula gives the chance that each additional piece of dust above `min_dust` dust will increase the level of the resulting enchantment by 1.
    - Example: From above, we know that Fire Aspect requires 3 dust. Each dust above 3 has a 33% chance of increasing the resulting enchantment to level 2.
- Level cost: `tier * 10 + ceil(10 * level / max_level)`
    - `tier`: The 0-indexed tier of the enchantment. Common (Vanilla) = 0, Uncommon = 1, Rare = 2, Epic = 3, Legendary = 4.
    - `level`: The actual level of the resulting enchantment.
    - Example: Fire Aspect I will cost 5 levels.
