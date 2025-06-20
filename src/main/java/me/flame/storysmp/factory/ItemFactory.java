package me.flame.storysmp.factory;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.UUIDDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

public class ItemFactory {
    public static final NamespacedKey IS_ENCHANTED_CATALYST =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-enchanted-catalyst");

    public static final NamespacedKey IS_COPPER_APPLE =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-copper-apple");

    public static final NamespacedKey WIND_CHARGES = new NamespacedKey(StorySMPPlugin.getInstance(), "wind_charges");

    public static final NamespacedKey IS_CHARGEABLE_MACE =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-chargeable-mace");

    public static final NamespacedKey IS_DIFFUSER =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-diffuser");

    public static final NamespacedKey IS_GROUND_SLAMMER =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-ground-slammer");

    public static final NamespacedKey IS_OMEN_STAFF =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-omen-staff");

    public static final NamespacedKey IS_WIND_ORB =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-wind-orb");

    public static final NamespacedKey IS_OMEN_STAFF_MOB =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-omen-staff-mob");

    public static final NamespacedKey OWNER =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-owner");

    public static final NamespacedKey IS_SOUL =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-soul");

    public static final NamespacedKey IS_DRAGON_CHESTPLATE =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-dragon-chestplate");

    public static final UUIDDataType UUID_DATA_TYPE = new UUIDDataType();

    public static final NamespacedKey IS_POSEIDONS_TRIDENT =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-poseidons-trident");

    public static final NamespacedKey IS_SHURIKEN =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-shuriken");

    public static final NamespacedKey IS_PHARAOHS_PRISM =
            new NamespacedKey(StorySMPPlugin.getInstance(), "is-pharaohs-prism");

    public ItemStack getEnchantedCatalyst() {
        ItemStack stack = new ItemStack(Material.SCULK_CATALYST, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_ENCHANTED_CATALYST, PersistentDataType.BOOLEAN, true);

        itemMeta.setEnchantmentGlintOverride(true);

        itemMeta.displayName(Component.text("Enchanted Catalyst").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getCopperApple() {
        ItemStack stack = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_COPPER_APPLE, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Copper Apple")
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(184, 115, 51)));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getChargeableMace() {
        ItemStack stack = new ItemStack(Material.MACE, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_CHARGEABLE_MACE, PersistentDataType.BOOLEAN, true);
        itemMeta.getPersistentDataContainer().set(WIND_CHARGES, PersistentDataType.INTEGER, 0);
        itemMeta.displayName(Component.text("Chargeable Mace")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GREEN));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getPharaohsPrism() {
        ItemStack stack = new ItemStack(Material.WARPED_FUNGUS, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_PHARAOHS_PRISM, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Pharaohs Prism")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.YELLOW));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getDiffuser() {
        ItemStack stack = ItemStack.of(Material.CARROT_ON_A_STICK, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_DIFFUSER, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Diffuser")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.RED));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getSoul() {
        ItemStack stack = ItemStack.of(Material.DRIED_KELP, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_SOUL, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Soul")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getWindOrb() {
        ItemStack stack = ItemStack.of(Material.WIND_CHARGE, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_WIND_ORB, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Wind Orb")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getGroundSlammer() {
        ItemStack stack = ItemStack.of(Material.WARPED_FUNGUS_ON_A_STICK, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_GROUND_SLAMMER, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Ground Slammer")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.DARK_RED));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getOmenStaff() {
        ItemStack stack = ItemStack.of(Material.WARPED_FUNGUS_ON_A_STICK, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_OMEN_STAFF, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Omen Staff")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.DARK_PURPLE));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack createDragonsChestplate() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ArmorMeta meta = (ArmorMeta) item.getItemMeta();

        meta.displayName(Component.text("Dragon’s Chestplate")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.DARK_PURPLE));

        TrimPattern pattern = TrimPattern.EYE;
        TrimMaterial material = TrimMaterial.NETHERITE;

        ArmorTrim trim = new ArmorTrim(material, pattern);
        meta.setTrim(trim);

        meta.getPersistentDataContainer().set(IS_DRAGON_CHESTPLATE, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getShuriken() {
        ItemStack stack = ItemStack.of(Material.SNOWBALL, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_SHURIKEN, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Shuriken")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.DARK_RED));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public ItemStack getPoseidonsTrident() {
        ItemStack stack = ItemStack.of(Material.TRIDENT, 1);
        ItemMeta itemMeta = stack.getItemMeta();

        itemMeta.getPersistentDataContainer().set(IS_POSEIDONS_TRIDENT, PersistentDataType.BOOLEAN, true);
        itemMeta.displayName(Component.text("Poseidons Trident")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.DARK_AQUA));
        stack.setItemMeta(itemMeta);
        return stack;
    }
}
