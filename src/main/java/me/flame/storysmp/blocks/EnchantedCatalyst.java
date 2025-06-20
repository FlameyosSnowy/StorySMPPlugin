package me.flame.storysmp.blocks;

import io.papermc.paper.ban.BanListType;
import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.quest.QuestType;
import me.flame.storysmp.utils.ParticleUtils;
import me.flame.storysmp.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnchantedCatalyst extends SMPBlock {
    private static final SecureRandom random = new SecureRandom();
    private static final long TICKS_IN_FIVE_MINUTES = (5 * 60) * 20;

    private boolean reinforced;
    private int hitCount;

    public EnchantedCatalyst(Block block, Material material, ItemStack itemStack, Location location, Minigame minigame) {
        super(block, material, itemStack, location, minigame);
    }

    @Override // This starts the mini-game's core functionality :)
    public void placeBlock() {
        super.placeBlock();
        minigame.start();

        minigame.setEnchantedCatalyst(this);

        block.setMetadata("enchanted-catalyst", new FixedMetadataValue(StorySMPPlugin.getInstance(), true));

        Bukkit.getScheduler().runTaskTimerAsynchronously(StorySMPPlugin.getInstance(), (task) -> {
            if (!block.isSolid()) {
                task.cancel();
                return;
            }
            ParticleUtils.drawCircleAroundBlock(block, 2.5, 40, Particle.ENCHANT);
        }, 5L, 3L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (random.nextInt(0, 100) + 1 > 20) continue;

            QuestType type = QuestType.getRandomQuest();
            if (type == QuestType.TRAVEL_BLOCKS) minigame.setTrackMoves(player);

            minigame.getQuestProgressManager().initializeQuest(type, player);
        }
    }

    @Override
    public void breakBlock() {
        super.breakBlock();
        Bukkit.getServer().showTitle(Title.title(
                Component.text("The server has been saved!")
                        .color(TextColor.color(216, 60, 169))
                        .decorate(TextDecoration.BOLD),
                Component.empty())
        );

        org.bukkit.Sound type = org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL;
        Bukkit.getServer().playSound(Sound.sound().type(type).build());

        minigame.pardonBannedPlayers();

        minigame.uncorruptAll();

        minigame.setEnchantedCatalyst(null);
    }

    @Override
    public void tick() {
        int radius = 5;
        World world = location.getWorld();

        if (world == null) return;

        Location center = location.clone();
        Location randomLocation = getRandomSolidLocation(center, radius);

        if (randomLocation != null && random.nextInt(0, 100) + 1 < 40) {
            Block targetBlock = randomLocation.getBlock();
            targetBlock.setType(itemStack.getType(), false);
        }
    }

    @Override
    public long duration() {
        return TICKS_IN_FIVE_MINUTES;
    }

    private static Location getRandomSolidLocation(Location origin, int radius) {
        int offsetX = random.nextInt(radius * 2 + 1) - radius;
        int offsetZ = random.nextInt(radius * 2 + 1) - radius;

        Location baseXZ = origin.clone().add(offsetX, 0, offsetZ);
        return Utils.getNearestSolidBlock(baseXZ, origin.getBlockY(), 5);
    }

    public void reinforce() {
        reinforced = true;
        Bukkit.broadcast(Component.text("The enchanted catalyst has been ")
                .color(NamedTextColor.DARK_RED)
                .append(Component.text("REINFORCED!")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)));
    }

    public boolean isReinforced() {
        return reinforced;
    }

    public void hitOnce() {
        hitCount++;
        Bukkit.broadcast(Component.text("Hit count = " + hitCount));

        if (hitCount >= 20 && reinforced) {
            minigame.getEnchantedCatalyst().breakBlock();
        } else if (hitCount >= 8 && !reinforced) {
            minigame.getEnchantedCatalyst().breakBlock();
        }
    }
}