package me.flame.storysmp;

import io.papermc.paper.ban.BanListType;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import me.flame.storysmp.blocks.EnchantedCatalyst;
import me.flame.storysmp.commands.CustomItem;
import me.flame.storysmp.listener.*;
import me.flame.storysmp.quest.QuestProgressManager;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Minigame {
    private final Map<UUID, UUID> targetMap = new HashMap<>(); // Tracks targeted opponent per player
    private final Object2DoubleMap<UUID> absorbedDamage =
            new Object2DoubleOpenHashMap<>(); // Tracks absorbed damage
    private final Map<UUID, Integer> hitCount = new HashMap<>(); // Tracks number of absorbed hits

    private final Set<UUID> disabledByDiffusers = new HashSet<>();
    private final Set<UUID> corruptedUsers = new HashSet<>();
    private final Set<UUID> paralyzed = new HashSet<>();
    private final Map<UUID, TrackedLocation> trackedLocations = new ConcurrentHashMap<>();
    private final Set<UUID> bannedFromCorruption = new HashSet<>();
    private final Set<UUID> participants = new HashSet<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Set<UUID> trackLives = new HashSet<>();
    public final Set<UUID> listeningForRightClick = new HashSet<>();

    private final JavaPlugin plugin;

    private final QuestProgressManager progressManager = new QuestProgressManager(objective -> {
        Player player = objective.getPlayer();
        PlayerInventory playerInventory = player.getInventory();

        if (playerInventory.firstEmpty() == -1) {
            player.sendMessage(Component.text("A copper apple will be dropped to you in three seconds.").color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("Please clear one item from your inventory or else it will be dropped on the ground at your location.").color(NamedTextColor.GREEN));

            this.queueCopperAppleGift(player, playerInventory);
        }
    });

    private EnchantedCatalyst enchantedCatalyst;

    private void queueCopperAppleGift(Player player, PlayerInventory playerInventory) {
        Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
            ItemStack copperApple = CustomItem.COPPER_APPLE.getCustomItemStack();

            int emptySlot = playerInventory.firstEmpty();
            if (emptySlot == -1) {
                Location location = player.getLocation();
                location.getWorld().dropItem(location, copperApple);
                player.sendMessage(Component.text("A copper apple has been dropped at X = " + location.x() + ", Y = " + location.y() + ", Z = " + location.z()));
                return;
            }

            playerInventory.setItem(emptySlot, copperApple);
            player.sendMessage(Component.text("A copper apple has been added to your inventory at slot: " + emptySlot).color(NamedTextColor.GREEN));
        }, 60L);
    }

    public void start() {
        // Manually register each listener
        Bukkit.getPluginManager().registerEvents(new ChargeableMaceListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new CorruptionEffectListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new DiffuseListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new GroundSlammerListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new OmenStaffListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PharaohsPrismListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuestListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ShurikenListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new WindOrbListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new ParalyzedListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new SoulListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new DragonsChestplateListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PoseidentsTridentListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ParalyzeRightClickListener(this), plugin);

        // Add all online players to participants
        for (Player player : Bukkit.getOnlinePlayers()) {
            participants.add(player.getUniqueId());
        }
    }


    public Minigame(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void stop() {
        if (enchantedCatalyst != null) enchantedCatalyst.breakBlock();

        HandlerList.unregisterAll(plugin);

        Iterator<UUID> iterator = participants.iterator();
        while (iterator.hasNext()) {
            UUID player = iterator.next();
            stopTracking(player);
            removeCorruption(player);
            iterator.remove();
        }

        // Pardon banned players and clear participants
        pardonBannedPlayers();
    }


    public UUID getTarget(UUID playerId) {
        return targetMap.get(playerId);
    }

    public void setTarget(UUID playerId, UUID targetId) {
        targetMap.put(playerId, targetId);
    }

    public void removeTarget(UUID playerId) {
        targetMap.remove(playerId);
    }

    public void addDeath(Player player) {
        int deathCount = deaths.getOrDefault(player.getUniqueId(), 0) + 1;

        if (deathCount == 5) {
            player.ban("You died five times.", Instant.now().plus(365, TimeUnit.DAYS.toChronoUnit()), null, true);
            return;
        }

        deaths.put(player.getUniqueId(), deaths.getOrDefault(player.getUniqueId(), 0));
    }

    public double getAbsorbedDamage(UUID playerId) {
        return absorbedDamage.getOrDefault(playerId, 0.0);
    }

    public void addAbsorbedDamage(UUID playerId, double damage) {
        absorbedDamage.put(playerId, getAbsorbedDamage(playerId) + damage);
    }

    public void resetAbsorbedDamage(UUID playerId) {
        absorbedDamage.removeDouble(playerId);
    }

    public int getHitCount(UUID playerId) {
        return hitCount.getOrDefault(playerId, 0);
    }
    public void incrementHitCount(UUID playerId) {
        hitCount.put(playerId, getHitCount(playerId) + 1);
    }

    public void resetHitCount(UUID playerId) {
        hitCount.remove(playerId);
    }

    public void paralyze(UUID playerId) {
        paralyzed.add(playerId);
        disabledByDiffusers.add(playerId);

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> unparalyze(playerId),
                (long) StorySMPPlugin.getInstance().getPrimaryConfig().get("paralyze.duration") * 20
        );
    }

    public void unparalyze(UUID playerId) {
        paralyzed.remove(playerId);
        disabledByDiffusers.remove(playerId);
    }

    public void addDiffused(UUID player) {
        this.disabledByDiffusers.add(player);
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> disabledByDiffusers.remove(player),
                (long) StorySMPPlugin.getInstance().getPrimaryConfig().get("diffuser.duration") * 20);
    }

    public boolean isDiffused(@NotNull UUID uniqueId) {
        return disabledByDiffusers.contains(uniqueId);
    }

    public boolean isCorrupted(Player player) {
        return corruptedUsers.contains(player.getUniqueId());
    }

    public boolean isCorrupted(UUID player) {
        return corruptedUsers.contains(player);
    }

    private final Map<UUID, Integer> corruptionNerfLevel = new HashMap<>();
    private final Map<UUID, BukkitTask> corruptionTasks = new HashMap<>();

    public void addCorruption(Player player) {
        UUID uuid = player.getUniqueId();

        player.sendMessage(Utils.color("<red>You have been <bold>corrupted<reset><red>."));

        corruptedUsers.add(uuid);
        corruptionNerfLevel.remove(uuid);

        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(40);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));

        startCorruptionTask(player);

        Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
            if (!corruptedUsers.contains(uuid)) return;

            bannedFromCorruption.add(uuid);
            removeCorruption(player);
            player.ban("You did not kill anyone within your corruption!", Instant.now().plus(Duration.ofDays(365)), null);
        }, TimeUnit.HOURS.toSeconds(4) * 20);

        if (participants.size() == corruptedUsers.size()) {
            // everyone is corrupted

            if (enchantedCatalyst == null) return;

            onCorruptEveryone();
        }
    }

    public void onCorruptEveryone() {
        enchantedCatalyst.reinforce();

        trackLives.addAll(corruptedUsers);
    }

    public int getDeaths(Player player) {
        return deaths.getOrDefault(player.getUniqueId(), 0);
    }

    public void stopTracking(Player player) {
        deaths.remove(player.getUniqueId());
        trackLives.remove(player.getUniqueId());
    }

    public void stopTracking(UUID player) {
        deaths.remove(player);
        trackLives.remove(player);
    }

    public void removeCorruption(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            removeCorruption(player); // does more actions
            return;
        }

        corruptedUsers.remove(uuid);
        corruptionNerfLevel.remove(uuid);

        BukkitTask task = corruptionTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    public void removeCorruption(OfflinePlayer offlinePlayer) {
        UUID uuid = offlinePlayer.getUniqueId();
        corruptedUsers.remove(uuid);
        corruptionNerfLevel.remove(uuid);

        BukkitTask task = corruptionTasks.remove(uuid);
        if (task != null) task.cancel();

        if (offlinePlayer instanceof Player player) {
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.SLOWNESS);

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);

            stopTracking(player);
        }
    }

    private void startCorruptionTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (corruptionTasks.containsKey(uuid)) {
            corruptionTasks.remove(uuid).cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(StorySMPPlugin.getInstance(), () -> {
            if (!corruptedUsers.contains(uuid)) {
                corruptionTasks.remove(uuid).cancel();
                return;
            }

            debuffCorrupted(player);
        }, TimeUnit.HOURS.toSeconds(1) * 20, TimeUnit.HOURS.toSeconds(1) * 20); // every hour

        corruptionTasks.put(uuid, task);
    }

    public int getCorruptionNerfLevel(Player player) {
        return corruptionNerfLevel.getOrDefault(player.getUniqueId(), 0);
    }

    public void restoreBuffsOfCorrupted(Player player) {
        UUID uuid = player.getUniqueId();
        corruptionNerfLevel.remove(uuid);

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(40);
        }

        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));

        // Restart their debuff timer
        BukkitTask contained = corruptionTasks.remove(uuid);
        if (contained != null) {
            contained.cancel();
            startCorruptionTask(player);
        }
    }

    public void debuffCorrupted(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        int level = corruptionNerfLevel.getOrDefault(uuid, 0);

        if (level >= 4) return;

        level++;
        corruptionNerfLevel.put(uuid, level);

        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;

        switch (level) {
            case 1 -> {
                healthAttribute.setBaseValue(30);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));
            }
            case 2 -> healthAttribute.setBaseValue(20);
            case 3 -> {
                healthAttribute.setBaseValue(16);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 0));
            }
            case 4 -> {
                healthAttribute.setBaseValue(10);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 1));
            }
        }
    }



    public QuestProgressManager getQuestProgressManager() {
        return progressManager;
    }

    public void setTrackMoves(Player player) {
        trackedLocations.put(player.getUniqueId(), new TrackedLocation(0, player.getLocation()));
    }

    public Map<UUID, TrackedLocation> getTrackedLocations() {
        return trackedLocations;
    }

    public void removeDiffused(@NotNull UUID uniqueId) {
        disabledByDiffusers.remove(uniqueId);
    }

    public EnchantedCatalyst getEnchantedCatalyst() {
        return enchantedCatalyst;
    }

    public void setEnchantedCatalyst(EnchantedCatalyst catalyst) {
        this.enchantedCatalyst = catalyst;
    }

    public boolean isParalyzed(@NotNull Player player) {
        return paralyzed.contains(player.getUniqueId());
    }

    public void pardonBannedPlayers() {
        if (bannedFromCorruption.isEmpty()) return;
        for (UUID player : bannedFromCorruption) {
            OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(player);
            Bukkit.getBanList(BanListType.PROFILE).pardon(bannedPlayer.getPlayerProfile());
        }
    }

    public void uncorruptAll() {
        for (UUID corrupted : corruptedUsers) {
            Player player = Bukkit.getPlayer(corrupted);

            if (player == null) return;
            removeCorruption(player);
        }
    }

    public boolean shouldTrackLives(Player player) {
        return trackLives.contains(player.getUniqueId());
    }
}
