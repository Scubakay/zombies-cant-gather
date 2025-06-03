# Zombies Can't Gather

A Minecraft mod that prevents Zombies from picking up any item you choose!

This mod is aimed at server admins who have to deal with Zombies picking up stuff like
eggs and rotten flesh, causing them to not despawn. Add the items to the list through commands
or the config file to prevent Zombies from picking up those items.

> By default, the mod only prevents Zombies from gathering `minecraft:glow_ink_sac`, just like vanilla Minecraft.

## Configuration

The `/zombiescantgather` command has options to add/remove/list items that a Zombie can't pick up. The
command will give you suggestions for the items you want to add to the list.

### Commands

```
/zombiescantgather add minecraft:white_concrete
```
After adding `minecraft:white_concrete` Zombies won't be able to gather white concrete.

```
/zombiescantgather remove minecraft:white_concrete
```
After removing `minecraft:white_concrete` Zombies will be able to gather white concrete again.

```
/zombiescantgather list
```
Lists every item in the list

### Manual configuration

The list of items a Zombie can't pick up is stored in a config file: `config/zombiescantgather/mod.properties`:

```properties
zombiescantgather_items=minecraft\:glow_ink_sac,minecraft\:rotten_flesh,minecraft\:white_concrete,minecraft\:acacia_leaves
```
Items are a comma separated value with escaped colons.

