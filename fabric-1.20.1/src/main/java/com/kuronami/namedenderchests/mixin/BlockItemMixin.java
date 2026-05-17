package com.kuronami.namedenderchests.mixin;

import com.kuronami.namedenderchests.NamedEnderChestsFabric;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 1.20.1: Fabric has no block-place event, so we hook the exact
 * vanilla method that writes the block into the world —
 * {@link BlockItem#placeBlock(BlockPlaceContext, BlockState)}. At its
 * {@code RETURN} the {@code context} is the final updated context, so
 * {@code context.getClickedPos()} is precisely where the block went and
 * {@code context.getPlayer()} is the placer. This reproduces the
 * NeoForge {@code BlockEvent.EntityPlaceEvent} semantics (including the
 * "clear any stale pos→channel mapping on every ender-chest placement"
 * guarantee) without dropping functionality.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void namedenderchests$onPlaceBlock(BlockPlaceContext context,
                                               BlockState state,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        NamedEnderChestsFabric.onBlockPlaced(
            context.getLevel(), context.getClickedPos(),
            state, context.getPlayer());
    }
}
