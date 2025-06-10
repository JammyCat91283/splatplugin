package com.silent_herbert.splatplugin;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.util.Vector;
// plugin command
import org.bukkit.command.PluginCommand;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SplatPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Player, String> playerTeams = new HashMap<>();
    private final Map<Location, Material> inkStorage = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");
    // splatplugin-splatterink is the key for the splatter weapon ink
    private final NamespacedKey splatterInkKey = new NamespacedKey(this, "splatplugin-splatterink");
    // splatpluginweapon
    private final NamespacedKey splatPluginWeaponKey = new NamespacedKey(this, "splatpluginweapon");

    private final Material[] validWoolColors = {
        Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
        Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
        Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
        Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
    };

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

    public String isSplatPluginWeapon(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(splatPluginWeaponKey, PersistentDataType.STRING)) {
            String weaponType = meta.getPersistentDataContainer().get(splatPluginWeaponKey, PersistentDataType.STRING);
            if (weaponType != null) {
                return weaponType;
            }
        }
        return null;
    }
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SplatPlugin has been enabled!");
        // commands (for some reason) just work with onCommand, so we don't need to register them
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
                meta.setLore(java.util.Collections.singletonList(ChatColor.GRAY + "A powerful ink weapon!"));
                meta.getPersistentDataContainer().set(splatPluginWeaponKey, PersistentDataType.STRING, splatterKey.toString());
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
        // /clearink command to clear ink storage and restore original blocks
        if (label.equalsIgnoreCase("clearink")) {
            if (inkStorage.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No ink to clear!");
                return true;
            }
            for (Map.Entry<Location, Material> entry : inkStorage.entrySet()) {
                Location loc = entry.getKey();
                Material originalMaterial = entry.getValue();
                if (loc.getBlock().getType() == Material.AIR) continue; // skip air blocks
                loc.getBlock().setType(originalMaterial);
            }
            player.sendMessage(ChatColor.GREEN + "All ink has been cleared!");
            return true;
        }

    // get command because paper is crying about it returning null
    public PluginCommand getCommand(String name) {
        PluginCommand command = super.getCommand(name);
        if (command == null) {
            command = getServer().getPluginCommand(name);
            if (command == null) {
                getLogger().warning("Command '" + name + "' not found!");
                return null;
            }
        }
        return command;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GREEN + "Welcome to SplatPlugin! Use /team <color> to join a team.");
    }
    @EventHandler
    public void onPlayerShootInk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isSplatPluginWeapon(item) == splatterKey.toString()) {
            

            for (double offsetX : new double[]{-0.5, 0.5}) {
                for (double offsetY : new double[]{-0.5, 0.5}) {
                    for (double offsetZ : new double[]{-0.5, 0.5}) {
                        Vector direction = player.getLocation().getDirection().clone();
                        direction.add(new Vector(offsetX, offsetY, offsetZ)).normalize().multiply(1.5);
                        Snowball inkShot = player.launchProjectile(Snowball.class, direction);
                        inkShot.setCustomName(splatterInkKey.toString());
                        inkShot.setCustomNameVisible(false);
                        inkShot.setShooter(player);
                    }
                }
            }

            player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            event.setCancelled(true);
        }
    }
    private void inkBlock(Location loc, Material inkMaterial, String weapon) {
        if (loc == null || inkMaterial == null) return;
        // Abort if the block is already air
        if (loc.getBlock().getType() == Material.AIR) return;
        // Save the original block type if not already stored
        
        // if weapon = splatterKey, then if a ink hits a same team ink block, it will randomly go around until it finds a block that is not inked same.
        if (weapon.equals(splatterInkKey.toString())) {
            // Check if the block is already inked with the same color
            if (loc.getBlock().getType() == inkMaterial) {
                // start searching for a nearby block that is not inked with the same color
                // but don't go too far, just check for a length of 3 blocks around it (this is called a nerf)
                // so like this: start from the current location and then do a random move and make count + 1. if count > 3, then restart
                // do this a max of 3 times before giving up
                int count = 0;
                while (count < 3) {
                    int x = (int) (Math.random() * 3) - 1; // Randomly choose -1, 0, or 1
                    int y = (int) (Math.random() * 3) - 1;
                    int z = (int) (Math.random() * 3) - 1;
                    Location newLoc = loc.clone().add(x, y, z);
                    
                    if (newLoc.getBlock().getType() != inkMaterial && newLoc.getBlock().getType() != Material.AIR) {
                        // Save the original block type if not already stored
                        if (!inkStorage.containsKey(newLoc)) {
                            inkStorage.put(newLoc, newLoc.getBlock().getType());
                        }
                        // Set the block to the ink material
                        newLoc.getBlock().setType(inkMaterial);
                        return; // Exit after successfully inking a different block
                    }
                    count++;
                }
                // If we reach here, it means we couldn't find a suitable block to ink
                loc.getBlock().setType(inkMaterial); // Fallback to inking the original block

            } else {
                // Save the original block type if not already stored
                if (!inkStorage.containsKey(loc)) {
                    inkStorage.put(loc, loc.getBlock().getType());
                }
                // Set the block to the ink material
                loc.getBlock().setType(inkMaterial);
            }
        }
    }
    @EventHandler
    public void onInkHitBlock(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        Snowball inkShot = (Snowball) event.getEntity();

        if (inkShot.getCustomName() == null || !inkShot.getCustomName().startsWith("splatplugin-")) return;
        if (!(inkShot.getShooter() instanceof Player)) return;

        Player shooter = (Player) inkShot.getShooter();
        Location hitLoc = event.getHitBlock().getLocation();
        String team = getPlayerTeam(shooter);

        if (team.equalsIgnoreCase("none")) return;
        //if (!isExposedToAir(hitLoc)) return;

        Material inkMaterial = getWoolColor(team);
        inkBlock(hitLoc, inkMaterial, inkShot.getCustomName());

        // spreadInk(hitLoc, inkMaterial); nvm i just had the best idea ever
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_WET_GRASS_PLACE, 1.0f, 1.0f);
    }

    private void spreadInk(Location loc, Material inkMaterial) {
        World world = loc.getWorld();
        
        if (world == null || inkMaterial == null) return;

        // loop blocks radius 2 around the hit location 
        // for each block, check if it's not air and exposed to air then set it to the ink material
        // don't overwrite existing ink blocks cause that would make the ink spread over ink, permanently erasing the original block
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location newLoc = loc.clone().add(x, y, z);
                    if (newLoc.getBlock().getType() != Material.AIR && isExposedToAir(newLoc)) {
                        
                        if (!inkStorage.containsKey(newLoc)) { // get data instead of just type so chests and other blocks can be restored
                            // save data of the original block
                            
                            inkStorage.put(newLoc, newLoc.getBlock().getType());
                            
                        }
                        newLoc.getBlock().setType(inkMaterial);
                    }
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

    private boolean isExposedToAir(Location loc) {
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

        Material teamWool = getWoolColor(team);
        if (teamWool != null && below == teamWool) {
            player.setWalkSpeed(0.4f);
        } else if (below.toString().endsWith("_WOOL")) {
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

        Material teamWool = getWoolColor(team);
        if (teamWool != null && below == teamWool) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0, true, false));
        }
    }
}
