package net.cleocyde.rpgmod.item;

import net.cleocyde.rpgmod.RPGMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item MENU = registerItem("menu", new Item(new FabricItemSettings()));
    public static final Item BOTTES_GARDE_ROYAL = registerItem("bottes_garde_royal", new BottesGardeRoyal( new FabricItemSettings().maxCount(1)));
    public static final Item ANNEAU_PIOU_VERT = registerItem("anneau_piou_vert", new AnneauPiouVert( new FabricItemSettings().maxCount(1)));

    public static final Item XP_ITEM = registerItem("xp_item", new XpItem(new FabricItemSettings()));
    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.add(MENU);
        entries.add(BOTTES_GARDE_ROYAL);
        entries.add(ANNEAU_PIOU_VERT);
        entries.add(XP_ITEM);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RPGMod.MOD_ID, name), item);
    }
    public static void registerModItems() {
        RPGMod.LOGGER.info("Registering Mod Items for " + RPGMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::addItemsToToolsItemGroup);
    }



}
