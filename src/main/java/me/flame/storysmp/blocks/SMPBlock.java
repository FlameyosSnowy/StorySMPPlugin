package me.flame.storysmp.blocks;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public abstract class SMPBlock {
    protected BukkitTask task;
    protected final Block block;
    protected final Material material;
    protected final ItemStack itemStack;
    protected final Location location;
    protected final Minigame minigame;

    public SMPBlock(Block block, Material material, ItemStack itemStack, Location location, Minigame minigame) {
        this.block = block;
        this.material = material;
        this.itemStack = itemStack;
        this.location = location;
        this.minigame = minigame;
    }

    public void placeBlock() {
        this.task = Bukkit.getScheduler().runTaskTimer(StorySMPPlugin.getInstance(), this::tick, duration(), duration());
        block.setType(itemStack.getType());
    }

    public void breakBlock() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
            this.task = null;
        }

        block.setType(Material.AIR);
    }

    public abstract void tick();

    public abstract long duration();
}
