# Lootr 1.7.10 GTNH

A backport of [Lootr](https://github.com/noobanidus/Lootr) by noobanidus and embeddedt from Minecraft 1.12.2 to 1.7.10, built on the [GTNewHorizons ExampleMod](https://github.com/GTNewHorizons/ExampleMod1.7.10) build system.

## What It Does

Lootr makes loot chests per-player. When a player opens a loot chest for the first time, they receive their own instance of the loot. Other players who open the same chest get their own separate loot. Previously opened chests render with a different texture so players know they've already looted them.

## Features

- Per-player loot for chests, trapped chests, minecarts, and custom inventory containers
- Server-side inventory storage via WorldSavedData
- Decay timers (configurable per-dimension) -- chests can be set to disappear after being opened
- Refresh timers -- chests can re-roll their loot after a configurable cooldown
- Break protection -- requires sneaking to break Lootr containers
- `/lootr` command for spawning chests/minecarts, clearing player data, listing openers
- Custom renderer showing opened vs unopened state per player
- World-gen chest conversion via Mixin (replaces vanilla chests during chunk generation)
- Minecart chest conversion (replaces vanilla chest minecarts)

## Port Status

Confirmed working in GTNH multiplayer on 1.7.10.

| Feature | Status |
|---------|--------|
| Per-player loot chests | Done |
| Trapped loot chests | Done |
| Lootr inventory/barrel | Done |
| Lootr minecarts | Done |
| Decay/refresh timers | Done |
| Config system | Done |
| Networking | Done |
| Custom renderer (opened/unopened texture) | Done |
| Chest lid animation | Done |
| Break protection | Done |
| World-gen chest conversion | Done |
| Minecart chest conversion | Done |
| `/lootr` command | Done |
| Shulker box support | Dropped (not in 1.7.10) |

## Notes

- Loot data is stored per-chest UUID via `WorldSavedData` (MapStorage) in `data/lootr/` inside the world save folder.
- In 1.7.10 there are no loot tables — world-gen chests already contain items. `convert_worldgen_inventories` is enabled by default to handle this.
- Uses [UniMixins](https://github.com/LegacyModdingMC/UniMixins) for mixin support, which is included in GTNH.

## Building

1. Clone the repository
2. Run `./gradlew setupDecompWorkspace`
3. Run `./gradlew build`

The output JAR will be in `build/libs/`.

## Configuration

All configuration lives in `config/lootr.cfg` (generated on first run). Key options:

- `disable_break` -- prevent breaking Lootr containers
- `decay_value` -- ticks before a decaying container expires (default 5 minutes)
- `refresh_value` -- ticks before container loot refreshes (default 20 minutes)
- `convert_mineshafts` -- replace vanilla chest minecarts with Lootr minecarts
- `convert_worldgen_inventories` -- convert legacy chests without loot tables

## Credits

- Original Lootr mod for 1.12.2 by [noobanidus](https://github.com/noobanidus) and [embeddedt](https://github.com/embeddedt)
- Build system by [GTNewHorizons](https://github.com/GTNewHorizons)
- 1.7.10 backport by Vonbraunz

## License

MIT -- see LICENSE file.
