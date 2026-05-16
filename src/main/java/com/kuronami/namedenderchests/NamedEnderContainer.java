package com.kuronami.namedenderchests;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * A 27-slot container backed by one named channel in
 * {@link NamedEnderData}. Subclasses {@link SimpleContainer} so all the
 * slot mechanics are vanilla-correct; only {@code setChanged()} is
 * overridden to flush back into the persistent channel. Multiple players
 * can have one open at once (same backing channel = shared, like a
 * normal chest with two viewers).
 */
public class NamedEnderContainer extends SimpleContainer {

    private final NamedEnderData data;
    private final String channel;

    public NamedEnderContainer(NamedEnderData data, String channel) {
        super(NamedEnderData.SIZE);
        this.data = data;
        this.channel = channel;
        NonNullList<ItemStack> src = data.getChannel(channel);
        for (int i = 0; i < NamedEnderData.SIZE; i++) {
            setItem(i, src.get(i));
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        NonNullList<ItemStack> dst = data.getChannel(channel);
        for (int i = 0; i < NamedEnderData.SIZE; i++) {
            dst.set(i, getItem(i));
        }
        data.setDirty();
    }
}
