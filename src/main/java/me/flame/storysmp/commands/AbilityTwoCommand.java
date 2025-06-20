package me.flame.storysmp.commands;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Command({ "ability2" })
@Description("The soul ability.")
public class AbilityTwoCommand implements Listener {
    public final Map<UUID, Instant> cooldowns = new HashMap<>();
    private final Minigame minigame;

    public AbilityTwoCommand(Minigame minigame) {
        this.minigame = minigame;
    }

    @CommandPlaceholder
    public void onDefaultCommand(@NotNull Player player) {
        if (minigame.listeningForRightClick.contains(player.getUniqueId())) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("paralyze.already-queued-to-paralyze")));
            return;
        }

        Instant cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null && checkCooldown(cooldown, player)) return;

        minigame.listeningForRightClick.add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(StorySMPPlugin.getInstance(), () -> {
            boolean contained = minigame.listeningForRightClick.remove(player.getUniqueId());
            if (contained) {
                player.sendMessage(Utils.color("<red>You didn't paralyze anyone within 3 seconds. Cancelled paralyze."));
            }
        }, 60L);

        player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("paralyze.queued-to-paralyze")));
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) {
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("paralyze.on-cooldown");
            Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().get("paralyze.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()) + 's')
                    .replace("%total%", totalCooldown.toString())));
            return true;
        }
        return false;
    }
}
