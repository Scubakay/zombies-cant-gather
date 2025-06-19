This version introduces the tracker:
- Tracks mobs with blacklisted items when they are loaded
- List tracked mobs with `/zcg tracker`
- Clear tracker with `/zcg tracker reset`
- Kill all loaded listed mobs with `/zcg tracker purge`
- Teleport to tracked mobs with buttons in the list

Changes:
- Client side support
- Mod Menu support
- Config options also available under `/zcg config`
- Blacklists are now paginated
- New alias `/zcg` (redirects to `/zombiescantgather`)
- Option to log mobs with blacklisted items in console