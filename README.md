# Ultima Addons
This plugin adds a couple features to the Ultima Kingdoms server that are not present in other plugins. While this plugin is intended for use on the Ultima server, you may compile this plugin for yourself to use on your own server, but I will provide no guarantees or support that it will work for yourself. There are many configuration changes in plugins listed here and otherwise which aren't noted here that contribute to Ultima's experience. The best way to see these features are simply to log on to play.ultimamc.net and join the Kingdoms server.

## Kingdoms
UltimaAddons uses the KingdomsX API by Crypto Morin to provide several extra features.

### Challenges
- Kingdoms can now only challenge 1 Kingdom at a time. However, one Kingdom may get challenged by multiple other Kingdoms.
- There is now a confirmation process before sending a challenge to make sure that it feels like a commitment.
- After a war, there is now a cooldown such that the challenger can't declare war on any Kingdom for 1 day (but they can still be challenged).
- Players will now receive messages upon logging in for upcoming and current wars.
- Players will now receive messages 1 minute before war starts, when it starts, and when it ends.
- You cannot make claims, unclaim, or move your nexus while being challenged.
- Only neutral and enemy Kingdoms can be challenged. Challenging a neutral Kingdom will automatically make them an enemy.
- Challenges can be cancelled by changing relation with the challenged Kingdom to neutral, truce, or ally.

### Invasions
- The goal of an invasion is now solely to *capture the chunk*, similar to a King of the Hill game. Attackers must stand inside the invasion chunk for 3 minutes in order for the chunk to be captured.
- Killing the Kingdom champion no longer wins the invasion (the champion is an upgradable boss that spawns whenever someone tries to invade your lands).
- The champion will now prioritize targetting attackers (and their allies) standing inside the invasion chunk, acting similar to how a player would defend.
- If *any* defenders, which includes the champion and players from the defending Kingdom and their allies, are standing inside the chunk, the capture progress will not advance.
- If the attackers fail to capture the chunk within 15 minutes, or if they surrender with `/k surrender`, the invasion fails.
- The champion teleports back to the invasion starting location if it has been outside the chunk for more than 15 seconds.
- Skeleton champions shoot shield-penetrating Piercing IV arrows.
- The "Thor" champion ability disables shield items for 5 seconds.

### Outposts
- Outposts now allow you to claim land that is disconnected from other kingdom lands, including in other worlds (you can no longer create claims in worlds in where you don't already have claims without an Outpost).
- Outposts no longer sell any items, but you can still access your kingdom Nexus from any placed Outpost.
- To use, place an Outpost on an *unclaimed* chunk to claim it, requiring both STRUCTURES and CLAIMS kingdom permissions.
- If the Outpost is broken, all lands claimed from that Outpost will be unclaimed.
- Invading an enemy Outpost chunk will also unclaim all lands claimed from that Outpost.
- You cannot move your nexus to a chunk claimed from an Outpost.
- You still cannot unclaim lands in a way that would disconnect them from each other. This is considered for every "group" of claims including nexus chunks, each outpost's chunks, and invaded chunks.

### Shields
- If you buy a shield, you will not be able to buy another shield for 2x the duration of that shield. For example, if you buy a 3 day shield, the next time you will be able to buy a shield is after 6 days, leaving you vulnerable for 3 days after the shield expires.
- If you try to declare war on another Kingdom while you are shielded, your shield will be removed, and you are still subject to the original shield buying date as described above.

### Discord
In addition to tracking player joining and leaving kingdoms as well as invasions, the discord channel now also tracks:
- Kingdoms being created and disbanded,
- Shield purchases and their durations,
- War declarations and their preparation time,
- War starts and ends, 
- Kingdom disbands due to invasions.

### Other
- You cannot make additional claims beyond 1 claim if you do not have a nexus.
- You cannot create outposts or challenge if you do not have a nexus.
- Turrets can be destroyed by TNT explosions, even if the Kingdom has anti-explosion misc upgrade.
- Players are forced to select a pacifist/aggressor option on Kingdom creation, by making the GUI re-open if closed without selecting anything.

## Custom Enchantments
The plugin also hooks into AdvancedEnchantments' API to provide the following custom effects:
 
- CAPTURE: Drops a mob's spawn egg. Excludes boss mobs and those defined in the config.
- RECUPERATE: Reduces cooldown time for shields when they are disabled by an axe.
- UADD_DURABILITY_CURRENT_ITEM: Adds/removes durability from an item, with consideration for unbreaking enchantment and custom durability items.
- UADD_DURABILITY_ARMOR: Adds/removes durability from all worn armor, with consideration for unbreaking enchantment and custom durability items.
- FALL_DISTANCE_DAMAGE: Multiplies damage dealt by the fall distance of the attacker.

This, with some additional placeholders, allows the the following enchants to function properly:
- Parry: Block your shield right as your opponent is about to hit you to perform a parry, dealing true damage and knocking them backwards.
- Gardener: Critical hits do more damage the higher you fall from.
- Mobslinger: Hold any mob's spawn egg in your offhand to load and shoot it from your crossbow.
- Disintegrate: Hits do more durability damage to both your weapon and opponent armor.
- Recuperate: Reduces cooldown time for shields when they are disabled by an axe.

## Custom Items
The plugin also provides some custom items and recipes that add some fresh gameplay elements to the survival experience. All recipes can be found by using the `/recipes` command in-game. Most weapons with abilities have cooldowns that are not listed here, since they are subject to change. Several items have increased durability over their vanilla counterparts, such as the armorsets.

### Weapons
- Blaze Sword: Right-click to shoot 3 fireballs in quick succession.
- Tanto: A short sword that can be swung extremely fast.
- Orcus: Land a critical hit to remove 20% of their remaining health, regardless of armor or effects, in addition to blinding and withering them temporarily.
- Shadowblade: Right-click to teleport 10 blocks in the direction you are facing. If you teleport through a player, you will face that player after the teleport.
- Oxtail Saber: Dual-wield the weapon to attack enemies twice as fast, halving their damage tick cooldown. Visually, other players will see you alternating between attacking with main hand and off hand sword.
- Shiruken: A throwable weapon that damages enemies in rapid bursts.

### Armor
Each armorset has the protection and armor toughness equivalent of Netherite, unless stated otherwise.
- Mithril: Each piece, when worn, gives +0.5% movement speed per level of Agility (from Aurelium) above 25.
- Obsidian: Each piece gives +0.2 armor toughness per level of Endurance above 25, increasing its resistance against high-damage attacks.
- Infused: Each piece gives +0.5% attack damage per level of Fighting above 25.
- Shard: Each piece gives +0.1 max health per level of Mining above 25.

### Warp Totems
The plugin replaces regular teleportation commands with item-based teleports. After crafting a Totem of Warping, it can be combined in the crafting table with various items to create totems set to different locations. Totems are consumed on use, and require a 5 second timer without moving before teleportation succeeds.
- Totem of Warping (Kingdom Home): Crafted using any bed, right-click to initiate a teleportation to your kingdom home location.
- Totem of Warping (Bed): Crafted using a compass, right-click to initiate a teleportation to your bed or respawn anchor location. Does not work if your respawn point was destroyed.
- Totem of Warping (Death): Crafted using a calibrated sculk sensor, right-click to initiate a teleportation to your last death location. Only works up to 5 minutes after your last death. The item is kept in inventory on death.
- Totem of Warping (Lodestone): Crafted using a lodestone compass, right-click to initiate a teleportation to the lodestone set by the lodestone compass. Does not work if the lodestone has been destroyed.
- Totem of Warping (Player): Crafting this item requires an original generation (non-copy) written book signed by another player. Right-click to request a teleportation to the player set by the book, if they are online.
- Totem duplication: Combine any set totem with an unset totem in crafting to produce 2 of the set totem. The result is automatically unstacked on collection.

### Others
- Diamond Chip: Like iron or gold nuggets, but for diamonds. Used for better economy integration, since Ultima uses a item-based economy with diamonds as its currency.
- Obsidian Ingot: A rare ingredient dropped when the blast resistance of obsidian is overcome. Gives Resistance I when held. Used to craft obsidian armor.
- Radiant Shard: Gives Regeneration I when held. Used to craft Shard armor.
- Infused Ingot: A rare ingredient dropped when unwaxed copper is destroyed by a charged explosion. Drop chance increases the more oxidized the copper. Gives Strength I when held. Used to craft Infused armor.
- Mithril Ingot: Formed when shulkers are destroyed by star-like temperatires. Gives Speed I when held. Used to craft Mithril armor.
- Dragon Firework: The only firework that can boost elytras in flight. Boost is tripled in the End.
- Bundle: An item that never became obtainable in survival, now has a recipe.
- Enchanted Dust: Allows enchantments to be obtained through the Cindersmith

## Enchanting
Ultima has a fully custom method of obtaining its vanilla and custom enchantments, starting from a resource known as enchanted dust. This can then be used in the Cindersmith custom enchanting table to obtain different rarities and levels of enchantments.

### Enchanted Dust
Common (vanilla), uncommon, rare, epic, and legendary dust can be obtained via:
- Chest loot. The rarity and amount of dust increases the closer the player is to the world border.
- Mining. The rarity and amount of dust depends on the distance to the world border, fortune, AuraSkills mining luck, and type of ore mined.
- Grindstone disenchanting. Each removed enchant has a chance of giving dust of the enchant's rarity. The amount of dust depends on AuraSkills Sorcery level, the level of the enchant, and its rarity if it is a vanilla enchant.

### Cindersmith
- A custom enchantment table-like GUI that takes in a gear item (or book), and enchanted dust (instead of the vanilla lapis).
- Only items that do not already have an enchantment of the rarity of the inputted dust can be enchanted.
- There is a *reroll* button, which allows you to refresh the enchantment options. The cost of rerolling doubles each reroll, and resets when you enchant an item.
- The enchantment choices are the same rarity as the inputted enchanted dust. Common corresponds to vanilla enchants.
- Placing more enchanted dust produces higher level enchantments.
- Enchantment cost is determined by its rarity and current/max level.

## Shopkeepers
The plugin also hooks onto Shopkeepers and creates two new types of shopkeepers (combinable), namely:
- Rotating Shopkeeper: Trades that rotate on a timely basis.
- Auction Shopkeeper: Trades that require bidding.

### Parameters
These shopkeepers have new parameters to further enhance supply/demand trading:
- Weights: Adds weights which affects an item's ability to appear in rotations.
- Limits: Adds a buy & sell limit to a certain item.

### Region Markets
The plugin uses the AdvancedRegionMarket API alongside Shopkeepers to link rentable regions to shopkeepers, adding rentable shops: 
- When a region is not rented, the associated shopkeeper will show as "For Hire".
- Renting the region will set the renter as the owner of the shopkeeper, allowing them to customize its trades.
- New shopkeepers cannot be created inside a shop region, so players can only use the provided shopkeeper.
- Shopkeepers associated with a region cannot be deleted.
- When a region is restored, the shopkeeper loses its trades and returns to its original position.
- When a region is sold back to the server, the shopkeeper returns to its original position, and is set to be ready for hiring again.

## Game Mechanics
Finally, the plugin adds a few game mechanics to make the survival experience more balanced and enjoyable.

- Elytras do not work if exposed to rain.
- Powered rails placed on copper blocks increases the maximum speed of Minecarts by 4x, and up to 6x if the copper is fully oxidized.
- Shulkers can be opened by right-clicking them.
