# Named Ender Chests

> Rename an Ender Chest on an anvil, place it, and every Ender Chest with that name shares one inventory — server-wide. Simple item routing with a vanilla block.

## What it does

- Anvil-rename an Ender Chest item (e.g. "farm"), place it. It now opens a **shared channel** named "farm".
- Place another Ender Chest also named "farm" anywhere — same contents. Two players, two locations, one inventory.
- **Unnamed Ender Chests are 100% vanilla** — your normal private ender inventory, completely untouched.
- Channels persist across restarts. Breaking a named chest keeps the channel (another same-name chest still has it).

Concurrent access works exactly like two players at one normal chest (single shared backing container).

## Why

Recurring r/feedthebeast request (14↑ / 22 comments): "a mod that lets me have multiple ender chest inventories... by renaming them at an anvil so that those with the same name share an inventory." A tiny vanilla twist for base item-routing — not a storage network, just smarter ender chests.

## Install

Drop `namedenderchests-<version>.jar` into `mods/`. Server-side (clients don't need it). No dependencies.

- Minecraft 1.21.1 · NeoForge · JDK 21

## Scope

Place / break / right-click listeners + persistent channel data + a vanilla `SimpleContainer` subclass. **No mixin, no config, no custom block or item** — it rides entirely on the vanilla Ender Chest. Channels are server-global by name (intended, for cross-base routing).

Known v0.1 limitation: the name is captured when the chest is placed from a renamed item; if you place the very last one in a stack the name may not be captured (place from a stack of 2+). 9 languages.

## License

MIT — modpack inclusion welcome, no credit required.

Author: KURONAMI
