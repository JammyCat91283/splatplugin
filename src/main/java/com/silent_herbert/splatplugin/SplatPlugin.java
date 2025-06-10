package com.silent_herbert.splatplugin;

import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
    private final Map<Location, Material> inkStorage = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");

    private final Material[] validWoolColors = {
        Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
        Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
        Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
        Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
    };

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
        Material woolType = getWoolColor(color);
        if (woolType == null) {
            player.sendMessage(ChatColor.RED + "Invalid team! Choose a valid wool color.");
            return;
        }
        playerTeams.put(player, color);
        player.sendMessage(ChatColor.GREEN + "You joined the " + ChatColor.BOLD + color + " team!");
    }

    public String getPlayerTeam(Player player) {
        return playerTeams.getOrDefault(player, "none");
    }

    public Material getWoolColor(String color) {
        for (Material wool : validWoolColors) {
            if (wool.name().toLowerCase().contains(color)) {
                return wool;
            }
        }
        return null;
    }

    public boolean isSplatterWeapon(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(splatterKey, PersistentDataType.STRING);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("givesplatter")) {
            if (!player.getName().equalsIgnoreCase("Silent_Herbert")) {
                player.sendMessage(ChatColor.RED + "You are not worthy of The Splatter!");
                return true;
            }

            ItemStack splatterWeapon = new ItemStack(Material.BOW);
            ItemMeta meta = splatterWeapon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "The Splatter");
                meta.getPersistentDataContainer().set(splatterKey, PersistentDataType.STRING, "true");
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

            setPlayerTeam(player, args[0].toLowerCase());
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerShootInk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isSplatterWeapon(item)) {
            event.setCancelled(true);

            Snowball inkShot = player.launchProjectile(Snowball.class);
            inkShot.setVelocity(player.getLocation().getDirection().multiply(1.5));
            inkShot.setCustomName("SPLATTER_SHOT");

            player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
        }
    }

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

        Material inkMaterial = getWoolColor(team);
        inkStorage.put(hitLoc, hitLoc.getBlock().getType());
        hitLoc.getBlock().setType(inkMaterial);

        spreadInk(hitLoc, inkMaterial);
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_WET_GRASS_PLACE, 1.0f, 1.0f);
    }

    private void spreadInk(Location loc, Material inkMaterial) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location newLoc = loc.clone().add(x, 0, z);
                if (isAirNearby(newLoc)) {
                    inkStorage.put(newLoc, newLoc.getBlock().getType());
                    newLoc.getBlock().setType(inkMaterial);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (inkStorage.containsKey(loc)) {
            event.setCancelled(true);
            event.getBlock().setType(inkStorage.get(loc));
            inkStorage.remove(loc);
        }
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
