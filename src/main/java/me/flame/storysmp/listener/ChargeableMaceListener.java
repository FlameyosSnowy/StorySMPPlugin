package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ChargeableMaceListener implements Listener {
    private final Minigame minigame;

    public ChargeableMaceListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack itemClicked = event.getItem();
        if (itemClicked == null) return;

        ItemMeta meta = itemClicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(ItemFactory.IS_CHARGEABLE_MACE)) return;

        Boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().get("chargeable-mace.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        int charges = data.getOrDefault(ItemFactory.WIND_CHARGES, PersistentDataType.INTEGER, 0);
        if (charges == 0) {
            player.sendMessage(Utils.color("<red>You have no wind charges!"));
            return;
        }

        event.setCancelled(true);


    }

    @EventHandler
    public void onWindChargeApply(@NotNull InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        ItemStack cursor = event.getCursor();
        ItemStack target = event.getCurrentItem();

        // Basic null and meta checks
        if (!cursor.hasItemMeta() || !target.hasItemMeta()) return;

        ItemMeta cursorMeta = cursor.getItemMeta();
        ItemMeta targetMeta = target.getItemMeta();

        PersistentDataContainer targetPDC = targetMeta.getPersistentDataContainer();

        // Check if cursor is a wind charge item and target is a mace
        if (cursor.getType() != Material.WIND_CHARGE) return;
        if (!targetPDC.has(ItemFactory.IS_CHARGEABLE_MACE)) return;

        event.setCancelled(true); // Prevent default behavior

        int targetCharges = targetPDC.getOrDefault(ItemFactory.WIND_CHARGES, PersistentDataType.INTEGER, 0);

        int maxCharges = 128;
        int space = maxCharges - targetCharges;

        if (space <= 0) {
            event.getWhoClicked().sendMessage(Utils.color("<red>Your Wind Mace is full!"));
            return;
        }

        int toTransfer = Math.min(space, cursor.getAmount());
        int remaining = cursor.getAmount() - toTransfer;

        // Set new target charge
        targetPDC.set(ItemFactory.WIND_CHARGES, PersistentDataType.INTEGER, targetCharges + toTransfer);
        target.setItemMeta(targetMeta);

        // Update cursor or clear it
        if (remaining > 0) {
            cursor.setAmount(remaining);
            cursor.setItemMeta(cursorMeta);
        } else {
            event.setCursor(null); // All charges used
        }

        event.getWhoClicked().sendMessage(Utils.color("<gray>Transferred <aqua>" + toTransfer + " <gray>wind charge(s) to your mace."));
    }
}
