package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.TrackedLocation;
import me.flame.storysmp.quest.PlayerQuestObjective;
import me.flame.storysmp.quest.QuestType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerQuestListener implements Listener {
    private final Minigame minigame;

    public PlayerQuestListener(Minigame minigame) {
        this.minigame = minigame;
        new MoveBukkitRunnable(minigame).runTaskTimerAsynchronously(StorySMPPlugin.getInstance(), 5, 5);  // Runs every 5 ticks asynchronously
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (killer == null) return;

        PlayerQuestObjective questObjective = minigame.getQuestProgressManager().getQuestObjective(killer);
        if (questObjective != null && questObjective.getType() == QuestType.KILL_ENEMIES) {
            minigame.getQuestProgressManager().completeQuestTask(killer);
        }

        if (minigame.isCorrupted(player)) {
            Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
                minigame.debuffCorrupted(player);
            }, 2L);
        }

        boolean killerCorrupted = minigame.isCorrupted(killer.getUniqueId());
        if (!minigame.isCorrupted(player) && killerCorrupted) {
            minigame.restoreBuffsOfCorrupted(killer);
        }

        if (killerCorrupted) {
            minigame.addCorruption(player);
            killer.getWorld().playSound(Sound.sound(Key.key("enchanting_table"), Sound.Source.BLOCK, 1.0f, 1.0f), player);
        }

        if (minigame.shouldTrackLives(player))
            minigame.addDeath(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();

        if (isEnchantedCatalyst(event, brokenBlock)) return;
        if (brokenBlock.getType() != Material.PURPUR_BLOCK) return;

        PlayerQuestObjective objective = minigame.getQuestProgressManager().getQuestObjective(player);
        if (objective == null || objective.getType() != QuestType.MINE_PURPUR) return;

        minigame.getQuestProgressManager().completeQuestTask(player);
    }

    private static boolean isEnchantedCatalyst(BlockBreakEvent event, @NotNull Block brokenBlock) {
        if (brokenBlock.getType() != Material.SCULK_CATALYST) return false;

        @NotNull List<MetadataValue> container = brokenBlock.getMetadata("enchanted-catalyst");
        if (container.isEmpty()) return false;

        event.setCancelled(true);
        return true;
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (event.isCancelled()) return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        PlayerQuestObjective objective = minigame.getQuestProgressManager().getQuestObjective(player);
        if (objective == null || objective.getType() != QuestType.POP_TOTEMS) return;

        minigame.getQuestProgressManager().completeQuestTask(player);
    }

    public static class MoveBukkitRunnable extends BukkitRunnable {
        private final Minigame minigame;

        public MoveBukkitRunnable(Minigame minigame) {
            this.minigame = minigame;
        }

        @Override
        public void run() {
            this.runSync();
        }

        private void runSync() {
            for (Map.Entry<UUID, TrackedLocation> entry : minigame.getTrackedLocations().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null) return;

                TrackedLocation traveled = entry.getValue();
                Location lastPos = traveled.location();
                int traveledBlocks = traveled.traveled();
                Location currentPos = player.getLocation();

                // Calculate squared distance moved
                double distance = lastPos.distance(currentPos);

                traveled.setTraveled((int) (traveledBlocks + distance));
                traveled.setLocation(currentPos);

                PlayerQuestObjective objective = minigame.getQuestProgressManager().getQuestObjective(player);
                if (objective == null || objective.getType() != QuestType.POP_TOTEMS) return;

                minigame.getQuestProgressManager().completeQuestTask(player, (int) distance);
            }
        }
    }
}