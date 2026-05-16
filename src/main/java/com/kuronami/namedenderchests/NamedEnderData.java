package com.kuronami.namedenderchests;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
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
 */
public class NamedEnderData extends SavedData {

    private static final String NAME = "namedenderchests";
    public static final int SIZE = 27;

    private static final Factory<NamedEnderData> FACTORY =
        new Factory<>(NamedEnderData::new, NamedEnderData::load, null);

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
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag chTag = new CompoundTag();
        channels.forEach((name, list) -> {
            CompoundTag one = new CompoundTag();
            ContainerHelper.saveAllItems(one, list, registries);
            chTag.put(name, one);
        });
        tag.put("channels", chTag);

        CompoundTag bn = new CompoundTag();
        blockNames.forEach(bn::putString);
        tag.put("blocks", bn);
        return tag;
    }

    private static NamedEnderData load(CompoundTag tag, HolderLookup.Provider registries) {
        NamedEnderData d = new NamedEnderData();
        CompoundTag chTag = tag.getCompound("channels");
        for (String name : chTag.getAllKeys()) {
            NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(chTag.getCompound(name), list, registries);
            d.channels.put(name, list);
        }
        CompoundTag bn = tag.getCompound("blocks");
        for (String k : bn.getAllKeys()) {
            d.blockNames.put(k, bn.getString(k));
        }
        return d;
    }
}
