package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ParalyzeRightClickListener implements Listener {
    private final Minigame minigame;

    public ParalyzeRightClickListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onRightClick(@NotNull PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (!minigame.listeningForRightClick.contains(player.getUniqueId())) {
            return;
        }

        if (!(entity instanceof Player victim)) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("paralyze.not-a-player")));
            return;
        }

        Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().get("paralyze.cooldown");
        StorySMPPlugin.getInstance().abilityTwoCommand.cooldowns.put(player.getUniqueId(), Instant.now().plus(Duration.ofSeconds(totalCooldown)));

        minigame.paralyze(victim.getUniqueId());
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
    }
}
