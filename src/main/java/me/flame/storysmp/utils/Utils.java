package me.flame.storysmp.utils;

import me.flame.storysmp.StorySMPPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static @NotNull Component color(String message) {
        return miniMessage.deserialize(message);
    }

    public static @NotNull String color(Component message) {
        return miniMessage.serialize(message);
    }

    /**
     * Adds wind charges to a number, handling overflow at 128, and returns the remainder.
     *
     * @param currentValue The current value.
     * @param windChargesToAdd The wind charges to add.
     * @return An array of two integers: [updatedValue, remainder]. Returns null if the addition cannot be performed.
     */
    public static int[] addNumberWithRemainder(int currentValue, int windChargesToAdd, int max) {
        if (currentValue < 0 || windChargesToAdd < 0) return null;

        int newValue = currentValue + windChargesToAdd;

        if (newValue <= max) return new int[] { newValue, 0 }; // No remainder
        int maxToAdd = max - currentValue;

        if (maxToAdd <= 0) return null; // Indicate that the addition failed.

        int remainder = windChargesToAdd - maxToAdd;
        return new int[] { max, remainder };
    }

    public static Location getNearestSolidBlock(Location baseXZ, int originY, int yRadius) {
        for (int i = 0; i <= yRadius; i++) {
            Location up = baseXZ.clone();
            up.setY(originY + i);

            Location down = baseXZ.clone();
            down.setY(originY - i);

            if (isSolidBlock(up.getBlock())) return up;
            if (isSolidBlock(down.getBlock())) return down;
        }
        return null;
    }

    public static Location getFixedLocation(final Player player, double distance, final double height) {
        Location pLoc = player.getLocation().clone();
        pLoc.setX(pLoc.getX() + distance);
        pLoc.setY(pLoc.getY() + height);
        return pLoc;
    }

    public static boolean isSolidBlock(Block block) {
        return block.getType().isSolid() && block.getType().isBlock();
    }

    public static void dash(@NotNull Player player, double distance) {
        Vector direction = player.getLocation().getDirection().normalize();
        Vector dashVelocity = direction.multiply(distance);

        player.setVelocity(dashVelocity);
        player.setFallDistance(0);
    }

    public static void push(Entity player, Location pushAwayFrom, int punchLevel) {
        if (punchLevel <= 0) return;

        Location playerLocation = player.getLocation();

        Vector direction = playerLocation.toVector().subtract(pushAwayFrom.toVector());
        if (direction.length() == 0) direction.add(new Vector(1, 1, 1));

        // Each punch level increases strength by 0.5 (vanilla behavior approximation)
        double horizontalStrength = 0.5 * punchLevel;
        double verticalStrength = 0.1 + (0.025 * punchLevel); // Add slight vertical lift

        direction = direction.multiply(horizontalStrength);
        direction.setY(verticalStrength);

        player.setVelocity(direction);
    }


    public static Map<String, List<Block>> getObsidianSphereWithLightInterior(Location center, int radius) {
        Map<String, List<Block>> result = new HashMap<>();
        List<Block> shell = new ArrayList<>();
        List<Block> interior = new ArrayList<>();

        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    Block block = world.getBlockAt(cx + x, cy + y, cz + z);

                    if (distanceSquared <= radius * radius) {
                        if (distanceSquared >= (radius - 1) * (radius - 1)) {
                            shell.add(block);
                        } else {
                            interior.add(block);
                        }
                    }
                }
            }
        }

        result.put("shell", shell);
        result.put("interior", interior);
        return result;
    }

    public static void summonObsidianDome(Location center) {
        Map<String, List<Block>> sphereMap = getObsidianSphereWithLightInterior(center, 10);
        List<Block> shellBlocks = sphereMap.get("shell");
        List<Block> interiorBlocks = sphereMap.get("interior");

        Map<Integer, List<Block>> shellLayers = new TreeMap<>();
        Map<Integer, List<Block>> interiorLayers = new TreeMap<>();

        for (Block b : shellBlocks) {
            int y = b.getY() - center.getBlockY();
            shellLayers.computeIfAbsent(y, k -> new ArrayList<>()).add(b);
        }

        for (Block b : interiorBlocks) {
            int y = b.getY() - center.getBlockY();
            interiorLayers.computeIfAbsent(y, k -> new ArrayList<>()).add(b);
        }

        List<Block> placedShell = new ArrayList<>();
        List<Block> placedInterior = new ArrayList<>();

        new BukkitRunnable() {
            final Iterator<Integer> it = shellLayers.keySet().iterator();

            public void run() {
                if (!it.hasNext()) {
                    cancel();
                    scheduleUnbuild(placedShell, placedInterior);
                    return;
                }

                int y = it.next();
                List<Block> shellLayer = shellLayers.getOrDefault(y, List.of());
                List<Block> interiorLayer = interiorLayers.getOrDefault(y, List.of());

                for (Block block : shellLayer) {
                    block.setType(Material.OBSIDIAN);
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.0f);
                    placedShell.add(block);
                }

                for (Block block : interiorLayer) {
                    block.setType(Material.LIGHT);
                    placedInterior.add(block);
                }
            }
        }.runTaskTimer(StorySMPPlugin.getInstance(), 0L, 1L);
    }

    public static void scheduleUnbuild(List<Block> shellBlocks, List<Block> interiorBlocks) {
        new BukkitRunnable() {
            public void run() {
                Map<Integer, List<Block>> shellLayers = new TreeMap<>(Comparator.reverseOrder());
                Map<Integer, List<Block>> interiorLayers = new TreeMap<>(Comparator.reverseOrder());

                for (Block b : shellBlocks) {
                    shellLayers.computeIfAbsent(b.getY(), k -> new ArrayList<>()).add(b);
                }

                for (Block b : interiorBlocks) {
                    interiorLayers.computeIfAbsent(b.getY(), k -> new ArrayList<>()).add(b);
                }

                new BukkitRunnable() {
                    final Iterator<Integer> it = shellLayers.keySet().iterator();

                    public void run() {
                        if (!it.hasNext()) {
                            cancel();
                            return;
                        }

                        int y = it.next();
                        List<Block> shell = shellLayers.getOrDefault(y, List.of());
                        List<Block> interior = interiorLayers.getOrDefault(y, List.of());

                        for (Block block : shell) {
                            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.0f);
                            block.setType(Material.AIR);
                        }
                        for (Block block : interior) block.setType(Material.AIR);
                    }
                }.runTaskTimer(StorySMPPlugin.getInstance(), 0L, 3L);
            }
        }.runTaskLater(StorySMPPlugin.getInstance(), 20 * 20L); // wait 20s
    }

}
