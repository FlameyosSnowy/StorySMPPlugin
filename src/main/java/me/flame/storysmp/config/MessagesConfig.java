package me.flame.storysmp.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.flame.storysmp.utils.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MessagesConfig {
    private final Registry registry = new Registry();
    private final YamlDocument config;

    public MessagesConfig(JavaPlugin plugin) {
        try {
            this.config = YamlDocument.create(
                    new File(plugin.getDataFolder(), "messages.yml"),
                    Objects.requireNonNull(plugin.getResource("messages.yml")),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).setAutoSave(true).build(),
                    DumperSettings.DEFAULT,
                    LoaderSettings.builder().setCreateFileIfAbsent(true).setDetailedErrors(true).setAutoUpdate(true).build(),
                    GeneralSettings.DEFAULT
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String key) {
        return registry.computeIfAbsent(key, config::getString);
    }

    public void reload() {
        try {
            config.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
