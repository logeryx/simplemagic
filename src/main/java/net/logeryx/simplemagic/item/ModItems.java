package net.logeryx.simplemagic.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.logeryx.simplemagic.SimpleMagic;
import net.logeryx.simplemagic.item.custom.FangsSpiritConsumableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final RegistryKey<Item> key = RegistryKey.of( RegistryKeys.ITEM, Identifier.of(SimpleMagic.MOD_ID, "fangs_spirit"));
    public static final FangsSpiritConsumableItem FANGS_SPIRIT = Registry.register(Registries.ITEM, key,
            new FangsSpiritConsumableItem( new Item.Settings().registryKey(key))
    );

    public static void registerModItems() {
        SimpleMagic.LOGGER.info("Registering items for mod " + SimpleMagic.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(FANGS_SPIRIT);
        });
    }
}
