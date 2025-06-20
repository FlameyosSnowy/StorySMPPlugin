package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.blocks.EnchantedCatalyst;
import me.flame.storysmp.factory.ItemFactory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

//@SuppressWarnings("experimental")
public class PlayerListener implements Listener {
    private final Minigame minigame;

    public PlayerListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) return;

        Block block = event.getBlock();
        Location location = block.getLocation();

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        if (!persistentDataContainer.has(ItemFactory.IS_ENCHANTED_CATALYST)) return;

        EnchantedCatalyst catalyst = new EnchantedCatalyst(block, block.getType(), item, location, minigame);
        catalyst.placeBlock();
    }


}
