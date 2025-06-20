package me.flame.storysmp.commands;

import me.flame.storysmp.utils.Utils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Description;

import java.util.concurrent.TimeUnit;

@Command({ "ability4" })
@Description("The wind orb ability.")
public class AbilityFourCommand {
    @CommandPlaceholder
    @Cooldown(value = 30, unit = TimeUnit.SECONDS)
    public void onDefaultCommand(Player player) {
        Utils.summonObsidianDome(player.getLocation());
    }
}
