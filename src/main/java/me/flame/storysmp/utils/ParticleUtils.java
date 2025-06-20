package me.flame.storysmp.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class ParticleUtils {
    public static void drawCircleAroundBlock(@NotNull Block block, double radius, int points, Particle particle) {
        Location center = block.getLocation().add(0.5, 1, 0.5); // center above block

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
        }
    }

}
