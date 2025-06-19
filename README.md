# Zombies Can't Gather

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/683FasTt?label=modrinth&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+)](https://modrinth.com/mod/zombies-cant-gather)
![Modrinth Followers](https://img.shields.io/modrinth/followers/683FasTt?color=#97ca00)
![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/683FasTt?color=#97ca00)

A Minecraft mod that prevents Zombies from picking up any item you choose!

Since 1.17.1 there's a check in the game to prevent zombies from gathering glow ink sacs.
Zombies Can't Gather changes that line to check against a blacklist, loaded from the config.
Zombies are not able to pick up blacklisted items, which prevents them from becoming persistent.

Now with a fancy tracker!

> By default, the mod only prevents Zombies from gathering `minecraft:glow_ink_sac`, just like vanilla Minecraft.

## Blacklists

Zombies Can't Gather provides blacklists of items that prevent Zombies or Piglins from picking them up. These
blacklists are available through the command `/zombiescantgather zombie` and `/zombiescantgather piglin`. From here,
you can add or remove items:

![blacklist_list.png](https://github.com/Scubakay/zombies-cant-gather/blob/master/docs/img/blacklist_list.png)

When an item is on the blacklist, the corresponding mob can not pick it up.

To reset the lists, use the following commands:

```properties
/zombiescantgather zombie reset
/zombiescantgather piglin reset
```

## Tracker

Do you have an existing world where mobs are already holding items you added to the blacklist? Turn on the tracker
and sniff them out. To turn on the tracker, use set `enableTracker` to true in the config, or if you use are in
single player, use Mod Menu. The tracker is turned off by default.

```properties
/zombiescantgather config enableTracker true
```

The tracker in Zombies Can't Gather logs of every time a mob holding a blacklisted item is loaded so you can
use a command to see where all those persistent mobs are. When turned on, running the command
`/zombiescantgather tracker` will show information about those mobs, like the item they are holding and
the amount of times the mob has been loaded.

![tracker_list.png](https://github.com/Scubakay/zombies-cant-gather/blob/master/docs/img/tracker_list.png)

At the bottom of the tracker is the `Purge` button. This button will kill any entities on the list, provided that
they are currently loaded. After running a purge, the tracker will refresh, leaving only the mobs that couldn't
be found due to them being in unloaded chunks. Here's where the `TP` button comes in handy. This teleport works
with mobs in unloaded chunks, so you can just teleport to the next remaining mob and run purge again when you arrive.

![tracker_teleport.png](https://github.com/Scubakay/zombies-cant-gather/blob/master/docs/img/tracker_teleport.png)

Running `/zombiescantgather tracker reset` will clear the list, so you can start fresh.

## Permissions

By default, everything in Zombies Can't Gather is locked under Permission Level 4 (configurable), but for more
control on permissions Luckperms support is available. The following permissions can be configured:

| Permission                           | Grants/revokes                          |
|--------------------------------------|-----------------------------------------|
| `zombiescantgather`                  | Everything                              |
| `zombiescantgather.*`                | All commands under `/zombiescantgather` |
| `zombiescantgather.blacklist`        | Access to blacklists                    |
| `zombiescantgather.blacklist.add`    | Adding blacklist items                  |
| `zombiescantgather.blacklist.remove` | Removing blacklist items                |
| `zombiescantgather.blacklist.reset`  | Resetting blacklists                    |
| `zombiescantgather.tracker`          | Access to tracker                       |
| `zombiescantgather.tracker.log`      | Receive updates on tracked mobs in chat |
| `zombiescantgather.tracker.reset`    | Clearing the tracker                    |
| `zombiescantgather.tracker.teleport` | Teleporting to tracked mobs             |
| `zombiescantgather.tracker.purge`    | Purging tracked mobs                    |
| `zombiescantgather.configure`        | Changing configuration                  |

## Configuration

Configuration options to use the mod in single player are available through Mod Menu. Configuration options
are also available through the command `/zcg config <option> <value>` or in `config/zombiescantgather.json`:

```json
{
  "zombiesBlacklist": [
    "minecraft:glow_ink_sac"
  ],
  "piglinsBlacklist": [],
  "permissionLevel": 4,
  "enableTracker": false,
  "trackCustomNamedMobs": false,
  "showTrackerLogs": false,
  "broadcastTrackedMobs": false
}
```