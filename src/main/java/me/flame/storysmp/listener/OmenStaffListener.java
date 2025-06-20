package me.flame.storysmp.listener;

import me.flame.storysmp.Minigame;
import me.flame.storysmp.StorySMPPlugin;
import me.flame.storysmp.commands.CustomItem;
import me.flame.storysmp.factory.ItemFactory;
import me.flame.storysmp.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class OmenStaffListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(OmenStaffListener.class);
    private final Set<UUID> omenStaffOwners = new HashSet<>();
    private final Map<UUID, Instant> cooldowns = new HashMap<>();

    private final ItemStack omenStaff;

    private final Minigame minigame;

    public OmenStaffListener(Minigame minigame) {
        this.omenStaff = CustomItem.OMEN_STAFF.getCustomItemStack();
        this.minigame = minigame;
    }

    @EventHandler
    public void onPickupOmenStaff(EntityPickupItemEvent event) {
        LivingEntity player = event.getEntity();
        if (!(player instanceof Player)) return;

        ItemStack item = event.getItem().getItemStack();

        if (item.isSimilar(this.omenStaff)) {
            omenStaffOwners.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDropOmenStaff(EntityDropItemEvent event) {
        Entity player = event.getEntity();
        if (!(player instanceof Player)) return;

        ItemStack item = event.getItemDrop().getItemStack();

        if (item.isSimilar(this.omenStaff)) {
            omenStaffOwners.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDropOmenStaff(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();

        for (ItemStack item : drops) {
            if (item.isSimilar(this.omenStaff)) {
                omenStaffOwners.remove(player.getUniqueId());
                return;
            }
        }

    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(ItemFactory.IS_OMEN_STAFF)) return;

        Player player = event.getPlayer();
        boolean enabled = StorySMPPlugin.getInstance().getPrimaryConfig().getBoolean("omen-staff.enabled");
        if (!enabled) {
            player.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("general.not-enabled")));
            return;
        }

        Instant cooldown = cooldowns.get(player.getUniqueId());
        if (cooldown != null && checkCooldown(cooldown, player)) return;

        Long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("omen-staff.cooldown");
        cooldowns.put(player.getUniqueId(), Instant.now().plus(Duration.ofSeconds(totalCooldown)));

        if (player.isSneaking()) {
            spawnCrouchFangs(player);
            return;
        }

        spawnLineFangs(player);
    }

    @EventHandler
    public void onTargetPlayer(EntityTargetLivingEntityEvent event) {
        LivingEntity target = event.getTarget();
        if (!(target instanceof Player player)) return;

        Entity entity = event.getEntity();
        if (minigame.isDiffused(target.getUniqueId())) {
            target.sendMessage(Utils.color(StorySMPPlugin.getInstance().getMessagesConfig().get("diffuser.victim-is-diffused")));
            return;
        }

        UUID owner = entity.getPersistentDataContainer().get(ItemFactory.OWNER, ItemFactory.UUID_DATA_TYPE);
        if (owner != null && !owner.equals(target.getUniqueId())) return;

        if (player.getInventory().getItemInMainHand().getPersistentDataContainer().has(ItemFactory.IS_OMEN_STAFF)
                || player.getInventory().getItemInOffHand().getPersistentDataContainer().has(ItemFactory.IS_OMEN_STAFF)) {
            event.setCancelled(true);
        }
    }

    public void spawnLineFangs(@NotNull Player player) {
        Location base = player.getLocation().add(0, -0.5, 0);
        Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize();

        // Force to X axis only
        direction.setZ(0).normalize();

        Integer distance = StorySMPPlugin.getInstance().getPrimaryConfig().getInt("omen-staff.standing.distance");
        Double damage = StorySMPPlugin.getInstance().getPrimaryConfig().getDouble("omen-staff.standing.damage");

        for (int i = 0; i < distance; i++) {
            Location loc = base.clone().add(direction.clone().multiply(i));
            spawnFang(loc, damage, player);
        }
    }

    public void spawnCrouchFangs(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();

        Integer radius = StorySMPPlugin.getInstance().getPrimaryConfig().getInt("omen-staff.sneaking.radius");
        Double damage = StorySMPPlugin.getInstance().getPrimaryConfig().getDouble("omen-staff.sneaking.damage");

        for (double x = -radius; x <= radius; x++) {
            for (double z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Location loc = center.clone().add(x, -0.5, z);
                    spawnFang(loc, damage, player); // 3 hearts
                }
            }
        }

        // Clear cobwebs
        boolean removed = false;
        for (Block b : getNearbyBlocks(center, radius)) {
            if (b.getType() == Material.COBWEB) {
                b.setType(Material.AIR);
                removed = true;
            }
        }

        if (removed) world.playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
    }

    @EventHandler
    public void damagedByRaidMob(@NotNull EntityDamageByEntityEvent event) {
        Entity causingEntity = event.getDamageSource().getCausingEntity();
        Entity entity = event.getEntity();

        if (!(entity instanceof Player killed)) return;

        if (causingEntity == null) return;

        UUID owner = causingEntity.getPersistentDataContainer().get(ItemFactory.OWNER, ItemFactory.UUID_DATA_TYPE);

        Player player;
        if (owner != null && (player = Bukkit.getPlayer(owner)) != null && player.isOnline()) {
            event.setCancelled(true);
            killed.damage(event.getDamage(), player);
        }
    }

    public List<Block> getNearbyBlocks(Location loc, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(loc.clone().add(x, y, z).getBlock());
                }
            }
        }
        return blocks;
    }

    public void spawnFang(Location loc, double damage, Player player) {
        World world = loc.getWorld();
        EvokerFangs fang = (EvokerFangs) world.spawnEntity(loc, EntityType.EVOKER_FANGS);
        fang.setOwner(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                world.playSound(loc, Sound.ENTITY_EVOKER_FANGS_ATTACK, 1.0f, 1.0f);
                for (Entity e : fang.getNearbyEntities(0.5, 1, 0.5)) {
                    if (e instanceof LivingEntity le && !le.getUniqueId().equals(fang.getOwner().getUniqueId())) {
                        le.damage(damage, player);
                    }
                }
            }
        }.runTaskLater(StorySMPPlugin.getInstance(), 10L);
    }

    private static boolean checkCooldown(Instant cooldown, Player player) {
        Instant now = Instant.now();
        if (now.isBefore(cooldown)) {
            String message = StorySMPPlugin.getInstance().getMessagesConfig().get("omen-staff.on-cooldown");
            long totalCooldown = StorySMPPlugin.getInstance().getPrimaryConfig().getLong("omen-staff.cooldown");
            player.sendMessage(Utils.color(message
                    .replace("%duration%", String.valueOf(Duration.between(now, cooldown).getSeconds()))
                    .replace("%total%", String.valueOf(totalCooldown))));
            return true;
        }
        return false;
    }
}
