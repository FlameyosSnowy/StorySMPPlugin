package me.flame.storysmp.commands;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Command({ "ability1" })
@Description("The soul ability.")
public class AbilityOneCommand {
    private static final DamageSource DAAMGE_SOURCE = DamageSource.builder(DamageType.GENERIC).build();

    private final Map<UUID, Instant> cooldowns = new HashMap<>();

    @CommandPlaceholder
    public void onDefaultCommand(@NotNull Player player) {
        if (checkCooldown(cooldowns.get(player.getUniqueId()), player)) return;

        List<Entity> entities = player.getNearbyEntities(20, 20, 20);

        String names = entities.stream()
                .filter(entity -> entity instanceof Damageable)
                .peek((entity) -> {
                    Damageable damageable = (Damageable) entity;
                    damageable.damage(8.0, DAAMGE_SOURCE);
                    Utils.push(entity, entity.getLocation(), 2);
                })
                .map(Entity::getName)
                .collect(Collectors.joining(", "));

        if (names.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You were too far away to push anyone!"));
            return;
        }

        cooldowns.put(player.getUniqueId(), Instant.now().plusSeconds(30));

        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Pushed away everyone within 20 blocks!\n Including: " + names));
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
