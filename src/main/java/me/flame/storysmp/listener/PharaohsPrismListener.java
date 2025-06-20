package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PharaohsPrismListener implements Listener {
    private final NamespacedKey DAMAGE_KEY = new NamespacedKey(StorySMPPlugin.getInstance(), "pharaohs_prism");

    private final Minigame minigame;

    public PharaohsPrismListener(Minigame minigame) {
        this.minigame = minigame;
    }

    // Right-click a player to start absorbing their damage
    @EventHandler
    public void onRightClickPlayer(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(ItemFactory.IS_PHARAOHS_PRISM, PersistentDataType.DOUBLE)) return;

        Boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().get("chargeable-mace.enabled");
        if (!enabled) {
            event.setCancelled(true);
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        if (minigame.isDiffused(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
            return;
        }

        minigame.setTarget(player.getUniqueId(), target.getUniqueId());
        String rightClick = StorySMPPlugin.getInstance().getMessagesConfig().get("pharaohs-prism.on-right-click");
        player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get(rightClick
                .replace("%player%", MiniMessage.miniMessage().serialize(target.displayName())))));
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        UUID victimId = victim.getUniqueId();
        UUID attackerId = attacker.getUniqueId();

        if (minigame.getTarget(victimId) != null && minigame.getTarget(victimId).equals(attackerId)) {
            if (minigame.isDiffused(attacker.getUniqueId())) {
                attacker.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
                return;
            }

            Boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().get("pharaohs-prism.enabled");
            if (!enabled) {
                victim.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
                return;
            }

            minigame.addAbsorbedDamage(victimId, event.getDamage());
            minigame.incrementHitCount(victimId);
            victim.sendMessage("Absorbed " + event.getDamage() + " damage! Total: " + minigame.getAbsorbedDamage(victimId));

            if (minigame.getHitCount(victimId) >= 2) {
                minigame.removeTarget(victimId);
                minigame.resetHitCount(victimId);
                victim.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("pharaohs-prism.on-absorb")));
            }
        }
    }

    @EventHandler
    public void onAttackWithPrism(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (item.getType() != Material.NETHER_STAR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(DAMAGE_KEY, PersistentDataType.DOUBLE)) return;

        if (minigame.isDiffused(attacker.getUniqueId())) {
            attacker.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
            return;
        }

        double storedDamage = minigame.getAbsorbedDamage(attacker.getUniqueId());
        if (storedDamage > 0) {
            event.setDamage(event.getDamage() + storedDamage);
            minigame.resetAbsorbedDamage(attacker.getUniqueId());
            String onHit = StorySMPPlugin.getInstance().getMessagesConfig().get("pharaohs-prism.on-hit");
            attacker.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get(onHit
                    .replace("%player%", MiniMessage.miniMessage().serialize(target.displayName()))
                    .replace("%damage%", String.valueOf(storedDamage)))));
        }
    }
}
