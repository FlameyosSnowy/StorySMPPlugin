package me.flame.storysmp.commands;

import me.flame.storysmp.StorySMPPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({ "stopsmp" })
@Description("Stop the SMP.")
@CommandPermission("storysmpplugin.stop")
public class StopCommand {
    @CommandPlaceholder
    public void onDefaultCommand(@NotNull Player player) {
        StorySMPPlugin.getInstance().getMinigame().stop();

        player.sendMessage(Component.text("Stopped this game!").color(NamedTextColor.GREEN));
    }
}
