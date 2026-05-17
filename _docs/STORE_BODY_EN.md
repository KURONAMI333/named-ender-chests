# Named Ender Chests

> Rename an Ender Chest on an anvil, place it, and every Ender Chest with that name shares one inventory — server-wide. Simple item routing with a vanilla block.

Recurring r/feedthebeast request (14↑ / 22 comments): "a mod that lets me have multiple ender chest inventories by renaming them at an anvil so that those with the same name share an inventory." A tiny vanilla twist for base item-routing — not a storage network, just smarter ender chests.

- 🏷️ Anvil-name an Ender Chest → it opens a shared **channel** of that name
- 🔁 Same name anywhere = same contents (two players, two locations, one inventory)
- 🟣 **Unnamed Ender Chests are 100% vanilla** — your private ender inventory, untouched
- 💾 Channels persist across restarts

## What it does / Usage

1. Rename an Ender Chest item on an anvil (e.g. "farm"), place it. It opens the shared channel "farm".
2. Place another Ender Chest also named "farm" anywhere — same contents.
3. Unnamed Ender Chests behave exactly like vanilla.

Concurrent access works exactly like two players at one normal chest (single shared backing container). Breaking a named chest keeps the channel (another same-name chest still has it).

## Supported loaders / versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | planned | planned |

Forge / Fabric / 1.20.1 ports planned; this release is NeoForge 1.21.1.

## Dependencies

None.

## Compatibility & scope

Server-side. Place / break / right-click listeners + persistent channel data + a vanilla `SimpleContainer` subclass. **No mixin, no config, no custom block or item** — it rides entirely on the vanilla Ender Chest, so it can't conflict with other mods. Channels are server-global by name (intended, for cross-base routing).

## Known limitations

- **No access control — treat the channel name as a shared password, not a lock.** Channels are server-global by name with no ownership: anyone who renames an ender chest to the same string opens the same inventory. This is intentional (cross-base / cross-player routing) but it means a named ender chest is **not private storage** — anyone who knows or guesses the name can read and empty it. For private storage use a plain **unnamed** ender chest (untouched, pure-vanilla per-player). Pick obscure names for anything you don't want shared.
- The name is captured when the chest is placed from a renamed item; placing the very last one in a stack may not capture it (place from a stack of 2+).
- A named ender chest **moved by a piston / structure block reverts to a vanilla ender chest** (the name is only captured at hand-placement) — its contents are safe in the channel; break and re-place a same-named chest to rebind.

## Install

1. Install NeoForge for Minecraft 1.21.1.
2. Drop `namedenderchests-0.1.0.jar` into `mods/`. Server-side (clients don't need it).

- Minecraft 1.21.1 · NeoForge · JDK 21

## Languages

Menu title localized in 9 languages.

## License

MIT — modpack inclusion welcome, no credit required.

Author: KURONAMI
