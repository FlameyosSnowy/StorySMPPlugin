package me.flame.storysmp.commands;

import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({ "gci", "givecustomitem" })
@Description("Give a custom item.")
@CommandPermission(value = "storysmpplugin.givecustomitem", defaultAccess = PermissionDefault.OP)
public class GiveCustomItemCommand {
    @CommandPlaceholder
    public void onDefaultCommand(@NotNull Player player, @SuggestWith(CustomItemSuggestionProvider.class) @NotNull String name, @Default(value = "1") Integer amount) {
        try {
            CustomItem item = CustomItem.valueOf(name.toUpperCase().replace(' ', '_'));

            ItemStack itemStack = item.getCustomItemStack().clone();
            itemStack.setAmount(amount);

            player.getInventory().addItem(itemStack);

            player.sendMessage(Utils.color("<green>" + name + " has been added to your inventory!"));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Utils.color("<red>That is not a valid item."));
        }
    }
}
