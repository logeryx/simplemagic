package net.logeryx.simplemagic.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.logeryx.simplemagic.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;


public class ModLootTableModifiers {
    // Main method
    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            // Evokers drop Fang's Spirit
            if (EntityType.EVOKER.getLootTableKey().map(k -> k.equals(key)).orElse(false)) {
                addToLootTable(tableBuilder, ModItems.FANGS_SPIRIT, registries,1.0f, 6.0f, 1.0f);
            }
        });
    }
    // The Helper Method
    private static void addToLootTable(LootTable.Builder tableBuilder, ItemConvertible item, RegistryWrapper.WrapperLookup registries,
                                       float min, float max, float lootingBonus) {

        var baseCount = SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max));

        var lootingFunc = EnchantedCountIncreaseLootFunction.builder(
                registries,
                UniformLootNumberProvider.create(0.0f, lootingBonus)
        );
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1.0f, 1.0f))
                .with(ItemEntry.builder(item)
                        .apply(baseCount)
                        .apply(lootingFunc)
                );

        tableBuilder.pool(poolBuilder);
    }
}
