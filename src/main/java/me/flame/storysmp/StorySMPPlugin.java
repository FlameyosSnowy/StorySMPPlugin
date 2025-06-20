package me.flame.storysmp;

import me.flame.storysmp.commands.*;
import me.flame.storysmp.config.Config;
import me.flame.storysmp.config.MessagesConfig;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.listener.PlayerListener;
import me.flame.storysmp.listener.PlayerQuestListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitLamp;

public class StorySMPPlugin extends JavaPlugin {
    private static StorySMPPlugin instance;
    private ItemFactory itemFactory;
    private Config config;
    private MessagesConfig messagesConfig;
    private Minigame minigame;
    public AbilityTwoCommand abilityTwoCommand;

    @Override
    public void onEnable() {
        instance = this;

        this.config = new Config(this);
        this.messagesConfig = new MessagesConfig(this);

        this.minigame = new Minigame(this);

        this.itemFactory = new ItemFactory();
        getLogger().info("Initialized item factory.");

        ItemStack result = itemFactory.createDragonsChestplate();
        ShapelessRecipe recipe = new ShapelessRecipe(ItemFactory.IS_DRAGON_CHESTPLATE, result);

        recipe.addIngredient(Material.DIAMOND_CHESTPLATE);
        recipe.addIngredient(Material.NETHERITE_INGOT);
        recipe.addIngredient(Material.DRAGON_EGG);
        recipe.addIngredient(Material.DRAGON_BREATH);
        recipe.addIngredient(Material.CHORUS_FRUIT);

        Bukkit.addRecipe(recipe);
        CustomItem.init();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(minigame), this);

        var lamp = BukkitLamp.builder(this).build();
        this.abilityTwoCommand = new AbilityTwoCommand(minigame);
        lamp.register(new AbilityFourCommand(), new AbilityThreeCommand(), abilityTwoCommand, new GiveCustomItemCommand(), new AbilityOneCommand(), new ReloadCommand(), new StopCommand(), new CorruptCommand());

        getLogger().info("Successfully enabled StorySMPPlugin");
    }

    @Override
    public void onDisable() {
        if (minigame != null) minigame.stop();
    }

    public static StorySMPPlugin getInstance() {
        return instance;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public @NotNull Config getPrimaryConfig() {
        return config;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public Minigame getMinigame() {
        return minigame;
    }
}
