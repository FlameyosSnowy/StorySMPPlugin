package me.flame.storysmp.listener;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PoseidentsTridentListener implements Listener {
    private final Set<UUID> tridentCooldown = new HashSet<>();

    // Listen to right-click while crouching
    @EventHandler
    public void onPlayerInteract(@NotNull ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Trident trident)) return;

        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Player player)) return;
        if (!holdingCustomTrident(player)) return;

        boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().getBoolean("poseidons-trident.enabled");
        if (!enabled) {
            event.setCancelled(true);
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        if (!player.isSneaking()) {
            event.setCancelled(true);
            dashWithPoseidonsTrident(player);
            return;
        }

        if (tridentCooldown.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Throw is on cooldown!").color(NamedTextColor.RED));
            return;
        }

        // Launch a custom projectile
        trident.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
        trident.setDamage(6.0);
        trident.setLoyaltyLevel(3);

        // Add cooldown
        tridentCooldown.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> tridentCooldown.remove(player.getUniqueId()), 20 * 30L);
    }

    private static void dashWithPoseidonsTrident(Player player) {
        Vector dashDirection = player.getLocation().getDirection().normalize().multiply(4);
        player.setVelocity(dashDirection);

        // Delay wall break slightly to give velocity time to apply (2 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                Location eyeLoc = player.getEyeLocation();
                Vector dir = eyeLoc.getDirection().normalize();

                // Try to find the wall directly ahead
                RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLoc, dir, 3);
                if (result == null || result.getHitBlock() == null || result.getHitBlockFace() == null) return;

                Block centerBlock = result.getHitBlock();
                BlockFace face = result.getHitBlockFace();
                World world = centerBlock.getWorld();

                // Get 2 perpendicular directions to the face
                BlockFace[] perpendiculars = getPerpendicularFaces(face);

                boolean broke = false;

                // Break 3x3 face centered on the impact point
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        Block relative = centerBlock.getRelative(perpendiculars[0], x).getRelative(perpendiculars[1], y);
                        if (!relative.getType().isAir() && relative.getType().isSolid()) {
                            relative.breakNaturally();
                            broke = true;
                        }
                    }
                }

                if (broke) {
                    world.playSound(centerBlock.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
                }
            }
        }.runTaskLater(StorySMPPlugin.getInstance(), 2); // Replace with your plugin instance
    }

    private static BlockFace[] getPerpendicularFaces(BlockFace face) {
        return switch (face) {
            case NORTH, SOUTH -> new BlockFace[]{BlockFace.EAST, BlockFace.UP};
            case EAST, WEST -> new BlockFace[]{BlockFace.NORTH, BlockFace.UP};
            case UP, DOWN -> new BlockFace[]{BlockFace.EAST, BlockFace.NORTH};
            default -> new BlockFace[]{BlockFace.EAST, BlockFace.UP}; // Fallback
        };
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player)) return;

        if (event.getHitEntity() != null) {
            event.getHitEntity().getWorld().strikeLightning(event.getHitEntity().getLocation());
        }
        if (event.getHitBlock() != null) {
            Location loc = event.getHitBlock().getLocation();
            loc.getWorld().createExplosion(loc, 3.5f, false, true);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        }
    }

    public boolean holdingCustomTrident(@NotNull Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.getType() == Material.TRIDENT && item.getItemMeta().getPersistentDataContainer().has(ItemFactory.IS_POSEIDONS_TRIDENT, PersistentDataType.BOOLEAN);
    }
}
