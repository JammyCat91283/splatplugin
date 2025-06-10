package com.silent_herbert.splatplugin;

import io.papermc.lib.PaperLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SplatPlugin extends JavaPlugin {

    private final Map<Player, String> playerTeams = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);
        saveDefaultConfig();
        getLogger().info("SplatPlugin has been enabled!");

        // Register event listeners & commands
        getServer().getPluginManager().registerEvents(new InkMechanics(this), this);
        getServer().getPluginManager().registerEvents(new InkShooting(this), this);
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("givesplatter").setExecutor(new GiveSplatterCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("SplatPlugin has been disabled!");
    }

    public void setPlayerTeam(Player player, String color) {
        playerTeams.put(player, color);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou joined the " + color + " team!"));
    }

    public String getPlayerTeam(Player player) {
        return playerTeams.getOrDefault(player, "none");
    }

    public boolean isSplatterWeapon(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(splatterKey, PersistentDataType.STRING);
    }
}

// Command to give The Splatter weapon (Only Silent_Herbert)
class GiveSplatterCommand implements CommandExecutor {
    private final SplatPlugin plugin;
    private final NamespacedKey splatterKey;

    public GiveSplatterCommand(SplatPlugin plugin) {
        this.plugin = plugin;
        this.splatterKey = new NamespacedKey(plugin, "splatterweapon");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Restrict command usage to Silent_Herbert
        if (!player.getName().equalsIgnoreCase("Silent_Herbert")) {
            player.sendMessage(ChatColor.RED + "You are not worthy of The Splatter!");
            return true;
        }

        // Create the Splatter weapon (Bow)
        ItemStack splatterWeapon = new ItemStack(Material.BOW);
        ItemMeta meta = splatterWeapon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eThe Splatter"));
            meta.getPersistentDataContainer().set(splatterKey, PersistentDataType.STRING, "true"); // Custom NBT tag
            splatterWeapon.setItemMeta(meta);
        }

        player.getInventory().addItem(splatterWeapon);
        player.sendMessage(ChatColor.GREEN + "You have received " + ChatColor.YELLOW + "The Splatter!");
        return true;
    }
}

// Command to change team to any color
class TeamCommand implements CommandExecutor {
    private final SplatPlugin plugin;

    public TeamCommand(SplatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /team <color>");
            return true;
        }

        String color = args[0].toLowerCase();
        plugin.setPlayerTeam(player, color);
        return true;
    }
}

// Ink Shooting Mechanics (Different Weapons & Projectile Handling)
class InkShooting implements Listener {
    private final SplatPlugin plugin;

    public InkShooting(SplatPlugin plugin) {
        this.plugin = plugin;
    }

    // Detect right-click to shoot ink
    @EventHandler
    public void onPlayerShootInk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (plugin.isSplatterWeapon(item)) {
            event.setCancelled(true);
            
            // Shoot The Splatter's ink shot
            Snowball inkShot = player.launchProjectile(Snowball.class);
            inkShot.setVelocity(player.getLocation().getDirection().multiply(1.5));
            inkShot.setCustomName("SPLATTER_SHOT"); // Identifies it as The Splatter's shot
            
            player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
        }
    }

    // Detect ink hitting a block
    @EventHandler
    public void onInkHitBlock(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        Snowball inkShot = (Snowball) event.getEntity();

        if (!"SPLATTER_SHOT".equals(inkShot.getCustomName())) return;
        if (!(inkShot.getShooter() instanceof Player)) return;

        Player shooter = (Player) inkShot.getShooter();
        Location hitLoc = event.getHitBlock().getLocation();
        String team = plugin.getPlayerTeam(shooter);

        if (team.equalsIgnoreCase("none")) return;

        // Check surrounding air blocks to ensure proper ink placement
        if (!isAirNearby(hitLoc)) return;

        Material inkMaterial = team.equalsIgnoreCase("blue") ? Material.BLUE_WOOL : Material.ORANGE_WOOL;
        hitLoc.getBlock().setType(inkMaterial);
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_WET_GRASS_PLACE, 1.0f, 1.0f);
    }

    private boolean isAirNearby(Location loc) {
        World world = loc.getWorld();
        return world.getBlockAt(loc.clone().add(1, 0, 0)).getType() == Material.AIR ||
               world.getBlockAt(loc.clone().add(-1, 0, 0)).getType() == Material.AIR ||
               world.getBlockAt(loc.clone().add(0, 1, 0)).getType() == Material.AIR ||
               world.getBlockAt(loc.clone().add(0, -1, 0)).getType() == Material.AIR ||
               world.getBlockAt(loc.clone().add(0, 0, 1)).getType() == Material.AIR ||
               world.getBlockAt(loc.clone().add(0, 0, -1)).getType() == Material.AIR;
    }
}
