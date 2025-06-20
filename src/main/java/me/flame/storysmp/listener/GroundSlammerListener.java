package me.flame.storysmp.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroundSlammerListener implements Listener {
    private final Map<UUID, Instant> cooldowns = new HashMap<>();
    private final Map<UUID, Location> groundSlammers = new HashMap<>();

    private final Minigame minigame;

    public GroundSlammerListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onUsingGroundSlammer(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ItemFactory.IS_GROUND_SLAMMER)) return;

        if (!StorySMPPlugin.getInstance().getPrimaryConfig().getBoolean("ground-slam.enabled")) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        if (minigame.isDiffused(player.getUniqueId())) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(Utils.color("<red>You cannot slam the ground in creative mode!"));
            return;
        }

        Instant cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null && checkCooldown(cooldown, player)) return;

        int fallDistance = StorySMPPlugin.getInstance().getPrimaryConfig().getInt("ground-slam.minimum-fall-distance");
        if (!isBlockBeneathFarEnough(player, fallDistance)) {
            player.sendMessage(Component.text("You are too close to the ground.").color(NamedTextColor.RED));
            return;
        }

        // Apply slam
        groundSlammers.put(player.getUniqueId(), player.getLocation());
        long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("ground-slam.cooldown");
        cooldowns.put(player.getUniqueId(), Instant.now().plus(Duration.ofSeconds(totalCooldown)));

        double slamSpeed = StorySMPPlugin.getInstance().getPrimaryConfig().getDouble("ground-slam.multiply-fall-speed");

        player.setVelocity(player.getVelocity().setY(-slamSpeed));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("ground-slam.activated")));
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Entity player = event.getEntity();
        if (!(player instanceof Player)) {
            return;
        }

        Location newLocation = player.getLocation();
        Location oldLocation = groundSlammers.get(player.getUniqueId());
        if (oldLocation == null) {
            return;
        }

        groundSlammers.remove(player.getUniqueId());

        event.setCancelled(true);

        ((Player) player).playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.0f, 1.0f);

        Double damageForEveryFiveBlocks = StorySMPPlugin.getInstance().getPrimaryConfig().get("ground-slam.damage-for-each-five-blocks-fallen");

        double blocksFallen = oldLocation.y() - newLocation.y();
        int radius = Math.min((int) ((blocksFallen / 5) * damageForEveryFiveBlocks), 25);
        int damage = ((oldLocation.getBlockY() - newLocation.getBlockY()) / 2);

        List<Entity> entitiesWithinRadius = player.getNearbyEntities(radius, radius, radius);
        for (Entity livingEntity : entitiesWithinRadius) {
            if (!(livingEntity instanceof LivingEntity living)) continue;
            living.damage(damage, player);
        }
    }

    public boolean isBlockBeneathFarEnough(Player player, int minDistance) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        for (int i = 1; i <= minDistance; i++) { // Check blocks below the player
            Location checkLoc = loc.clone().subtract(0, i, 0);
            if (world.getBlockAt(checkLoc).isEmpty()) continue;

            return false;
        }
        return true; // No solid block was found within minDistance
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) {
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("ground-slam.on-cooldown");
            Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().get("ground-slam.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()) + 's')
                    .replace("%total%", totalCooldown.toString())));
            return true;
        }
        return false;
    }
}
