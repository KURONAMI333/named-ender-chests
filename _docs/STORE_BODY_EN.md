# Named Ender Chests

Rename an Ender Chest on an anvil, place it, and every Ender Chest with that name shares one inventory — server-wide. Simple item routing with a vanilla block; unnamed Ender Chests stay 100% vanilla.

A small vanilla twist for base item-routing — not a storage network, just smarter ender chests.

1. Rename an Ender Chest item on an anvil (e.g. "farm") and place it. It opens the shared channel "farm".
2. Place another Ender Chest also named "farm" anywhere — same contents.
3. Unnamed Ender Chests behave exactly like vanilla: your private per-player inventory, untouched.

Concurrent access works like two players at one normal chest (a single shared backing container). Breaking a named chest keeps the channel as long as another same-name chest exists. Channels persist across restarts.

**No access control — treat the channel name as a shared password, not a lock.** Channels are server-global by name with no ownership: anyone who renames a chest to the same string opens the same inventory. This is intentional, for cross-base routing — but it means a named ender chest is not private storage; use a plain unnamed one for that. Two edges to know: the name is captured only at hand-placement, so placing the last item in a stack may not capture it (place from a stack of 2+), and a named chest moved by a piston or structure block reverts to vanilla — its contents stay safe in the channel, just re-place a same-named chest to rebind.

It rides entirely on the vanilla Ender Chest — no mixin, no config, no custom block or item — so it can't conflict with other mods.

Server-side — install on the server only; clients don't need it.

Free to use in any modpack. Source and issues: https://github.com/KURONAMI333/named-ender-chests
