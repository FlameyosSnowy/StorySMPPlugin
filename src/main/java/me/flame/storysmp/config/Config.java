package me.flame.storysmp.config;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.flame.storysmp.utils.Registry;
import org.bukkit.plugin.java.JavaPlugin;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Config {
    private final Registry registry = new Registry();
    private final YamlDocument config;

    public Config(JavaPlugin plugin) {
        try {
            this.config = YamlDocument.create(
                    new File(plugin.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(plugin.getResource("config.yml")),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).setAutoSave(true).build(),
                    DumperSettings.DEFAULT,
                    LoaderSettings.builder().setCreateFileIfAbsent(true).setDetailedErrors(true).setAutoUpdate(true).build(),
                    GeneralSettings.DEFAULT
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) registry.computeIfAbsent(key, config::get);
    }

    public boolean getBoolean(String key) {
        return registry.computeIfAbsent(key, config::getBoolean);
    }

    public Long getLong(String key) {
        return registry.computeIfAbsent(key, config::getLong);
    }

    public Integer getInt(String key) {
        return registry.computeIfAbsent(key, config::getInt);
    }

    public Double getDouble(String key) {
        return registry.computeIfAbsent(key, config::getDouble);
    }

    public void reload() {
        try {
            config.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
