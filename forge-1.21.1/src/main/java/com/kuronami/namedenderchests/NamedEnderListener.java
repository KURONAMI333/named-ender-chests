package com.kuronami.namedenderchests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Three small hooks, all server-side, all skipping unnamed ender chests
 * entirely (those keep the pure vanilla path):
 * <ul>
 *   <li><b>place</b> — if the ender chest was placed from a renamed item,
 *       remember "this position → that channel name".</li>
 *   <li><b>right-click</b> — a named ender chest opens its shared channel
 *       container instead of the vanilla per-player ender inventory.</li>
 *   <li><b>break</b> — forget the position mapping (channel contents are
 *       kept, so another same-name chest still has them).</li>
 * </ul>
 */
public class NamedEnderListener {

    private static String posKey(Level level, BlockPos p) {
        return level.dimension().location() + ":" + p.getX() + "," + p.getY() + "," + p.getZ();
    }

    private static String namedEnderChest(Player player) {
        for (InteractionHand h : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(h);
            if (s.is(Items.ENDER_CHEST) && s.has(DataComponents.CUSTOM_NAME)) {
                String n = s.get(DataComponents.CUSTOM_NAME).getString().trim();
                if (!n.isEmpty()) {
                    return n;
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        if (!event.getPlacedBlock().is(Blocks.ENDER_CHEST)
                || !(event.getEntity() instanceof Player player)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        // CRITICAL: an ender chest can leave a position WITHOUT firing
        // BreakEvent (piston, explosion, /setblock, other mods). If a
        // stale pos->channel mapping survived, a later UNNAMED ender
        // chest placed here would silently open the shared channel,
        // breaking the "unnamed = pure vanilla" guarantee. So on ANY
        // ender-chest placement, clear this position first, then re-set
        // only if the placed item was actually named.
        String posKey = posKey(level, event.getPos());
        NamedEnderData data = NamedEnderData.get(server);
        data.clearBlockName(posKey);
        String name = namedEnderChest(player);
        if (name != null) {
            data.setBlockName(posKey, name);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        BlockPos p = event.getPos();
        if (!level.getBlockState(p).is(Blocks.ENDER_CHEST)
                || !(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }
        MinecraftServer server = sp.getServer();
        if (server == null) {
            return;
        }
        NamedEnderData data = NamedEnderData.get(server);
        String channel = data.blockName(posKey(level, p));
        if (channel == null) {
            return; // unnamed → leave vanilla ender chest alone
        }
        event.setCanceled(true); // suppress vanilla ender inventory
        final String ch = channel;
        sp.openMenu(new SimpleMenuProvider(
            (id, inv, pl) -> ChestMenu.threeRows(id, inv, data.getContainer(ch)),
            Component.translatable("namedenderchests.title", ch)));
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        if (!event.getState().is(Blocks.ENDER_CHEST)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server != null) {
            NamedEnderData.get(server).clearBlockName(posKey(level, event.getPos()));
        }
    }
}
