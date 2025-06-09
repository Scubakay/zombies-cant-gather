# Zombies Can't Gather

A Minecraft mod that prevents Zombies from picking up any item you choose!

Since 1.17.1 there's a check in the game to prevent zombies from gathering glow ink sacs. 
Zombies Can't Gather changes that line to check against a blacklist, loaded from the config. 
Zombies are not able to pick up blacklisted items, which prevents them from becoming persistent.

Now with support for Piglins!

> By default, the mod only prevents Zombies from gathering `minecraft:glow_ink_sac`, just like vanilla Minecraft.

## Configuring the blacklists

The `/zombiescantgather` command has a list for both zombies and piglins:
```properties
/zombiescantgather zombie list
/zombiescantgather piglin list
```

Zombies and piglins can not pick up any items on their list. To add items to the list you can use the commands:
```properties
/zombiescantgather zombie add minecraft:rotten_flesh
/zombiescantgather piglin add minecraft:rotten_flesh
```
And of course you can also remove items from the lists so the zombies and piglins can pick up the items again:

```properties
/zombiescantgather zombie add minecraft:rotten_flesh
/zombiescantgather piglin add minecraft:rotten_flesh
```
The item ids are provided as command suggestions, so you don't need to worry about the names.


The `/zombiescantgather` command has options to add/remove/list items that a Zombie can't pick up. The
command will give you suggestions for the items you want to add to the list.

### Manual configuration

The list of items a Zombie can't pick up is stored in a config file: `config/zombiescantgather/mod.properties`:

```properties
zombiescantgather_items=minecraft\:glow_ink_sac,minecraft\:rotten_flesh,minecraft\:egg,minecraft\:bone
zombiescantgather_items=minecraft\:rotten_flesh,minecraft\:egg
```
Items are a comma separated value with escaped colons.

