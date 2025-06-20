package me.flame.storysmp.commands;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum CustomItem {
    SOUL,
    WIND_ORB,
    DIFFUSER,
    GROUND_SLAMMER,
    COPPER_APPLE,
    CHARGEABLE_MACE,
    ENCHANTED_CATALYST,
    POSEIDONS_TRIDENT,
    OMEN_STAFF,
    SHURIKEN,
    PHARAOHS_PRISM,
    DRAGONS_CHESTPLATE;

    public static final Map<CustomItem, ItemStack> items = new EnumMap<>(CustomItem.class);

    public static void init() {
        ItemFactory factory = StorySMPPlugin.getInstance().getItemFactory();
        items.put(SOUL, factory.getSoul());
        items.put(WIND_ORB, factory.getWindOrb());
        items.put(DIFFUSER, factory.getDiffuser());
        items.put(GROUND_SLAMMER, factory.getGroundSlammer());
        items.put(COPPER_APPLE, factory.getCopperApple());
        items.put(CHARGEABLE_MACE, factory.getChargeableMace());
        items.put(ENCHANTED_CATALYST, factory.getEnchantedCatalyst());
        items.put(OMEN_STAFF, factory.getOmenStaff());
        items.put(SHURIKEN, factory.getShuriken());
        items.put(PHARAOHS_PRISM, factory.getPharaohsPrism());
        items.put(POSEIDONS_TRIDENT, factory.getPoseidonsTrident());
        items.put(DRAGONS_CHESTPLATE, factory.createDragonsChestplate());
    }

    public ItemStack getCustomItemStack() {
        return items.get(this);
    }
}
