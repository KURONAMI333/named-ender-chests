package com.kuronami.namedenderchests;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named Ender Chests — entry point (Fabric 1.20.1).
 *
 * <p>Rename an ender chest on an anvil, place it, and every ender chest
 * with that name opens one shared server-wide inventory channel. Unnamed
 * ender chests are never touched (pure vanilla).
 *
 * <p>Fabric has no block-place event, so placement name-capture rides a
 * tiny {@code BlockItem#placeBlock} mixin that calls
 * {@link #onBlockPlaced}. Right-click open uses {@code UseBlockCallback}
 * (return {@code SUCCESS} to suppress the vanilla ender inventory),
 * break-clear uses {@code PlayerBlockBreakEvents.AFTER}.
 *
 * <p>1.20.1 has no {@code DataComponents.CUSTOM_NAME}; the renamed-item
 * check uses the legacy hover-name API
 * ({@code hasCustomHoverName()} / {@code getHoverName()}).
 */
public class NamedEnderChestsFabric implements ModInitializer {

    public static final String MOD_ID = "namedenderchests";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static String posKey(Level level, BlockPos p) {
        return level.dimension().location() + ":" + p.getX() + "," + p.getY() + "," + p.getZ();
    }

    private static String namedEnderChest(Player player) {
        for (InteractionHand h : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(h);
            if (s.is(Items.ENDER_CHEST) && s.hasCustomHoverName()) {
                String n = s.getHoverName().getString().trim();
                if (!n.isEmpty()) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Called from {@code BlockItemMixin} the moment vanilla writes a
     * block into the world. Mirrors the NeoForge
     * {@code BlockEvent.EntityPlaceEvent} handler: on ANY ender-chest
     * placement clear this position's stale mapping first, then re-set it
     * only if the placed item carried a non-empty custom name.
     */
    public static void onBlockPlaced(Level level, BlockPos pos,
                                     BlockState placed, Player player) {
        if (level == null || level.isClientSide() || player == null) {
            return;
        }
        if (!placed.is(Blocks.ENDER_CHEST)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        String key = posKey(level, pos);
        NamedEnderData data = NamedEnderData.get(server);
        data.clearBlockName(key);
        String name = namedEnderChest(player);
        if (name != null) {
            data.setBlockName(key, name);
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Named Ender Chests ready — rename an ender chest to share a channel.");

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            BlockPos p = hit.getBlockPos();
            if (!level.getBlockState(p).is(Blocks.ENDER_CHEST)
                    || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            MinecraftServer server = sp.getServer();
            if (server == null) {
                return InteractionResult.PASS;
            }
            NamedEnderData data = NamedEnderData.get(server);
            String channel = data.blockName(posKey(level, p));
            if (channel == null) {
                return InteractionResult.PASS; // unnamed → pure vanilla
            }
            final String ch = channel;
            sp.openMenu(new SimpleMenuProvider(
                (id, inv, pl) -> ChestMenu.threeRows(id, inv, data.getContainer(ch)),
                Component.translatable("namedenderchests.title", ch)));
            return InteractionResult.SUCCESS; // suppress vanilla ender inv
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide() || !state.is(Blocks.ENDER_CHEST)) {
                return;
            }
            MinecraftServer server = level.getServer();
            if (server != null) {
                NamedEnderData.get(server).clearBlockName(posKey(level, pos));
            }
        });
    }
}
