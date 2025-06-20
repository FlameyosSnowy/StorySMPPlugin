package me.flame.storysmp.listener;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.flame.storysmp.Minigame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

public class ParalyzedListener implements Listener {
    private final Minigame minigame;

    public ParalyzedListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(@NotNull PrePlayerAttackEntityEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(@NotNull PlayerAttemptPickupItemEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(@NotNull PlayerPickupArrowEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        if (minigame.isParalyzed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(InventoryClickEvent event) {
        if (minigame.isParalyzed((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(InventoryDragEvent event) {
        if (minigame.isParalyzed((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
}
