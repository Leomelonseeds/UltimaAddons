# Ultima Addons
This plugin adds a couple features to the Ultima Kingdoms server that are not present in other plugins. While this plugin is intended for use on the Ultima server, you may compile this plugin for yourself to use on your own server, but I will provide no guarantees or support that it will work for yourself. There are many configuration changes in plugins listed here and otherwise which aren't noted here that contribute to Ultima's experience. The best way to see these features are simply to log on to play.ultimamc.net and join the Kingdoms server (currently under revamp and coming soon).

## Kingdoms
UltimaAddons uses the KingdomsX API by Crypto Morin to provide several extra features.
### Challenges
- Kingdoms can now only challenge 1 Kingdom at a time. However, one Kingdom may get challenged by multiple other Kingdoms.
- There is now a confirmation process before sending a challenge to make sure that it feels like a commitment.
- After a war, there is now a cooldown such that the challenger can't declare war on any Kingdom for 1 day (but they can still be challenged).
- Players will now receive messages upon logging in for upcoming and current wars.
- Players will now receive messages 1 minute before war starts, when it starts, and when it ends.
- You cannot make claims, unclaim, or move your nexus while being challenged.

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
- You cannot create outposts or challenge if you do not have a neuxs.

## Custom Enchantments
The plugin also hooks into AdvancedEnchantments' API to provide the following custom effects:
 
- CAPTURE: Drops a mob's spawn egg. Excludes boss mobs and those defined in the config.
- RECUPERATE: Reduces cooldown time for shields when they are disabled by an axe.

## Custom Items
The plugin also provides some custom items and recipes that add some fresh gameplay elements to the survival experience. All recipes can be found by using the `/recipes` command in-game, and most weapons with abilities have cooldowns that are not listed here, since they are subject to change.

### Weapons
- Blaze Sword: Right-click to shoot 3 fireballs in quick succession.
- Tanto: A short sword that can be swung extremely fast.
- Orcus: Land a critical hit to remove 20% of their remaining health, regardless of armor or effects, in addition to blinding and withering them temporarily.
- Shadowblade: Right-click to teleport 10 blocks in the direction you are facing. If you teleport through a player, you will face that player after the teleport.
- Oxtail Saber: Dual-wield the weapon to attack enemies twice as fast, halving their damage tick cooldown. Visually, other players will see you alternating between attacking with main hand and off hand sword.
- Shiruken: A throwable weapon that damages enemies in rapid bursts.

### Armor
Each armorset has the protection and armor toughness equivalent of Netherite, unless stated otherwise.
- Mithril: Each piece, when worn, gives +1% movement speed per level of Wisdom (from Aurelium) above 25.

### Others
- Diamond Chip: Like iron or gold nuggets, but for diamonds. Used for better economy integration, since Ultima uses a item-based economy with diamonds as its currency.

## Shopkeepers
The plugin also hooks onto Shopkeepers and creates two new types of shopkeepers (combinable), namely:
- Rotating Shopkeeper: Trades that rotate on a timely basis.
- Auction Shopkeeper: Trades that require bidding.

In addition, these shopkeepers have new parameters to further enhance supply/demand trading:
### Parameters
- Weights: Adds weights which affects an item's ability to appear in rotations.
- Limits: Adds a buy & sell limit to a certain item.
