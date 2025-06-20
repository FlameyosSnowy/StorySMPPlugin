package me.flame.storysmp.commands;

import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Description;

import java.util.List;
import java.util.Random;

@Command({ "ability3" })
@Description("The omen staff ability.")
public class AbilityThreeCommand {
    private static final Random random = new Random();

    @CommandPlaceholder
    public void onDefaultCommand(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        List<Entity> entities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : entities) {
            Utils.push(entity, playerLocation, 3);
        }

        AreaEffectCloud cloud = (AreaEffectCloud) world.spawnEntity(playerLocation, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(5f);
        cloud.setDuration(100); // 10 seconds (in ticks)
        cloud.setReapplicationDelay(20);
        cloud.setWaitTime(0);
        cloud.setParticle(Particle.CRIT);
        cloud.setColor(Color.fromRGB(66, 239, 245));
        cloud.setSource(player);

        Location inFront = Utils.getFixedLocation(player, 8, 2);

        for (int i = 0; i < 20; i++) {
            int randomInt = random.nextInt(0, 3);
            if (randomInt == 0) spawn(world, Zombie.class, inFront, player);
            else if (randomInt == 1) spawn(world, Skeleton.class, inFront, player);
            else if (randomInt == 2) spawn(world, Spider.class, inFront, player);
        }
    }

    private static <T extends Entity> void spawn(@NotNull World world, Class<T> clazz, Location inFront, Player player) {
        world.spawn(inFront, clazz, (entity) -> {
            entity.getPersistentDataContainer().set(ItemFactory.IS_OMEN_STAFF_MOB, PersistentDataType.BOOLEAN, true);
            entity.getPersistentDataContainer().set(ItemFactory.OWNER, ItemFactory.UUID_DATA_TYPE, player.getUniqueId());
        });
    }
}
