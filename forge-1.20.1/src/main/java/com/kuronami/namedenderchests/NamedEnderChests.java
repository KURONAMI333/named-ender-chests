package com.kuronami.namedenderchests;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named Ender Chests — entry point (Forge 1.20.1).
 *
 * <p>Rename an ender chest on an anvil, place it, and every ender chest
 * with that name opens one shared server-wide inventory channel. Unnamed
 * ender chests are never touched (pure vanilla). One listener + a
 * persistent {@link net.minecraft.world.level.saveddata.SavedData} store
 * + a vanilla {@code SimpleContainer} subclass. No mixin, no config, no
 * custom block/item — it rides entirely on the vanilla ender chest.
 *
 * <p>Forge 47.x (1.20.1) uses a no-arg {@code @Mod} constructor; only the
 * game event bus is needed here.
 */
@Mod(NamedEnderChests.MOD_ID)
public class NamedEnderChests {

    public static final String MOD_ID = "namedenderchests";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public NamedEnderChests() {
        LOGGER.info("Named Ender Chests ready — rename an ender chest to share a channel.");
        MinecraftForge.EVENT_BUS.register(new NamedEnderListener());
    }
}
