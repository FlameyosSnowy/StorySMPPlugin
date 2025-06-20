package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulListener implements Listener {
    private final Minigame minigame;

    private final Map<UUID, Instant> cooldowns = new HashMap<>();

    public SoulListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onRightClick(@NotNull PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        if (!persistentDataContainer.has(ItemFactory.IS_SOUL)) return;

        Player player = event.getPlayer();

        event.setCancelled(true);
        boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().getBoolean("soul.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        Instant cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null && checkCooldown(cooldown, player)) return;

        Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("soul.cooldown");
        cooldowns.put(player.getUniqueId(), Instant.now().plus(Duration.ofSeconds(totalCooldown)));

        Utils.dash(player, 10.0);
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) { // essentially true
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("soul.on-cooldown");
            Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("soul.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()))
                    .replace("%total%", totalCooldown.toString())));
            return true;
        }
        return false;
    }
}
