package com.kuronami.namedenderchests;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named Ender Chests — entry point.
 *
 * <p>Rename an ender chest on an anvil, place it, and every ender chest
 * with that name opens one shared server-wide inventory channel. Unnamed
 * ender chests are never touched (pure vanilla). One listener + a
 * persistent {@link SavedData} store + a vanilla {@code SimpleContainer}
 * subclass. No mixin, no config, no custom block/item — it rides
 * entirely on the vanilla ender chest.
 */
@Mod(NamedEnderChests.MOD_ID)
public class NamedEnderChests {

    public static final String MOD_ID = "namedenderchests";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public NamedEnderChests(IEventBus modBus, ModContainer container) {
        LOGGER.info("Named Ender Chests ready — rename an ender chest to share a channel.");
        NeoForge.EVENT_BUS.register(new NamedEnderListener());
    }
}
