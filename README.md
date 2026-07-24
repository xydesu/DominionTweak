# DominionTweak

DominionTweak is an add-on plugin for Dominion, providing independent ban lists and additional administrative tools to improve server management.

## Features

1. **Independent Ban System**
   - Members are no longer managed through Dominion's native interface for bans.
   - Separate `bans.yml` to store banned players without polluting the original member list.
   - Prevents banned players from entering the territory (blocks Move and Teleport events).

2. **Set Home Restriction**
   - Prevents non-members (Guests) from setting a home inside a Dominion territory.
   - Intercepts commands from common plugins like HuskHomes, CMI, and EssentialsX (`/sethome`, `/cmi sethome`, `/huskhomes:sethome`, etc.).
   - Adds a custom flag (Set Home) to the Dominion GUI, allowing owners to toggle this permission for members.

3. **Admin Tools & Bypasses**
   - Comprehensive admin commands to manage any territory's ban list.
   - Bypass permissions allow administrators to ignore territory bans and home-setting restrictions.

4. **Fly Bug Fix**
   - Patches a bug in Dominion where players could fly infinitely after teleporting out of a territory.
   - Automatically disables flight when a player teleports out of a territory or logs out while outside of one (ignoring Creative/Spectator modes).

## Commands

- `/dt ban <player>`: Add a player to your territory ban list.
- `/dt unban <player>`: Remove a player from your territory ban list.
- `/dt admin ban <owner> <target>`: Forcibly ban a player from someone else's territory.
- `/dt admin unban <owner> <target>`: Forcibly unban a player.
- `/dt admin list <owner>`: View the ban list of a specific territory owner.
- `/dt admin clear <owner>`: Clear the entire ban list of a specific territory owner.
- `/dt reload`: Reload the plugin configuration and language files without restarting the server.

## Permissions

- `dominiontweak.admin`: Grants access to `/dt reload` and all `/dt admin` commands.
- `dominiontweak.bypass`: Allows the player to bypass all territory restrictions (like walk, tp).
- `dominiontweak.bypass.ban`: Allows the player to enter any territory, ignoring ban lists.
- `dominiontweak.bypass.sethome`: Allows the player to set homes in any territory, ignoring restrictions.
- `dominiontweak.bypass.walk`: Allows the player to bypass territory walk restrictions.
- `dominiontweak.bypass.tp`: Allows the player to bypass territory tp restrictions.
## Configuration

Language strings and plugin messages are fully customizable in `plugins/DominionTweak/lang/zh-TW.yml` and `en-US.yml`.

## Installation

1. Place `DominionTweak-1.0.0.jar` into your `plugins` folder.
2. Restart the server to properly register custom flags.
3. If the custom flag doesn't appear in the Dominion GUI, check `plugins/Dominion/flags.yml`, ensure `enable: true` is set under `sethome`, and type `/dom reload`.
