package me.flame.storysmp.commands;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.Utils;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({ "corrupt" })
@Description("Corrupt someone.")
@CommandPermission("storysmpplugin.corrupt")
public class CorruptCommand {
    @CommandPlaceholder
    public void onCommand(@NotNull Player sender, Player target) {
        StorySMPPlugin.getInstance().getMinigame().addCorruption(target);

        sender.sendMessage(Utils.color("<green>You have corrupted " + Utils.color(target.displayName()) + '!'));
    }
}
