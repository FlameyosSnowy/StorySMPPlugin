package me.flame.storysmp.commands;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CustomItemSuggestionProvider implements SuggestionProvider<BukkitCommandActor> {
    private static final List<String> VALUES = Arrays.stream(CustomItem.values()).map(CustomItem::name).toList();

    @Override
    public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<BukkitCommandActor> context) {
        return VALUES;
    }
}
