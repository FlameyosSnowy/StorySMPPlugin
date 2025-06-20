package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.quest.PlayerQuestObjective;
import me.flame.storysmp.quest.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class CorruptionEffectListener implements Listener {
    private final Minigame minigame;

    public CorruptionEffectListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onPlayerDrink(@NotNull PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player drank milk or honey
        Material type = item.getType();

        PlayerQuestObjective objective = minigame.getQuestProgressManager().getQuestObjective(player);
        if (isEnchantedGoldenApple(objective, type, player)) return;

        if (type == Material.MILK_BUCKET || type == Material.HONEY_BOTTLE) {
            Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
                // Re-apply your custom effect
                if (minigame.isCorrupted(player)) {
                    resetCorruptionEffects(player);
                }
            }, 2L); // Delay slightly to re-apply after milk clears effects
        }

        if (type == Material.GOLDEN_APPLE) {
            ItemMeta meta = item.getItemMeta();
            if (!meta.getPersistentDataContainer().has(ItemFactory.IS_COPPER_APPLE)) return;
            minigame.removeCorruption(player);

            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);
        }
    }

    private void resetCorruptionEffects(Player player) {
        int stage = minigame.getCorruptionNerfLevel(player);

        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;

        switch (stage) {
            case 0 -> {
                healthAttribute.setBaseValue(40);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2));
            }
            case 1 -> {
                healthAttribute.setBaseValue(30);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));
            }
            case 2 -> {
                healthAttribute.setBaseValue(20);
            }
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

    private boolean isEnchantedGoldenApple(PlayerQuestObjective objective, Material type, Player player) {
        if (objective == null || objective.getType() != QuestType.EAT_ENCHANTED_GOLDEN_APPLES) return false;
        if (type != Material.ENCHANTED_GOLDEN_APPLE) return false;
        minigame.getQuestProgressManager().completeQuestTask(player);
        return true;
    }

}
