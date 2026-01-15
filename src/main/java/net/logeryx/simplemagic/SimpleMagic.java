package net.logeryx.simplemagic;

import net.fabricmc.api.ModInitializer;
import net.logeryx.simplemagic.item.ModItems;
import net.logeryx.simplemagic.loot.ModLootTableModifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMagic implements ModInitializer {
	public static final String MOD_ID = "simplemagic";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModLootTableModifiers.modifyLootTables();
	}
}