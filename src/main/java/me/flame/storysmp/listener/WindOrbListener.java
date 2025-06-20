package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class WindOrbListener implements Listener {
    private final Minigame minigame;

    private final Map<UUID, Integer> windOrbUsageCount = new HashMap<>();
    private final Map<UUID, Instant> windOrbCooldowns = new HashMap<>();

    public WindOrbListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onRightClick(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (itemStack == null || !itemStack.hasItemMeta()) return;

        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.getPersistentDataContainer().has(ItemFactory.IS_WIND_ORB)) return;

        event.setCancelled(true);

        boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().getBoolean("wind-orb.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        if (minigame.isDiffused(uuid)) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
            return;
        }

        // Check if player is on cooldown
        Instant now = Instant.now();
        Instant cooldownEnd = windOrbCooldowns.get(uuid);
        if (cooldownEnd != null && checkCooldown(cooldownEnd, player)) {
            return;
        }

        Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("wind-orb.cooldown");

        int uses = windOrbUsageCount.getOrDefault(uuid, 0);

        // If player used it twice, put on cooldown
        if (uses == 1) {
            Duration cd = Duration.ofSeconds(totalCooldown);
            windOrbCooldowns.put(uuid, now.plus(Duration.ofSeconds(totalCooldown)));
            windOrbUsageCount.remove(uuid); // Optional: reset usage count immediately
            player.sendMessage(Utils.color("<red>Wind Orb has gone on cooldown for " + cd.getSeconds() + " seconds!"));
        } else {
            windOrbUsageCount.put(uuid, uses + 1);
        }

        // Perform the action
        Utils.dash(player, 8.0);
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) { // essentially true
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("wind-orb.on-cooldown");
            Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("wind-orb.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()))
                    .replace("%total%", String.valueOf(totalCooldown))));
            return true;
        }
        return false;
    }
}