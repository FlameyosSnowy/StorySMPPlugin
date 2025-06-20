package me.flame.storysmp.listener;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;

import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.*;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.UUID;

public class DragonsChestplateListener implements Listener {


    private final Object2IntOpenHashMap<UUID> hitCounter = new Object2IntOpenHashMap<>();

    private final Object2LongOpenHashMap<UUID> breathCooldowns = new Object2LongOpenHashMap<>();

    private static final SecureRandom random = new SecureRandom();

    private boolean isNotWearingDragonsChestplate(@NotNull Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null) return true;

        ItemMeta meta = chestplate.getItemMeta();
        return meta == null || !meta.getPersistentDataContainer().has(ItemFactory.IS_DRAGON_CHESTPLATE, PersistentDataType.BOOLEAN);
    }

    @EventHandler
    public void onPlayerCrouch(@NotNull PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || !isWearingDragonsChestplate(player)) return;

        Boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().get("dragons-chestplate.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (breathCooldowns.containsKey(uuid) && currentTime < breathCooldowns.getLong(uuid)) {
            long secondsLeft = (breathCooldowns.getLong(uuid) - currentTime) / 1000;
            player.sendMessage(Component.text("Dragon’s Breath is on cooldown for " + secondsLeft + " more seconds.").color(NamedTextColor.RED));
            return;
        }

        Integer cooldown = StorySMPPlugin.getInstance().getPrimaryConfig().get("dragons-breath.cooldown");
        breathCooldowns.put(uuid, currentTime + (cooldown * 1000)); // 120 seconds cooldown

        Location loc = player.getLocation();
        World world = player.getWorld();

        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1); // Fireball SFX

        AreaEffectCloud cloud = (AreaEffectCloud) world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(8f);
        cloud.setDuration(200); // 10 seconds (in ticks)
        cloud.setReapplicationDelay(20);
        cloud.setWaitTime(0);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setColor(Color.fromRGB(128, 0, 128)); // Purplish
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 40, 1), true);
        cloud.setSource(player);
        cloud.getPersistentDataContainer().set(ItemFactory.IS_DRAGON_CHESTPLATE, PersistentDataType.BOOLEAN, true);
    }

    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (cloud.getPersistentDataContainer().has(ItemFactory.IS_DRAGON_CHESTPLATE, PersistentDataType.BOOLEAN)) {
            event.getAffectedEntities().remove(cloud.getSource());
        }
    }

    @EventHandler
    public void onEatChorusFruit(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (isNotWearingDragonsChestplate(player)) return;

        if (event.getItem().getType() == Material.CHORUS_FRUIT && random.nextInt(0, 100) + 1  <= 10) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 4)); // Regen 5
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1)); // Res 2
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isNotWearingDragonsChestplate(player)) return;

        UUID id = player.getUniqueId();
        hitCounter.put(id, hitCounter.getOrDefault(id, 0) + 1);

        if (hitCounter.getInt(id) >= 250) {
            hitCounter.put(id, 0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 300, 7)); // Haste 8
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2)); // Strength 3
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1, 1);
        }
    }

    private boolean isWearingDragonsChestplate(@NotNull Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null) return false;

        ItemMeta meta = chestplate.getItemMeta();

        return meta != null && meta.getPersistentDataContainer().has(ItemFactory.IS_DRAGON_CHESTPLATE, PersistentDataType.BOOLEAN);
    }
}