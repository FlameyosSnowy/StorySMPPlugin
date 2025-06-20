package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiffuseListener implements Listener {
    private final Minigame minigame;

    public DiffuseListener(Minigame minigame) {
        this.minigame = minigame;
    }

    private final Map<UUID, Instant> cooldowns = new HashMap<>();

    @EventHandler
    public void onDiffuseUse(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Entity rightClicked = event.getRightClicked();
        Player player = event.getPlayer();
        if (!(rightClicked instanceof Player victim)) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        if (!persistentDataContainer.has(ItemFactory.IS_DIFFUSER)) return;

        Boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().get("diffuser.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        if (minigame.isDiffused(victim.getUniqueId())) {
            String victimAlreadyDiffused = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-already-diffused");
            player.sendMessage(Utils.color(victimAlreadyDiffused.replace("%victim%", MiniMessage.miniMessage().serialize(victim.displayName()))));
        }

        Instant cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null && checkCooldown(cooldown, player)) return;

        minigame.addDiffused(victim.getUniqueId());

        String message = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.attacker-diffused");
        String victimMessage = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-got-diffused");
        victim.sendMessage(Utils.color(victimMessage.replace("%attacker%", MiniMessage.miniMessage().serialize(player.displayName()))));
        player.sendMessage(Utils.color(message.replace("%victim%", MiniMessage.miniMessage().serialize(victim.displayName()))));

        Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
            minigame.removeDiffused(victim.getUniqueId());

            String unDiffusedMessage = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-no-longer-diffused-attacker");
            String victimUnDiffusedMessage = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-no-longer-diffused-victim");
            victim.sendMessage(Utils.color(unDiffusedMessage));
            player.sendMessage(Utils.color(victimUnDiffusedMessage));
        }, 20 * 90);

        cooldowns.put(player.getUniqueId(), Instant.now().plus(StorySMPPlugin.getInstance().getPrimaryConfig().get("diffuser.cooldown")));
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) { // essentially true
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.on-cooldown");
            Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().get("diffuser.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()))
                    .replace("%total%", totalCooldown.toString())));
            return true;
        }
        return false;
    }
}
