package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShurikenListener implements Listener {
    private final Minigame minigame;

    private static final String TIMES = "hit-how-many-times";

    public ShurikenListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onPlayerRightClick(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();

        if (!(projectile instanceof Snowball shuriken) || !(source instanceof Player player)) return;

        ItemMeta meta = getShurikenMetaFromProjectile(shuriken);
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(ItemFactory.IS_SHURIKEN)) return;

        if (minigame.getEnchantedCatalyst() == null) {
            return;
        }

        if (minigame.isCorrupted(player) && !minigame.getEnchantedCatalyst().isReinforced()) {
            event.setCancelled(true);
            player.sendMessage(Utils.color("<red>Corrupted players cannot use shurikens."));
            return;
        }

        shuriken.setMetadata("shuriken", new FixedMetadataValue(StorySMPPlugin.getInstance(), true));
        shuriken.setGravity(false);
        shuriken.setSilent(true);

        addTrailEffect(shuriken);
    }

    private ItemMeta getShurikenMetaFromProjectile(Projectile projectile) {
        if (!(projectile.getShooter() instanceof Player player)) return null;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return itemInHand.hasItemMeta() ? itemInHand.getItemMeta() : null;
    }

    @EventHandler
    public void onShurikenHit(@NotNull ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball shuriken) || !shuriken.hasMetadata("shuriken")) return;

        Player owner = (Player) shuriken.getShooter();
        if (owner == null) return;

        Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            if (isEnchantedCatalyst(hitBlock)) return;

            Location location = hitBlock.getLocation();

            Snowball returning = location.getWorld().spawn(location, Snowball.class, snowball -> {
                snowball.setShooter(owner);
                snowball.setGravity(false);
                snowball.setSilent(true); // Optional: quieter flight
            });

            returnToShooter(returning, owner);
            return;
        }

        Entity hitEntity = event.getHitEntity();
        if (hitEntity != null) {
            Location location = hitEntity.getLocation();

            Snowball returning = location.getWorld().spawn(location, Snowball.class, snowball -> {
                snowball.setShooter(owner);
                snowball.setGravity(false);
                snowball.setSilent(true); // Optional: quieter flight
            });

            returnToShooter(returning, owner);
            return;
        }
    }

    private boolean isEnchantedCatalyst(Block hitBlock) {
        List<MetadataValue> list = hitBlock.getMetadata("enchanted-catalyst");
        if (list.isEmpty()) return false;

        minigame.getEnchantedCatalyst().hitOnce();
        return true;
    }

    public void returnToShooter(Snowball snowball, Player shooter) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (snowball.isDead() || !shooter.isOnline()) {
                    cancel();
                    return;
                }

                Location from = snowball.getLocation();
                Location to = shooter.getEyeLocation();
                Vector direction = to.toVector().subtract(from.toVector()).normalize().multiply(0.8);

                snowball.setVelocity(direction);

                // If it's close enough to the player, remove it
                if (from.distanceSquared(to) < 3.0) {
                    snowball.remove();
                    cancel();
                }
            }
        }.runTaskTimer(StorySMPPlugin.getInstance(), 0L, 1L); // Runs every tick
    }


    private void addTrailEffect(Snowball snowball) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!snowball.isValid() || snowball.isDead()) {
                    cancel();
                    return;
                }

                // Spawn particles along the snowball's trajectory
                snowball.getWorld().spawnParticle(Particle.CRIT, snowball.getLocation(), 5, 0.1, 0.1, 0.1, 0.02);
            }
        }.runTaskTimer(StorySMPPlugin.getInstance(), 0L, 1L); // Runs every tick
    }
}
