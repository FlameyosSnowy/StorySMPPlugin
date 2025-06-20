package me.flame.storysmp.commands;

import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.utils.Utils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({ "storysmpplugin" })
@Description("Reload StorySMPPlugin")
@CommandPermission("storysmpplugin.reload")
public class ReloadCommand {
    @Subcommand("reload")
    public void onReload(@NotNull Player player) {
        StorySMPPlugin.getInstance().getPrimaryConfig().reload();
        StorySMPPlugin.getInstance().getMessagesConfig().reload();

        player.sendMessage(Utils.color("<green>You have reloaded config.yml and messages.yml."));
    }
}
