package com.kuronami.namedenderchests;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Server-wide state: every named channel's 27-slot contents, plus which
 * placed ender-chest positions map to which channel name. Vanilla
 * {@link SavedData} so it survives restarts
 * ({@code world/data/namedenderchests.dat}). Unnamed ender chests are
 * never recorded here — they stay 100% vanilla.
 *
 * <p>1.20.1 {@link SavedData} pre-dates the registry-aware serialization:
 * {@code save(CompoundTag)} / {@code load(CompoundTag)} take no
 * {@code HolderLookup.Provider}, there is no {@code Factory}, and the
 * accessor is {@code computeIfAbsent(loadFn, ctorFn, NAME)} (loader
 * first). {@code ContainerHelper.save/loadAllItems} likewise take no
 * registries here.
 */
public class NamedEnderData extends SavedData {

    private static final String NAME = "namedenderchests";
    public static final int SIZE = 27;

    /** channel name -> 27 slots (the persistent backing store). */
    private final Map<String, NonNullList<ItemStack>> channels = new HashMap<>();
    /** "dim:x,y,z" -> channel name. */
    private final Map<String, String> blockNames = new HashMap<>();
    /** One live shared container per channel so concurrent viewers behave
     *  exactly like two players at the same vanilla chest. */
    private final Map<String, NamedEnderContainer> live = new HashMap<>();

    /** The single shared container for a channel (lazily built from the
     *  persistent slots, reused while the server runs). */
    public NamedEnderContainer getContainer(String name) {
        return live.computeIfAbsent(name, n -> new NamedEnderContainer(this, n));
    }

    public static NamedEnderData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
            .computeIfAbsent(NamedEnderData::load, NamedEnderData::new, NAME);
    }

    public NonNullList<ItemStack> getChannel(String name) {
        return channels.computeIfAbsent(name,
            k -> NonNullList.withSize(SIZE, ItemStack.EMPTY));
    }

    public void setBlockName(String posKey, String name) {
        blockNames.put(posKey, name);
        setDirty();
    }

    public void clearBlockName(String posKey) {
        if (blockNames.remove(posKey) != null) {
            setDirty();
        }
    }

    /** Channel name for a placed ender chest position, or null if unnamed. */
    public String blockName(String posKey) {
        return blockNames.get(posKey);
    }

    /**
     * Copy every live container's current contents into the persistent
     * {@code channels} map. Normal edits flush via the container's
     * {@code setChanged()}, but that is an invariant we don't want to
     * bet item safety on across a crash / {@code /stop} mid-session, so
     * we reconcile here before every serialize. Cheap insurance against
     * silent item loss.
     */
    private void flushLive() {
        live.forEach((name, c) -> {
            NonNullList<ItemStack> dst = channels.computeIfAbsent(
                name, k -> NonNullList.withSize(SIZE, ItemStack.EMPTY));
            for (int i = 0; i < SIZE; i++) {
                dst.set(i, c.getItem(i));
            }
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        flushLive();
        CompoundTag chTag = new CompoundTag();
        channels.forEach((name, list) -> {
            CompoundTag one = new CompoundTag();
            ContainerHelper.saveAllItems(one, list);
            chTag.put(name, one);
        });
        tag.put("channels", chTag);

        CompoundTag bn = new CompoundTag();
        blockNames.forEach(bn::putString);
        tag.put("blocks", bn);
        return tag;
    }

    private static NamedEnderData load(CompoundTag tag) {
        NamedEnderData d = new NamedEnderData();
        CompoundTag chTag = tag.getCompound("channels");
        for (String name : chTag.getAllKeys()) {
            NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(chTag.getCompound(name), list);
            d.channels.put(name, list);
        }
        CompoundTag bn = tag.getCompound("blocks");
        for (String k : bn.getAllKeys()) {
            d.blockNames.put(k, bn.getString(k));
        }
        return d;
    }
}
