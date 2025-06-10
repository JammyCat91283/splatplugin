package com.silent_herbert.splatplugin;

import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SplatPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Player, String> playerTeams = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);
        saveDefaultConfig();
        getLogger().info("SplatPlugin has been enabled!");

        // Register event listeners & commands
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("team").setExecutor(this);
        getCommand("givesplatter").setExecutor(this);
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

    // Command to give The Splatter weapon (Only Silent_Herbert)
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("givesplatter")) {
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

        if (label.equalsIgnoreCase("team")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /team <color>");
                return true;
            }

            String color = args[0].toLowerCase();
            setPlayerTeam(player, color);
            return true;
        }

        return false;
    }

    // Detect right-click to shoot ink
    @EventHandler
    public void onPlayerShootInk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isSplatterWeapon(item)) {
            event.setCancelled(true);

            // Shoot The Splatter's ink shot
            Snowball inkShot = player.launchProjectile(Snowball.class);
            inkShot.setVelocity(player.getLocation().getDirection().multiply(1.5));
            inkShot.setCustomName("SPLATTER_SHOT");

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
        String team = getPlayerTeam(shooter);

        if (team.equalsIgnoreCase("none")) return;

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

    // Ink Mechanics (Movement Boosts & Slowdowns)
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String team = getPlayerTeam(player);
        Material below = player.getLocation().getBlock().getType();

        if (team.equalsIgnoreCase("none")) return;

        if (below == Material.ORANGE_WOOL && team.equalsIgnoreCase("orange") ||
            below == Material.BLUE_WOOL && team.equalsIgnoreCase("blue")) {
            player.setWalkSpeed(0.4f);
        } else if (below == Material.ORANGE_WOOL && team.equalsIgnoreCase("blue") ||
                   below == Material.BLUE_WOOL && team.equalsIgnoreCase("orange")) {
            player.setWalkSpeed(0.1f);
        } else {
            player.setWalkSpeed(0.2f);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String team = getPlayerTeam(player);
        Material below = player.getLocation().getBlock().getType();

        if (team.equalsIgnoreCase("none")) return;
        if (!event.isSneaking()) return;

        if ((below == Material.ORANGE_WOOL && team.equalsIgnoreCase("orange")) ||
            (below == Material.BLUE_WOOL && team.equalsIgnoreCase("blue"))) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0, true, false));
        }
    }
}
