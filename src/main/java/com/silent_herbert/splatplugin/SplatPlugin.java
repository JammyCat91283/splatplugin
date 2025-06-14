package com.silent_herbert.splatplugin;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.block.Action;
// plugin command
import org.bukkit.command.PluginCommand;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.ibm.icu.message2.Mf2DataModel.Text;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
// runnable
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.entity.Breeze;

public class SplatPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Player, String> playerTeams = new HashMap<>();

    private static class InkRecord {
        private final Color color;

        public InkRecord(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private final Map<Location, InkRecord> inkStorage = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");
    // splatplugin-splatterink is the key for the splatter weapon ink
    private final NamespacedKey splatterInkKey = new NamespacedKey(this, "splatterink");
    // splatpluginweapon
    private final NamespacedKey splatPluginWeaponKey = new NamespacedKey(this, "splatpluginweapon");
    // testweapon
    private final NamespacedKey testWeaponKey = new NamespacedKey(this, "testweapon");
    // inktank
    private final NamespacedKey inkTankKey = new NamespacedKey(this, "inktank");
    private final Map<String, Long> weaponCooldowns = new HashMap<>();
    private int valuestuffs = -1;
    // Rules configuration:
    // - InfiniteInk: controls if players have infinite ink.
    // - DisableCooldowns: controls if weapon cooldowns are disabled.
    // Both can be set globally (by passing a null target) or individually (by
    // providing a target player).
    private Boolean globalInfiniteInkRule = null; // null means use per-player settings; true/false overrides globally.
    private Boolean globalDisableCooldownsRule = null; // null means use per-player settings.

    private final Map<String, Boolean> infiniteInkRules = new HashMap<>();
    private final Map<String, Boolean> cooldownsRules = new HashMap<>();
    private final Map<String, Number> chargesScope = new HashMap<>();
    private final List<Material> airLikeMaterials = new ArrayList<>(Arrays.asList(
            Material.AIR));

    private void populateExtendedAirLike() {
        String[] blockTags = new String[] {
                "#minecraft:all_signs",
                "#minecraft:doors",
                "#minecraft:banners",
                "#minecraft:fire",
                "#minecraft:rails",
                "#minecraft:beds",
                "#minecraft:bee_growables",
                "#minecraft:wool_carpets",
                "#minecraft:buttons",
                "#minecraft:candles",
                "#minecraft:candle_cakes",
                "#minecraft:climbable",
                "#minecraft:corals",
                "#minecraft:flowers",
                "#minecraft:flower_pots",
                "#minecraft:pressure_plates",
                "#minecraft:saplings",
                "minecraft:end_portal",
                "minecraft:end_gateway",
                "minecraft:torch",
                "minecraft:wall_torch",
                "minecraft:soul_torch",
                "minecraft:soul_wall_torch",
                "minecraft:lantern",
                "minecraft:soul_lantern",
                "minecraft:anvil",
                "minecraft:bamboo",
                "minecraft:bell",
                "minecraft:brewing_stand",
                "minecraft:cake",
                "minecraft:chain",
                "minecraft:chest",
                "minecraft:cobweb",
                "minecraft:composter",
                "minecraft:conduit",
                "minecraft:dead_bush",
                "minecraft:detector_rail",
                "minecraft:dragon_egg",
                "minecraft:end_rod",
                "minecraft:warped_fungus",
                "minecraft:crimson_fungus",
                "minecraft:player_head",
                "minecraft:player_wall_head",
                "minecraft:lectern",
                "minecraft:lily_pad",
                "minecraft:nether_sprouts",
                "minecraft:redstone_wire",
                "minecraft:redstone_wall_torch",
                "minecraft:redstone_torch",
                "minecraft:repeater",
                "minecraft:crimson_roots",
                "minecraft:sea_pickle",
                "minecraft:sugar_cane",
                "minecraft:sweet_berry_bush",
                "minecraft:tall_grass",
                "minecraft:tripwire_hook",
                "minecraft:turtle_egg",
                "minecraft:twisting_vines",
                "minecraft:vine",
                "minecraft:water",
                "minecraft:weeping_vines",
                "minecraft:redstone",
                "minecraft:moss_carpet",
                "minecraft:air",
                "minecraft:water",
                "minecraft:lava",
                "minecraft:short_grass",
                "minecraft:tall_grass",
                "minecraft:iron_bars",
                "minecraft:chain",
                "minecraft:dandelion",
                "minecraft:poppy",
                "minecraft:blue_orchid",
                "minecraft:allium",
                "minecraft:azure_bluet",
                "minecraft:red_tulip",
                "minecraft:orange_tulip",
                "minecraft:white_tulip",
                "minecraft:pink_tulip",
                "minecraft:oxeye_daisy",
                "minecraft:cornflower",
                "minecraft:lily_of_the_valley",
                "minecraft:seagrass",
                "minecraft:oak_fence",
                "minecraft:birch_fence",
                "minecraft:spruce_fence",
                "minecraft:jungle_fence",
                "minecraft:acacia_fence",
                "minecraft:dark_oak_fence",
                "minecraft:cobblestone_wall",
                "minecraft:mossy_cobblestone_wall",
                "minecraft:chest",
                "minecraft:trapped_chest",
                "minecraft:red_bed",
                "minecraft:blue_bed",
                "minecraft:furnace",
                "minecraft:blast_furnace",
                "minecraft:smoker",
                "minecraft:redstone",
                "minecraft:string",
                "minecraft:piston",
                "minecraft:piston_head",
                "minecraft:sticky_piston",
                "minecraft:copper_grate"
        };

        for (Material material : Material.values()) {
            for (String tag : blockTags) {
                if (tag.startsWith("#")) {
                    // Treat as a tag identifier.
                    String tagName = tag.substring(1);
                    try {
                        String namespace;
                        String key;
                        if (tagName.contains(":")) {
                            String[] parts = tagName.split(":");
                            namespace = parts[0];
                            key = parts[1];
                        } else {
                            namespace = "minecraft";
                            key = tagName;
                        }
                        NamespacedKey tagKey = new NamespacedKey(namespace, key);
                        Tag<Material> tagr = Bukkit.getTag("blocks", tagKey, Material.class);
                        if (tagr != null && tagr.getValues().contains(material)) {
                            if (!airLikeMaterials.contains(material)) {
                                airLikeMaterials.add(material);
                            }
                            break; // found a matching tag, skip further checks for this material
                        }
                    } catch (Exception e) {
                        getLogger().warning("Tag not found: " + tag);
                    }
                } else {
                    // Treat the string as a direct material name.
                    try {
                        Material directMaterial = Material.valueOf(tag.toUpperCase());
                        if (material == directMaterial) {
                            if (!airLikeMaterials.contains(material)) {
                                airLikeMaterials.add(material);
                            }
                            break; // found a match, skip further checks for this material
                        }
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
    }

    // Returns true if the player has infinite ink enabled.
    public boolean hasInfiniteInk(Player player) {
        if (globalInfiniteInkRule != null) {
            return globalInfiniteInkRule;
        }
        return infiniteInkRules.getOrDefault(player.getName().toLowerCase(), false);
    }

    // Returns true if cooldowns are disabled for the player.
    public boolean isCooldownDisabled(Player player) {
        if (globalDisableCooldownsRule != null) {
            return globalDisableCooldownsRule;
        }
        return cooldownsRules.getOrDefault(player.getName().toLowerCase(), false);
    }

    // Only admins can change rules.
    // If 'target' is null, the rule is applied globally.
    public void setInfiniteInkRule(Player target, boolean enabled, CommandSender sender) {
        if (!(sender.isOp() || sender.hasPermission("splatplugin.admin"))) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to change rules.");
            return;
        }
        if (target == null) {
            globalInfiniteInkRule = enabled;
            sender.sendMessage(ChatColor.GREEN + "Global InfiniteInk rule set to " + enabled + ".");
        } else {
            infiniteInkRules.put(target.getName().toLowerCase(), enabled);
            sender.sendMessage(
                    ChatColor.GREEN + "InfiniteInk rule for " + target.getName() + " set to " + enabled + ".");
        }
    }

    public void setDisableCooldownsRule(Player target, boolean disabled, CommandSender sender) {
        if (!(sender.isOp() || sender.hasPermission("splatplugin.admin"))) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to change rules.");
            return;
        }
        if (target == null) {
            globalDisableCooldownsRule = disabled;
            sender.sendMessage(ChatColor.GREEN + "Global DisableCooldowns rule set to " + disabled + ".");
        } else {
            cooldownsRules.put(target.getName().toLowerCase(), disabled);
            sender.sendMessage(
                    ChatColor.GREEN + "DisableCooldowns rule for " + target.getName() + " set to " + disabled + ".");
        }
    }

    @Override
    public void onDisable() {
        // Restore all inked blocks to their original materials when reloading.
        // for (Location loc : new HashSet<>(inkStorage.keySet())) {
        // unink(loc);
        // } // save this
        // inkStorage.clear();
        /*
         * private final Map<String, Long> weaponCooldowns = new HashMap<>();
         * // Rules configuration:
         * // - InfiniteInk: controls if players have infinite ink.
         * // - DisableCooldowns: controls if weapon cooldowns are disabled.
         * // Both can be set globally (by passing a null target) or individually (by
         * // providing a target player).
         * private Boolean globalInfiniteInkRule = null; // null means use per-player
         * settings; true/false overrides globally.
         * private Boolean globalDisableCooldownsRule = null; // null means use
         * per-player settings.
         * 
         * private final Map<String, Boolean> infiniteInkRules = new HashMap<>();
         * private final Map<String, Boolean> cooldownsRules = new HashMap<>();
         */ // save those
        getLogger().info("All ink has been cleared on reload.");
    }

    // new feature: list
    // {"splatterweapon":{name:"The Splatter",ink:"splatterink",
    // weapon:Material.BOW,cost:2.5f, damage:5.0f, force:1.0f, cooldown:0.3f,
    // auto:true}, debugweapon:{name:"The Debug Splatter",ink:"splatterink",
    // weapon:Material.BOW,cost:2.5f, damage:5.0f, force:1.0f, cooldown:0.3f,
    // auto:true},"examplesubweapon":{name:"Example Sub Weapon",ink:"subweaponink",
    // weapon:Material.TNT,cost:1.0f, damage:10.0f, force:3.0f, cooldown:3.0f,
    // auto:false}}
    private final Map<String, Map<String, Object>> weaponList = new HashMap<>();

    // Load weapon configurations from the resource file "config.yml".
    //
    // GUIDE: How to add a new weapon:
    // 1. Open config.yml in the plugin's data folder (created by
    // saveDefaultConfig() in onEnable()).
    // 2. Under the "weapons" section, add a new weapon entry. For example:
    //
    // weapons:
    // splatterweapon:
    // name: "The Splatter"
    // ink: "splatterink"
    // damage: 5.0
    // cost: 2.5
    // force: 1.0
    // cooldown: 0.3
    // auto: true
    //
    // 3. Save the file and restart or reload the server and the plugin will load
    // the new weapon automatically.
    //
    // Note: Ensure saveDefaultConfig() is called in onEnable() before this code
    // runs.
    @Override
    public void onLoad() {
        populateExtendedAirLike();
    }

    private final Material[] validWoolColors = {
            Material.YELLOW_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
            Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.ORANGE_WOOL
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
        // commands (for some reason) just work with onCommand, so we don't need to
        // register them
        // prepare the runnable for ink regen for this plugin
        BukkitTask inkRegenTask = new BukkitRunnable() {
            @Override
            public void run() {
                onInkRegen();
            }
        }.runTaskTimer(this, 0L, 1L); // Run every ticj

        // stuffffsss
        saveResource("config.yml", true);
        reloadConfig();
        org.bukkit.configuration.ConfigurationSection weaponsSection = getConfig().getConfigurationSection("weapons");
        if (weaponsSection != null) {
            for (String weaponName : weaponsSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection section = weaponsSection
                        .getConfigurationSection(weaponName);
                if (section != null) {
                    Map<String, Object> weaponConfig = new HashMap<>();
                    weaponConfig.put("name", section.getString("name"));
                    weaponConfig.put("damage", section.getDouble("damage"));
                    weaponConfig.put("ink", section.getString("ink"));
                    // We cast cost to float if needed, otherwise keep as Double.
                    weaponConfig.put("cost", (float) section.getDouble("cost"));
                    // You can add more parameters such as force, cooldown, auto, etc.:
                    weaponConfig.put("force", (float) section.getDouble("force"));
                    weaponConfig.put("cooldown", (float) section.getDouble("cooldown"));
                    weaponConfig.put("auto", section.getBoolean("auto"));
                    if (section.contains("weapon")) {
                        weaponConfig.put("weapon", section.getString("weapon"));
                    } else {
                        weaponConfig.put("weapon", "BOW");
                    }
                    if (section.contains("type")) {
                        weaponConfig.put("type", section.getString("type"));
                    } else {
                        weaponConfig.put("type", "weapon"); // Default type is "weapon"
                    }
                    weaponList.put(weaponName, weaponConfig);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // /giveweapon weapon
        if (label.equalsIgnoreCase("giveweapon")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /giveweapon <weapon>");
                return true;
            }
            String weaponId = args[0].toLowerCase();
            if (!weaponList.containsKey(weaponId)) {
                player.sendMessage(ChatColor.RED + "Weapon not found! Check your config.");
                return true;
            }
            Map<String, Object> weaponConfig = weaponList.get(weaponId);
            Material weaponMaterial = Material.BOW;
            if (weaponConfig.containsKey("weapon")) {
                try {
                    weaponMaterial = Material.valueOf(weaponConfig.get("weapon").toString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // fallback to BOW if material is invalid
                }
            }
            ItemStack weaponItem = new ItemStack(weaponMaterial);
            ItemMeta meta = weaponItem.getItemMeta();
            if (meta != null) {
                if (weaponConfig.containsKey("name")) {
                    meta.setDisplayName(ChatColor.GOLD + weaponConfig.get("name").toString());
                } else {
                    meta.setDisplayName(ChatColor.GOLD + weaponId);
                }
                meta.getPersistentDataContainer().set(splatPluginWeaponKey, PersistentDataType.STRING, weaponId);
                weaponItem.setItemMeta(meta);
            }
            player.getInventory().addItem(weaponItem);
            player.sendMessage(ChatColor.GREEN + "You have been given the weapon: " + weaponId);
            return true;
        }

        if (label.equalsIgnoreCase("team")) {
            if (args.length < 1 || args.length > 1) {
                player.sendMessage(ChatColor.RED + "Usage: /team <color>");
                return true;
            }
            // if the player is already have a tank, then remove it
            if (player.getInventory().containsAtLeast(new ItemStack(Material.BUCKET), 1)) {
                player.getInventory().removeItem(new ItemStack(Material.BUCKET));
                player.sendMessage(ChatColor.YELLOW + "Your previous Ink Tank has been removed.");
            }
            ItemStack inkTank = new ItemStack(Material.BUCKET);
            ItemMeta tankMeta = inkTank.getItemMeta();
            if (tankMeta != null) {
                tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank 100/100");
                tankMeta.setLore(java.util.Arrays.asList(ChatColor.GRAY + "A tank filled with vibrant ink"));
                tankMeta.setCustomModelData(100); // Custom model data to represent ink storage

                tankMeta.getPersistentDataContainer().set(splatPluginWeaponKey, PersistentDataType.STRING,
                        inkTankKey.toString());
                tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, 100.0); // Initial ink
                                                                                                         // amount

                inkTank.setItemMeta(tankMeta);
            }
            player.getInventory().addItem(inkTank);
            player.sendMessage(ChatColor.GREEN + "Your Ink Tank is ready!");
            setPlayerTeam(player, args[0].toLowerCase());
            return true;
        }
        // /clearink command to clear ink storage and restore original blocks
        if (label.equalsIgnoreCase("clearink")) {
            if (inkStorage.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No ink to clear!");
                return true;
            }
            for (Location loc : new HashSet<>(inkStorage.keySet())) {
                unink(loc);
            }
            player.sendMessage(ChatColor.GREEN + "All ink has been cleared!");
            return true;
        }
        if (label.equalsIgnoreCase("clearinkteam")) {
            if (inkStorage.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No ink to clear!");
                return true;
            }
            String team = getPlayerTeam(player);
            if (team.equalsIgnoreCase("none")) {
                player.sendMessage(ChatColor.RED + "You are not part of any team!");
                return true;
            }
            Material teamColor = getWoolColor(team);
            if (teamColor == null) {
                player.sendMessage(ChatColor.RED + "Your team's color is invalid!");
                return true;
            }

            List<Location> locationsToUnink = new ArrayList<>();
            boolean foundTeamInk = false; // Keep track if any team ink was found

            // Iterate through inkStorage to identify blocks to unink
            // We're iterating over a snapshot of the entries, which is safe.
            for (Map.Entry<Location, InkRecord> entry : inkStorage.entrySet()) {
                // Compare colors using the equals() method for proper comparison
                // Note: entry.getValue().color is a Bukkit Color object.
                // getBukkitColorForMaterial(teamColor) also returns a Bukkit Color object.
                if (entry.getValue().color.equals(getBukkitColorForMaterial(teamColor))) {
                    locationsToUnink.add(entry.getKey()); // Add the location to our list
                    foundTeamInk = true;
                }
            }

            // Now, outside the iteration loop, process the collected locations
            for (Location loc : locationsToUnink) {
                unink(loc); // Call unink for each collected location
            }
            if (!foundTeamInk) {
                player.sendMessage(ChatColor.RED + "No ink found for your team!");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Team ink has been cleared!");
            return true;
        }
        if (label.equalsIgnoreCase("splatrule")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /splatrule <rule> <value> [player]");
                return true;
            }
            String rule = args[0].toLowerCase();
            String valueStr = args[1].toLowerCase();
            if (!valueStr.equals("true") && !valueStr.equals("false")) {
                player.sendMessage(ChatColor.RED + "Value must be true or false.");
                return true;
            }
            boolean value = Boolean.parseBoolean(valueStr);
            Player target = null;
            if (args.length >= 3) {
                target = getServer().getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                    return true;
                }
            }
            if (rule.equals("infiniteink")) {
                setInfiniteInkRule(target, value, sender);
            } else if (rule.equals("disablecooldowns") || rule.equals("cooldowns")) {
                setDisableCooldownsRule(target, value, sender);
            } else {
                player.sendMessage(ChatColor.RED + "Unknown gamerule: " + rule);
            }
            return true;
        }
        if (label.equalsIgnoreCase("countink")) {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region region;
            try {
                region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "Please select a region first!");
                return true;
            }

            int totalBlocks = 0;
            Map<String, Integer> teamInkCounts = new HashMap<>();

            for (BlockVector3 blockVector : region) { // Corrected type here
                // You need the Bukkit World to create a Bukkit Location
                // Make sure 'player.getWorld()' refers to org.bukkit.World
                Location bukkitLoc = new Location(player.getWorld(), blockVector.x(), blockVector.y(),
                        blockVector.z());

                if (inkStorage.containsKey(bukkitLoc)) {
                    InkRecord inkRecord = inkStorage.get(bukkitLoc);
                    Color color = inkRecord.getColor();
                    String teamColor = colorToString(color);
                    teamInkCounts.put(teamColor, teamInkCounts.getOrDefault(teamColor, 0) + 1);
                    totalBlocks++;
                }
            }

            player.sendMessage(ChatColor.GREEN + "Ink coverage:");
            for (Map.Entry<String, Integer> entry : teamInkCounts.entrySet()) {
                double percentInk = (totalBlocks > 0) ? ((double) entry.getValue() / totalBlocks) * 100 : 0;
                player.sendMessage(
                        ChatColor.GREEN + "Team " + entry.getKey() + ": " + String.format("%.2f", percentInk) + "%");
            }
            return true;
        }
        if (label.equalsIgnoreCase("debug")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /debug <value>");
                return true;
            }

            try {
                int newValue = Integer.parseInt(args[0]);
                valuestuffs = newValue;
                player.sendMessage(ChatColor.GREEN + "Stuffsss value updated to: " + valuestuffs);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid value! Please enter a valid integer.");
            }
            return true;
        }
        // /game: start a game with the current selection on world edit
        // it first searches for spawn points (team stained glass surrounded by 4
        // team_terracotta)
        if (label.equalsIgnoreCase("game")) {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region region;
            try {
                region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "Please select a region first!");
                return true;
            }
            if (region == null) {
                player.sendMessage(ChatColor.RED + "No region selected!");
                return true;
            }
            // Check if the region is valid
            if (region.getArea() <= 0) {
                player.sendMessage(ChatColor.RED + "Selected region is invalid or empty!");
                return true;
            }
            // check for spawn points of at least 2 teams
            Map<Color, Location> foundTeams = new HashMap<>();
            for (BlockVector3 blockVector : region) {
                // You need the Bukkit World to create a Bukkit Location
                // Make sure 'player.getWorld()' refers to org.bukkit.World
                Location bukkitLoc = new Location(player.getWorld(), blockVector.x(), blockVector.y(),
                        blockVector.z());
                Block block = bukkitLoc.getBlock();
                Material type = block.getType();
                // check for team_stained_glass surrounded by 4 team_terracotta
                // if the block is *_stained glass
                if (type.name().endsWith("_STAINED_GLASS")) {
                    // get the color
                    String color = type.name().replace("_STAINED_GLASS", "").toLowerCase();
                    // if it's not a team
                    if (!getWoolColor(color).name().endsWith("_WOOL")) {
                        continue; // not a team color
                    }
                    Color bukkitColor = getBukkitColorForMaterial(getWoolColor(color));
                    // check if the block is surrounded by 4 team_terracotta blocks
                    boolean surrounded = true;
                    for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST,
                            BlockFace.SOUTH }) {
                        Block relativeBlock = block.getRelative(face);
                        Material relativeType = relativeBlock.getType();
                        if (!relativeType.name().endsWith("_CONCRETE") ||
                                !relativeType.name().toLowerCase().contains(color)) {
                            surrounded = false;
                            break; // Not surrounded by team terracotta
                        }
                    }
                    if (surrounded) {
                        // Add the team color to the found teams
                        foundTeams.put(bukkitColor, bukkitLoc);
                        // send a message to the player
                        Bukkit.getServer()
                                .sendMessage(Component.text("<SplatPlugin> Found spawn point for team: " + color)
                                        .color(TextColor.color(0x00FF00))); // Green color
                    }

                }
            }
            // if we found at least 2 teams, start the game
            if (foundTeams.size() < 2) {
                Bukkit.getServer().sendMessage(
                        Component.text("<SplatPlugin> Not enough teams found! At least 2 teams are required.")
                                .color(TextColor.color(0xFF0000))); // Red color
                return true;
            }
            // Start the game logic here
            // everyone TO SPECTATOR
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.setGameMode(GameMode.SPECTATOR);
                onlinePlayer.sendMessage(ChatColor.YELLOW + "Starting the game...");
            }
            // evenly distribute players to teams
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            int teamCount = foundTeams.size();
            for (int i = 0; i < players.size(); i++) {
                Player playerToAssign = players.get(i);
                Color teamColor = (Color) foundTeams.keySet().toArray()[i % teamCount];
                String teamColorName = colorToString(teamColor);
                getLogger().info("Assigned player " + playerToAssign.getName() + " to team: " + teamColorName);
                playerTeams.put(playerToAssign, teamColorName);
                playerToAssign.sendMessage(ChatColor.GREEN + "You have been assigned to the " + teamColor + " team!");
            }

            // give them a GUI to select what weapons within 30 seconds. disable all others
            // of same type when one is selected

            Inventory weaponSelectionInventory = Bukkit.createInventory(null, 54, Component.text("Select Your Weapon")
                    .color(TextColor.color(0x800080))); // Dark Purple color

            int slot = 0;
            for (Map.Entry<String, Map<String, Object>> entry : weaponList.entrySet()) {
                String weaponId = entry.getKey();
                Map<String, Object> weaponConfig = entry.getValue();

                Material weaponMaterial = Material.BOW; // Default material
                if (weaponConfig.containsKey("weapon")) {
                    try {
                        weaponMaterial = Material.valueOf(weaponConfig.get("weapon").toString().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Fallback to default material
                    }
                }

                ItemStack weaponItem = new ItemStack(weaponMaterial);
                ItemMeta meta = weaponItem.getItemMeta();
                if (meta != null) {
                    if (weaponConfig.containsKey("name")) {
                        meta.setDisplayName(ChatColor.GOLD + weaponConfig.get("name").toString());
                    } else {
                        meta.setDisplayName(ChatColor.GOLD + weaponId);
                    }
                    List<String> lore = new ArrayList<>();
                    if (weaponConfig.containsKey("damage")) {
                        lore.add(ChatColor.RED + "Damage: " + weaponConfig.get("damage"));
                    }
                    if (weaponConfig.containsKey("cost")) {
                        lore.add(ChatColor.BLUE + "Ink Cost: " + weaponConfig.get("cost"));
                    }
                    if (weaponConfig.containsKey("force")) {
                        lore.add(ChatColor.GREEN + "Force: " + weaponConfig.get("force"));
                    }
                    if (weaponConfig.containsKey("cooldown")) {
                        lore.add(ChatColor.YELLOW + "Cooldown: " + weaponConfig.get("cooldown") + "s");
                    }
                    meta.setLore(lore);
                    meta.getPersistentDataContainer().set(splatPluginWeaponKey, PersistentDataType.STRING, weaponId);
                    weaponItem.setItemMeta(meta);
                }

                // Separate weapons and bombs into different sections
                if (weaponConfig.containsKey("type")
                        && weaponConfig.get("type").toString().equalsIgnoreCase("weapon")) {
                    weaponSelectionInventory.setItem(slot++, weaponItem);
                } else if (weaponConfig.containsKey("type")
                        && weaponConfig.get("type").toString().equalsIgnoreCase("bomb")) {
                    // Place bombs in the second half of the inventory
                    int bombSlot = 27 + (slot % 27); // Start bombs at slot 27
                    weaponSelectionInventory.setItem(bombSlot, weaponItem);
                    slot++;
                }
                if (slot >= weaponSelectionInventory.getSize()) {
                    break; // Prevent exceeding inventory size
                }
            }

            // Open the inventory for all players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.openInventory(weaponSelectionInventory);
                onlinePlayer.getInventory().clear();
            }

            // Add an event handler for inventory clicks
            // wait until 30 seconds, then close the inventory for all players
            SplatPlugin plugin = this;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.closeInventory();
                        onlinePlayer.sendMessage(ChatColor.YELLOW + "Weapon selection time is over! Game starting...");
                        boolean hasWeapon = false;
                        boolean hasBomb = false;

                        for (ItemStack item : onlinePlayer.getInventory().getContents()) {
                            if (item != null && item.getItemMeta() != null) {
                                ItemMeta itemMeta = item.getItemMeta();
                                if (itemMeta.getPersistentDataContainer().has(splatPluginWeaponKey,
                                        PersistentDataType.STRING)) {
                                    String weaponId = itemMeta.getPersistentDataContainer().get(splatPluginWeaponKey,
                                            PersistentDataType.STRING);
                                    Map<String, Object> weaponConfig = weaponList.get(weaponId);
                                    if (weaponConfig != null && weaponConfig.containsKey("type")) {
                                        String weaponType = weaponConfig.get("type").toString();
                                        if (weaponType.equalsIgnoreCase("weapon")) {
                                            hasWeapon = true;
                                        } else if (weaponType.equalsIgnoreCase("bomb")) {
                                            hasBomb = true;
                                        }
                                    }
                                }
                            }
                        }

                        if (!hasWeapon) {
                            Map<String, Object> defaultWeaponConfig = weaponList.get("splattershot");
                            if (defaultWeaponConfig != null) {
                                Material weaponMaterial = Material.BOW; // Default material
                                if (defaultWeaponConfig.containsKey("weapon")) {
                                    try {
                                        weaponMaterial = Material
                                                .valueOf(defaultWeaponConfig.get("weapon").toString().toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        // Fallback to default material
                                    }
                                }

                                ItemStack defaultWeapon = new ItemStack(weaponMaterial);
                                ItemMeta meta = defaultWeapon.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.GOLD + defaultWeaponConfig.get("name").toString());
                                    meta.getPersistentDataContainer().set(splatPluginWeaponKey,
                                            PersistentDataType.STRING, "splattershot");
                                    defaultWeapon.setItemMeta(meta);
                                }
                                onlinePlayer.getInventory().addItem(defaultWeapon);
                                onlinePlayer.sendMessage(
                                        ChatColor.GREEN + "You have been given the default weapon: Splattershot.");
                            }
                        }

                        if (!hasBomb) {
                            Map<String, Object> defaultBombConfig = weaponList.get("splatbomb");
                            if (defaultBombConfig != null) {
                                Material bombMaterial = Material.TNT; // Default material
                                if (defaultBombConfig.containsKey("weapon")) {
                                    try {
                                        bombMaterial = Material
                                                .valueOf(defaultBombConfig.get("weapon").toString().toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        // Fallback to default material
                                    }
                                }

                                ItemStack defaultBomb = new ItemStack(bombMaterial);
                                ItemMeta meta = defaultBomb.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.GOLD + defaultBombConfig.get("name").toString());
                                    meta.getPersistentDataContainer().set(splatPluginWeaponKey,
                                            PersistentDataType.STRING, "splatbomb");
                                    defaultBomb.setItemMeta(meta);
                                }
                                onlinePlayer.getInventory().addItem(defaultBomb);
                                onlinePlayer.sendMessage(
                                        ChatColor.GREEN + "You have been given the default bomb: Splatbomb.");
                            }
                        }
                        // set all to adventure
                        onlinePlayer.setGameMode(GameMode.ADVENTURE);
                        // give them their ink tank
                        ItemStack inkTank = new ItemStack(Material.BUCKET);
                        ItemMeta tankMeta = inkTank.getItemMeta();
                        if (tankMeta != null) {
                            tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank 100/100");
                            tankMeta.setLore(
                                    java.util.Arrays.asList(ChatColor.GRAY + "A tank filled with vibrant ink"));
                            tankMeta.setCustomModelData(100); // Custom model data to represent ink storage

                            tankMeta.getPersistentDataContainer().set(splatPluginWeaponKey, PersistentDataType.STRING,
                                    inkTankKey.toString());
                            tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, 100.0); // Initial
                                                                                                                     // ink
                                                                                                                     // amount

                            inkTank.setItemMeta(tankMeta);
                        }
                        onlinePlayer.getInventory().addItem(inkTank);
                        // tp them to their spawn point
                        Location spawnPoint = foundTeams.get(getBukkitColorForMaterial(getWoolColor(
                                getPlayerTeam(onlinePlayer))));
                        if (spawnPoint != null) {
                            onlinePlayer.teleport(spawnPoint);
                            onlinePlayer.sendMessage(ChatColor.GREEN + "You have been teleported to your spawn point.");
                            // Set the spawn point for the player
                            onlinePlayer.setRespawnLocation(spawnPoint.clone().add(0, 1, 0), true);
                        } else {
                            onlinePlayer.sendMessage(ChatColor.RED + "Your spawn point could not be found!");
                        }
                    }

                    new BukkitRunnable() {
                        int timeLeft = 300; // 5 minutes in seconds

                        @Override
                        public void run() {
                            if (timeLeft <= 0) {
                                cancel();
                                Map<String, Integer> teamInkCounts = new HashMap<>();
                                int totalBlocks = 0;

                                for (Location loc : inkStorage.keySet()) {
                                    if (region.contains(
                                            BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))) {
                                        InkRecord record = inkStorage.get(loc);
                                        if (record != null) {
                                            Material woolMaterial = getWoolMaterialForColor(record.getColor());
                                            String team = getInkColorFromWool(woolMaterial);
                                            teamInkCounts.put(team, teamInkCounts.getOrDefault(team, 0) + 1);
                                            totalBlocks++;
                                        }
                                    }
                                }

                                String winningTeam = "none";
                                int maxInkCount = 0;

                                for (Map.Entry<String, Integer> entry : teamInkCounts.entrySet()) {
                                    if (entry.getValue() > maxInkCount) {
                                        maxInkCount = entry.getValue();
                                        winningTeam = entry.getKey();
                                    }
                                }

                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendMessage(ChatColor.YELLOW + "Game Over!");
                                    if (!winningTeam.equals("none")) {
                                        double percentInk = (totalBlocks > 0)
                                                ? ((double) maxInkCount / totalBlocks) * 100
                                                : 0;
                                        player.sendMessage(ChatColor.GREEN + "Winning Team: " + winningTeam + " with " +
                                                String.format("%.2f", percentInk) + "% ink coverage!");
                                    } else {
                                        player.sendMessage(ChatColor.RED + "No team won the game!");
                                    }
                                    player.setGameMode(GameMode.SPECTATOR);
                                }
                            } else {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendMessage(ChatColor.YELLOW + "Time Left: " + timeLeft + " seconds");
                                }
                                timeLeft--;
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 20L); // Run every second
                    this.cancel(); // Stop the task after closing the inventory
                }
            }.runTaskLater(this, 600L); // 600 ticks = 30 seconds

        }
        return false; // If the command is not recognized, return false to indicate failure
    }

    public String colorToString(Color color) {
        if (color == null) {
            return "none"; // Default or error handling for null color
        }

        if (color.equals(Color.BLUE)) {
            return "blue";
        } else if (color.equals(Color.RED)) {
            return "red";
        } else if (color.equals(Color.LIME)) {
            return "lime";
        } else if (color.equals(Color.YELLOW)) {
            return "yellow";
        } else if (color.equals(Color.BLACK)) {
            return "black";
        } else if (color.equals(Color.WHITE)) {
            return "white";
        } else if (color.equals(Color.ORANGE)) {
            return "orange";
        } else if (color.equals(Color.FUCHSIA)) { // Bukkit's representation of Magenta
            return "magenta";
        } else if (color.equals(Color.AQUA)) { // Bukkit's representation of Light Blue
            return "light_blue";
        } else if (color.equals(Color.PURPLE)) {
            return "purple";
        } else if (color.equals(Color.GRAY)) {
            return "gray";
        } else if (color.equals(Color.SILVER)) { // Bukkit's representation of Light Gray
            return "light_gray";
        } else if (color.equals(Color.TEAL)) { // Bukkit's representation of Cyan
            return "cyan";
        } else if (color.equals(Color.GREEN)) {
            return "green";
        } else if (color.equals(Color.fromRGB(139, 69, 19))) { // Custom RGB for Brown
            return "brown";
        }

        return "none"; // Fallback for unmapped or unexpected color
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

    // Player Shooting Ink
    private void ShootWeapon(Player player, ItemStack item, String weapon, PlayerInteractEvent event) {
        // if player is not on a team, return
        String team = getPlayerTeam(player);
        if (team.equalsIgnoreCase("none")) {
            player.sendMessage(ChatColor.RED + "You must join a team to use this weapon!");
            return;
        }
        // find the tank
        ItemStack inkTank = null;
        for (ItemStack tank : player.getInventory().getContents()) {
            if (tank != null && tank.getItemMeta() != null
                    && tank.getItemMeta().getPersistentDataContainer().has(inkTankKey, PersistentDataType.DOUBLE)) {
                inkTank = tank;
                break;
            }
        }
        if (inkTank == null) {
            player.sendMessage(ChatColor.RED + "You need an Ink Tank to use this weapon!");
            return;
        }
        // Get the weapon configuration from the config-loaded map
        Map<String, Object> weaponConfig = weaponList.get(weapon);
        if (weaponConfig == null) {
            player.sendMessage(ChatColor.RED + "Weapon configuration not found!");
            return;
        }
        // cooldown
        long currentTime = System.currentTimeMillis();

        // Retrieve cost and force values from the configuration
        double cost = 10.0; // default cost
        if (weaponConfig.containsKey("cost")) {
            cost = ((Number) weaponConfig.get("cost")).doubleValue();
        }
        float force = 1.5f; // default force
        if (weaponConfig.containsKey("force")) {
            force = ((Number) weaponConfig.get("force")).floatValue();
        }
        String ink = splatterInkKey.toString();
        if (weaponConfig.containsKey("ink")) {
            ink = ((String) weaponConfig.get("ink"));
        }
        // check if the player has enough ink in the tank
        ItemMeta tankMeta = inkTank.getItemMeta();
        if (tankMeta == null || !tankMeta.getPersistentDataContainer().has(inkTankKey, PersistentDataType.DOUBLE)) {
            player.sendMessage(ChatColor.RED + "Your Ink Tank is empty!");
            return;
        }
        double inkAmount = tankMeta.getPersistentDataContainer().get(inkTankKey, PersistentDataType.DOUBLE);
        if (weapon.equalsIgnoreCase("splatterscope") && !player.isSneaking()) {
            cost = 0; // Set cost to 0 if the weapon is splatterscope and the player is not sneaking
        }
        if (player.isSneaking() && !(weapon.equalsIgnoreCase("splatterscope")))
            return;
        if (inkAmount < cost) {
            // play a sound for insufficient ink
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }
        double cooldownSeconds = 0.3;
        if (weaponConfig.containsKey("cooldown")) {
            cooldownSeconds = ((Number) weaponConfig.get("cooldown")).doubleValue();
        }
        long cooldownMillis = (long) (cooldownSeconds * 1000);
        String cooldownKey = player.getUniqueId().toString() + ":" + weapon;
        if (!isCooldownDisabled(player) && weaponCooldowns.containsKey(cooldownKey)) {
            long lastUsed = weaponCooldowns.get(cooldownKey);
            if (weapon.equalsIgnoreCase("splatterscope")) {
                cooldownMillis = 2000;
            }
            if (currentTime - lastUsed < cooldownMillis) {
                if (!Boolean.TRUE.equals(weaponConfig.get("auto"))) {
                    player.sendMessage(ChatColor.RED + "Weapon is cooling down!");
                }
                return;
            }
        }
        weaponCooldowns.put(cooldownKey, currentTime);
        // reduce the ink amount in the tank and update its display name
        inkAmount -= cost;
        tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, inkAmount);
        tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank " + (int) inkAmount + "/100");
        inkTank.setItemMeta(tankMeta);
        if (weapon.equalsIgnoreCase("inkbrush")) {
            // Push the player forward
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 1));

            // Leave a narrow ink trail along the ground using inkBlock
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || ticks++ > 10) {
                        cancel();
                        return;
                    }
                    Location start = player.getLocation().clone();
                    Snowball brushInk = player.getWorld().spawn(start, Snowball.class);
                    // Calculate one block behind the player (ignoring vertical direction)
                    Location spawnLoc = player.getLocation().clone()
                            .subtract(player.getLocation().getDirection().setY(0).normalize());
                    // Move the snowball to that location
                    brushInk.teleport(spawnLoc);
                    // Set its velocity to go straight down
                    brushInk.setVelocity(new Vector(0, -0.1, 0));
                    brushInk.setCustomName("brushink");
                    brushInk.setCustomNameVisible(false);
                    // set owner
                    brushInk.setShooter(player);
                }
            }.runTaskTimer(this, 0L, 1L);

            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
            return;
        }
        if (weapon.equalsIgnoreCase("splatterscope")) {
            if (player.isSneaking()) {
                String playerName = player.getName().toLowerCase();
                int currentCharges = chargesScope.getOrDefault(playerName, 0).intValue();
                if (currentCharges < 5) {
                    chargesScope.put(playerName, currentCharges + 1);
                    player.sendMessage(
                            ChatColor.GREEN + "Your charges have been increased to: " + (currentCharges + 1));
                    cooldownSeconds = 2.0;
                } else {
                    player.sendMessage(ChatColor.RED + "You already have the maximum charges (5).");
                }
            } else {
                // if no charges no shoot
                String playerName = player.getName().toLowerCase();
                int currentCharges = chargesScope.getOrDefault(playerName, 0).intValue();
                if (currentCharges <= 0) {
                    player.sendMessage(
                            ChatColor.RED + "You have no charges left! Sneak and right click to gain charges.");
                    return;
                }
                // Decrease the charges by charges
                chargesScope.put(playerName, 0);
                Snowball scopeRound = player.launchProjectile(Snowball.class,
                        player.getLocation().getDirection().clone().normalize()
                                .multiply(force + (currentCharges * 0.5f)));
                scopeRound.setCustomName(ink);
                scopeRound.setCustomNameVisible(false);
                scopeRound.setShooter(player);
                new BukkitRunnable() {
                    Location loc = scopeRound.getLocation().clone();

                    @Override
                    public void run() {
                        if (scopeRound.isValid()) {
                            loc = scopeRound.getLocation().clone();
                            // drip ink
                            if (loc.getBlock().getType() == Material.AIR) {
                                // Spawn a snowball at the location to simulate ink drip
                                Snowball dripInk = loc.getWorld().spawn(loc, Snowball.class);
                                dripInk.setCustomName("dripink");
                                dripInk.setCustomNameVisible(false);
                                dripInk.setShooter(scopeRound.getShooter());
                                // Set the velocity to go straight down
                                dripInk.setVelocity(new Vector(0, -0.1, 0).add(scopeRound.getVelocity().multiply(0.5)));
                            }

                        } else {
                            for (int i = 0; i < 4; i++) {
                                Snowball dripInk = loc.getWorld().spawn(loc, Snowball.class);
                                dripInk.setCustomName("dripink");
                                dripInk.setCustomNameVisible(false);
                                dripInk.setShooter(scopeRound.getShooter());
                                dripInk.setVelocity(new Vector(0, -0.1, 0));
                            }
                            cancel(); // Stop the task if the projectile is no longer valid
                        }
                    }
                }.runTaskTimer(this, 0L, 1L);
            }
        }
        if (weapon.equalsIgnoreCase("defaultblaster")) {
            Snowball blasterRound = player.launchProjectile(Snowball.class,
                    player.getLocation().getDirection().clone().normalize().multiply(force));
            blasterRound.setCustomName("blasterink");
            blasterRound.setCustomNameVisible(false);
            blasterRound.setShooter(player);
            final Location launchLoc = blasterRound.getLocation().clone();

            new BukkitRunnable() {
                Location lastLocation = blasterRound.getLocation().clone();

                @Override
                public void run() {
                    if (blasterRound.isValid()) {
                        lastLocation = blasterRound.getLocation().clone();
                    }
                    // Detonate the round after it travels 6 blocks
                    if (lastLocation.distance(launchLoc) >= 10 || !blasterRound.isValid()) {
                        Location burstLoc = lastLocation.clone();
                        // Burst: spawn multiple ink fragments in a spherical pattern
                        // Define how many ink fragments you want in the random sphere.
                        // The previous fixed grid generated 124 fragments, which is a lot.
                        // Start with a lower number and adjust for desired density and performance.
                        int numberOfFragments = 60; // You can adjust this number (e.g., 20 to 60)

                        // Define the speed at which each fragment will be launched.
                        float fragmentSpeed = 0.5f; // Matches your original speed

                        for (int i = 0; i < numberOfFragments; i++) {
                            // Generate random components for the vector.
                            // We want values typically between -1.0 and 1.0, which will then be normalized.
                            double offsetX = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetY = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetZ = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);

                            Vector randomDirection = new Vector(offsetX, offsetY, offsetZ);

                            // It's possible (though rare) to get a zero vector if all offsets are 0.
                            // Normalizing a zero vector causes an error. Add a check.
                            if (randomDirection.lengthSquared() == 0) {
                                // If it's a zero vector, give it a tiny upward nudge to avoid errors.
                                randomDirection = new Vector(0, 0.1, 0);
                            }

                            // Normalize the vector to get a unit direction, then scale by desired speed.
                            randomDirection.normalize().multiply(fragmentSpeed);

                            Snowball burstInk = burstLoc.getWorld().spawn(burstLoc, Snowball.class);
                            burstInk.setVelocity(randomDirection);
                            burstInk.setCustomName("blasterink");
                            burstInk.setCustomNameVisible(false);
                            burstInk.setShooter(player);
                        }
                        burstLoc.getWorld().playSound(burstLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                        try {
                            blasterRound.remove();
                        } finally {
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(this, 1L, 1L);
            return;
        }
        if (weapon.equalsIgnoreCase("suctionbomb")) {
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(1.5f);
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setVelocity(direction);
            tnt.setFuseTicks(823450);
            tnt.setCustomName(weapon);
            tnt.setCustomNameVisible(true);
            tnt.setSource(player);
            new BukkitRunnable() {
                int tickCounter = 0;
                int interval = 20; // starting beep interval in ticks
                int fuseTicks = 40;
                int nextBeepTick = interval;
                boolean isSurrounded = false;

                @Override
                public void run() {
                    Block block = tnt.getLocation().getBlock();

                    for (double offsetX = -0.1; offsetX <= 0.1; offsetX += 0.1) {
                        for (double offsetY = -0.1; offsetY <= 0.1; offsetY += 0.1) {
                            for (double offsetZ = -0.1; offsetZ <= 0.1; offsetZ += 0.1) {
                                Location newLoc = block.getLocation().clone().add(offsetX, offsetY, offsetZ);
                                if (!newLoc.getBlock().isPassable()) {
                                    isSurrounded = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (isSurrounded) {
                        tnt.setVelocity(new Vector(0, 0.1, 0)); // Freeze the TNT
                        tickCounter++;
                    }
                    if (tickCounter >= fuseTicks) {
                        int numberOfFragments = 100; // Adjust this number (e.g., from 50 to 150)

                        // Define the speed at which each fragment will be launched.
                        // The original SplatBomb code used 1.0f, so we'll maintain that.
                        float fragmentSpeed = 1.1f;

                        for (int i = 0; i < numberOfFragments; i++) {
                            // Generate random components for the vector in range [-1.0, 1.0]
                            double offsetX = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetY = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetZ = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            Location burstLoc = tnt.getLocation();
                            Vector randomDirection = new Vector(offsetX, offsetY, offsetZ);

                            // Ensure the vector is not zero (as normalize() on a zero vector causes an
                            // error)
                            if (randomDirection.lengthSquared() == 0) {
                                randomDirection = new Vector(0, 0.1, 0); // Give it a slight upward nudge
                            }

                            // Normalize the vector to get a unit direction, then scale by desired speed.
                            randomDirection.normalize().multiply(fragmentSpeed);

                            Snowball ball = tnt.getWorld().spawn(burstLoc, Snowball.class); // Spawn at the TNT's
                                                                                            // location
                            ball.setVelocity(randomDirection);
                            ball.setCustomName("triangleink"); // Maintain the SplatBomb's unique custom name
                            ball.setCustomNameVisible(false);
                            ball.setShooter((ProjectileSource) tnt.getSource()); // Keep the original shooter
                        }
                        tnt.remove();
                        cancel();
                        return; // is
                    }
                    double secondsRemaining = (fuseTicks - tickCounter) / 20.0;
                    tnt.setCustomName(String.format("%.1f", secondsRemaining));
                    if (tickCounter >= nextBeepTick) {
                        tnt.getWorld().playSound(tnt.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0f, 1.0f);
                        interval = interval / 2;
                        nextBeepTick = tickCounter + interval;
                    }
                }
            }.runTaskTimer(this, 1L, 1L);
        }

        // nuclear bomb: what the fuck
        if (weapon.equalsIgnoreCase("nuclearbomb")) {
            // oh god
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(1.5f);
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setVelocity(direction);
            tnt.setFuseTicks(823450);
            tnt.setCustomName("EVERYBODY RUN OH GOD IT'S A NUCLEAR BOMB");
            tnt.setCustomNameVisible(true);
            tnt.setSource(player);
            new BukkitRunnable() {
                int tickCounter = 0;
                int interval = 20; // starting beep interval in ticks
                int fuseTicks = 40;
                int nextBeepTick = interval;
                boolean isSurrounded = false;

                @Override
                public void run() {
                    Block block = tnt.getLocation().getBlock();

                    for (double offsetX = -0.1; offsetX <= 0.1; offsetX += 0.1) {
                        for (double offsetY = -0.1; offsetY <= 0.1; offsetY += 0.1) {
                            for (double offsetZ = -0.1; offsetZ <= 0.1; offsetZ += 0.1) {
                                Location newLoc = block.getLocation().clone().add(offsetX, offsetY, offsetZ);
                                if (!newLoc.getBlock().isPassable()) {
                                    isSurrounded = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isSurrounded) {
                        int numberOfFragments = 1000; // Adjust this number (e.g., from 50 to 150)

                        // Define the speed at which each fragment will be launched.
                        // The original SplatBomb code used 1.0f, so we'll maintain that.
                        float fragmentSpeed = 1.8f;

                        for (int i = 0; i < numberOfFragments; i++) {
                            // Generate evenly distributed points on a sphere using spherical coordinates
                            double theta = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI); // Angle around the
                                                                                                   // sphere
                            double phi = Math.acos(ThreadLocalRandom.current().nextDouble(-1, 1)); // Angle from the
                                                                                                   // vertical axis

                            // Convert spherical coordinates to Cartesian coordinates
                            double offsetX = Math.sin(phi) * Math.cos(theta);
                            double offsetY = Math.sin(phi) * Math.sin(theta);
                            double offsetZ = Math.cos(phi);

                            Location burstLoc = tnt.getLocation();
                            Vector randomDirection = new Vector(offsetX, offsetY, offsetZ);

                            // Normalize the vector to get a unit direction, then scale by desired speed.
                            randomDirection.normalize().multiply(fragmentSpeed);

                            Snowball ball = tnt.getWorld().spawn(burstLoc, Snowball.class); // Spawn at the TNT's
                                                                                            // location
                            ball.setVelocity(randomDirection);
                            ball.setCustomName("nuclearink"); // Maintain the SplatBomb's unique custom name
                            ball.setCustomNameVisible(false);
                            ball.setShooter((ProjectileSource) tnt.getSource()); // Keep the original shooter
                        }
                        tnt.remove();
                        cancel();
                        return; // is
                    }
                }
            }.runTaskTimer(this, 1L, 1L);
        }
        // burst bomb: like suction bomb but explodes on touch
        if (weapon.equalsIgnoreCase("burstbomb")) {
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(1.5f);
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setVelocity(direction);
            tnt.setFuseTicks(823450);
            tnt.setCustomName("Burst Bomb");
            tnt.setCustomNameVisible(true);
            tnt.setSource(player);
            new BukkitRunnable() {
                int tickCounter = 0;
                int interval = 20; // starting beep interval in ticks
                int fuseTicks = 40;
                int nextBeepTick = interval;
                boolean isSurrounded = false;

                @Override
                public void run() {
                    Block block = tnt.getLocation().getBlock();

                    for (double offsetX = -0.1; offsetX <= 0.1; offsetX += 0.1) {
                        for (double offsetY = -0.1; offsetY <= 0.1; offsetY += 0.1) {
                            for (double offsetZ = -0.1; offsetZ <= 0.1; offsetZ += 0.1) {
                                Location newLoc = block.getLocation().clone().add(offsetX, offsetY, offsetZ);
                                if (!newLoc.getBlock().isPassable()) {
                                    isSurrounded = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isSurrounded) {
                        int numberOfFragments = 100; // Adjust this number (e.g., from 50 to 150)

                        // Define the speed at which each fragment will be launched.
                        // The original SplatBomb code used 1.0f, so we'll maintain that.
                        float fragmentSpeed = 1.0f;

                        for (int i = 0; i < numberOfFragments; i++) {
                            // Generate random components for the vector in range [-1.0, 1.0]
                            double offsetX = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetY = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetZ = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            Location burstLoc = tnt.getLocation();
                            Vector randomDirection = new Vector(offsetX, offsetY, offsetZ);

                            // Ensure the vector is not zero (as normalize() on a zero vector causes an
                            // error)
                            if (randomDirection.lengthSquared() == 0) {
                                randomDirection = new Vector(0, 0.1, 0); // Give it a slight upward nudge
                            }

                            // Normalize the vector to get a unit direction, then scale by desired speed.
                            randomDirection.normalize().multiply(fragmentSpeed);

                            Snowball ball = tnt.getWorld().spawn(burstLoc, Snowball.class); // Spawn at the TNT's
                                                                                            // location
                            ball.setVelocity(randomDirection);
                            ball.setCustomName("triangleink"); // Maintain the SplatBomb's unique custom name
                            ball.setCustomNameVisible(false);
                            ball.setShooter((ProjectileSource) tnt.getSource()); // Keep the original shooter
                        }
                        tnt.remove();
                        cancel();
                        return; // is
                    }
                }
            }.runTaskTimer(this, 1L, 1L);
        }
        // Splat Roller!
        if (weapon.equalsIgnoreCase("splatroller")) {
            // Set a task to create an ink trail behind the roller
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || ticks++ > 10) {
                        cancel();
                        return;
                    }
                    Location start = player.getLocation().clone();
                    Vector direction = start.getDirection().normalize();
                    for (double offsetX = -1.5; offsetX <= 1.5; offsetX += 0.3) {
                        Location spawnLoc = start.clone().add(direction.clone().multiply(1)).add(offsetX, 0, 0);
                        Snowball rollerInk = player.getWorld().spawn(spawnLoc, Snowball.class);
                        rollerInk.setVelocity(new Vector(0, -0.1, 0));
                        rollerInk.setCustomName("rollerink");
                        rollerInk.setCustomNameVisible(false);
                        rollerInk.setShooter(player);
                    }
                }
            }.runTaskTimer(this, 0L, 1L);
        }
        // Ink Sprinkler: sprinkle ink around this location
        // works like suction bomb but spawns ink like a sprinkler while not destroyed.
        // is a breeze
        if (weapon.equalsIgnoreCase("inksprinkler")) {
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setFuseTicks(823450); // Set fuse duration (80 ticks = 4 seconds)
            tnt.setCustomName(weapon);
            tnt.setCustomNameVisible(true);
            tnt.setSource(player); // set the owner so the ink will inking
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!tnt.isValid()) {
                        cancel();
                        return;
                    }
                    if (!tnt.isOnGround())
                        return;

                    // Replace TNT with Breeze entity
                    Breeze breeze = tnt.getWorld().spawn(tnt.getLocation(), Breeze.class);
                    breeze.setAI(false); // Disable AI
                    breeze.setCustomName(player.getName() + "'s Ink Sprinkler");
                    breeze.setCustomNameVisible(true);

                    new BukkitRunnable() {
                        int rotationAngle = 0;

                        @Override
                        public void run() {
                            if (!breeze.isValid() || player.isDead() || !player.isOnline() || breeze.isDead()) {
                                cancel();
                                return;
                            }

                            // Rotate the Breeze entity
                            rotationAngle = (rotationAngle + 10) % 360;
                            breeze.teleport(breeze.getLocation()
                                    .setDirection(new Vector(Math.cos(Math.toRadians(rotationAngle)), 0,
                                            Math.sin(Math.toRadians(rotationAngle)))));

                            // spew ink from left and right
                            // like ears spewing ink

                        }
                    }.runTaskTimerAsynchronously(SplatPlugin.this, 0L, 1L);
                    tnt.remove();
                    cancel();
                    return;
                }
            }.runTaskTimer(this, 0L, 1L);

        }
        // Launch ink projectiles according to weapon type
        if (weapon.equalsIgnoreCase("splatbomb")) {
            // Trianglesub: Shoot a TNT that travels ~6 blocks away.
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(1.5f);
            // Spawn a primed TNT slightly above the player's location
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setVelocity(direction);
            tnt.setFuseTicks(823450); // Set fuse duration (80 ticks = 4 seconds)
            tnt.setCustomName(weapon);
            tnt.setCustomNameVisible(true);
            // set the owner so the ink will inking
            tnt.setSource(player);
            new BukkitRunnable() {
                int tickCounter = 0;
                int interval = 20; // starting beep interval in ticks
                int fuseTicks = 40;
                int nextBeepTick = interval;

                @Override
                public void run() {
                    if (tnt.isOnGround()) {
                        tickCounter++;
                    }
                    if (tickCounter >= fuseTicks) {
                        int numberOfFragments = 80; // Adjust this number (e.g., from 50 to 150)

                        // Define the speed at which each fragment will be launched.
                        // The original SplatBomb code used 1.0f, so we'll maintain that.
                        float fragmentSpeed = 1.0f;

                        for (int i = 0; i < numberOfFragments; i++) {
                            // Generate random components for the vector in range [-1.0, 1.0]
                            double offsetX = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetY = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            double offsetZ = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                            Location burstLoc = tnt.getLocation();
                            Vector randomDirection = new Vector(offsetX, offsetY, offsetZ);

                            // Ensure the vector is not zero (as normalize() on a zero vector causes an
                            // error)
                            if (randomDirection.lengthSquared() == 0) {
                                randomDirection = new Vector(0, 0.1, 0); // Give it a slight upward nudge
                            }

                            // Normalize the vector to get a unit direction, then scale by desired speed.
                            randomDirection.normalize().multiply(fragmentSpeed);

                            Snowball ball = tnt.getWorld().spawn(burstLoc, Snowball.class); // Spawn at the TNT's
                                                                                            // location
                            ball.setVelocity(randomDirection);
                            ball.setCustomName("triangleink"); // Maintain the SplatBomb's unique custom name
                            ball.setCustomNameVisible(false);
                            ball.setShooter((ProjectileSource) tnt.getSource()); // Keep the original shooter
                        }
                        tnt.remove();
                        cancel();
                        return; // is this?? yes
                    }
                    double secondsRemaining = (fuseTicks - tickCounter) / 20.0;
                    tnt.setCustomName(String.format("%.1f", secondsRemaining));
                    if (tickCounter >= nextBeepTick) {
                        tnt.getWorld().playSound(tnt.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0f, 1.0f);
                        interval = interval / 2;
                        nextBeepTick = tickCounter + interval;
                    }
                }
            }.runTaskTimer(SplatPlugin.this, 1L, 1L);

        } else if (weapon.equalsIgnoreCase("splattershot") || weapon.equalsIgnoreCase("debugweapon")) {
            // Splatter / Debug Weapon: Launch 3x3x3 balls with slight random variations
            for (

            double offsetX : new double[] { -0.01, 0, 0.01 }) {
                for (double offsetY : new double[] { -0.01, 0, 0.01 }) {
                    for (double offsetZ : new double[] { -0.01, 0, 0.01 }) {
                        Vector direction = player.getLocation().getDirection().clone();
                        double randomOffsetX = (Math.random() * 0.02) - 0.01;
                        double randomOffsetY = (Math.random() * 0.02) - 0.01;
                        double randomOffsetZ = (Math.random() * 0.02)
                                - 0.01;
                        direction.add(
                                new Vector(offsetX + randomOffsetX, offsetY + randomOffsetY, offsetZ + randomOffsetZ))
                                .normalize().multiply(force);
                        Snowball inkShot = player.launchProjectile(Snowball.class,
                                direction);
                        inkShot.setCustomName(ink);
                        inkShot.setCustomNameVisible(false);
                        inkShot.setShooter(player);
                    }
                }
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);

    }

    @EventHandler
    public void onPlayerShootInk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        // debugging! send isSplatPluginWeapon(item) to player
        String splatterWeapon = isSplatPluginWeapon(item);
        // if it didn't return null, then it is a splat plugin weapon
        if (splatterWeapon == null)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ShootWeapon(player, item, splatterWeapon, event);
        event.setCancelled(true);
    }

    private void unink(Location loc) {
        // 1. Remove the InkRecord from the main storage
        // Use the exact location object from the map if possible to ensure removal.
        // If loc is always a new clone, ensure .equals and .hashCode are correctly
        // implemented for Location
        // or just use loc directly.
        InkRecord record = inkStorage.remove(loc); // Remove the record based on the exact location

        // 2. If a record was found (meaning it was actually inked), revert it for all
        // online players
        if (record != null) {
            // Get the actual BlockData of the block in the world.
            // This is important because the world block might have been changed by
            // something else
            // or might have reverted due to distance from a player.
            // It's safer to send the *original* data we stored for the ink record.
            BlockData originalBlockData = loc.getBlock().getBlockData();

            // Iterate through all online players to revert the client-side display
            for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                // Only send the change if the player is in the same world as the block
                if (onlinePlayer.getWorld().equals(loc.getWorld())) {
                    // Send the original block data to the player's client.
                    // This makes them see the block as it was before it was inked.
                    onlinePlayer.sendBlockChange(loc, originalBlockData);
                }
            }
            // If the actual server block was temporarily changed to an ink block (which it
            // shouldn't be now
            // with client-side changes, but good for robustness), revert it too:
            // loc.getBlock().setBlockData(originalBlockData); // This line is not needed
            // with client-side only
        }
    }

    private static BlockData transferGlassPaneProperties(BlockState originalBlockState, Material newMaterial) {
        // Basic validation: Ensure inputs are not null
        if (originalBlockState == null) {
            throw new IllegalArgumentException("Original block state cannot be null.");
        }
        if (newMaterial == null) {
            throw new IllegalArgumentException("New material cannot be null.");
        }

        // Get the BlockData from the original BlockState
        BlockData currentBlockData = originalBlockState.getBlockData();

        // Create a new BlockData object for the desired new material.
        // This will initially have the default properties for the new material.
        BlockData newBlockData = newMaterial.createBlockData();

        // Verify that both the original block data and the new material's data
        // are indeed instances of GlassPane. This is crucial for property transfer.
        if (!(currentBlockData instanceof GlassPane) || !(newBlockData instanceof GlassPane)) {
            // Log a warning or handle the error appropriately if types don't match.
            // For simplicity, we'll return the default newBlockData.
            System.out.println("Warning: Original block data or new material is not a GlassPane type. " +
                    "Cannot transfer pane-specific properties. Returning default new BlockData.");
            return newBlockData;
        }

        // Cast both BlockData objects to the GlassPane specific type
        GlassPane currentPaneData = (GlassPane) currentBlockData;
        GlassPane newPaneData = (GlassPane) newBlockData;

        // --- Transfer GlassPane specific properties ---

        // 1. Transfer connection states (east, west, north, south, up)
        // Iterate through all possible faces that a GlassPane can connect to.
        // Glass panes automatically determine their 'pillar' shape based on surrounding
        // connections.
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
            // Check if the original pane has a connection to a block in this direction
            boolean isConnected = currentPaneData.hasFace(face);
            // Set the same connection state for the new pane
            newPaneData.setFace(face, isConnected);
        }

        // 2. Transfer the 'waterlogged' property (if the pane is submerged)
        newPaneData.setWaterlogged(currentPaneData.isWaterlogged());

        // NBT Data Note:
        // This function specifically handles Bukkit's BlockData properties.
        // For blocks that are also TileEntities (like chests, furnaces, signs, etc.)
        // and store custom NBT data beyond their basic BlockData, this method will NOT
        // automatically transfer that NBT. Glass panes are not TileEntities and
        // typically do not have complex NBT data, so this approach is suitable for
        // them.

        return newPaneData;
    }

    /**
     * A smaller, more easily integratable function to "kill the pain" of changing
     * glass pane types by preserving their connection properties.
     * This is a wrapper around the more detailed property transfer logic.
     *
     * @param originalState  The BlockState of the original glass pane.
     * @param targetMaterial The new Material for the glass pane (must be a stained
     *                       glass pane).
     * @return A new BlockData instance with the target material and transferred
     *         properties,
     *         or the default BlockData for the target material if the operation
     *         fails.
     * @throws IllegalArgumentException if originalState or targetMaterial are null.
     */
    public static BlockData painKillerPills(BlockState originalState, Material targetMaterial) {
        return transferGlassPaneProperties(originalState, targetMaterial);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation().clone().add(0, -1, 0); // Check the block below the player
        String team = getPlayerTeam(player);

        if (team.equalsIgnoreCase("none")) {
            return; // Player is not part of any team
        }

        // Check if team ink is nearby within a small radius
        boolean nearbyTeamInk = false;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location nearbyLoc = loc.clone().add(x, y, z);
                    if (isTeamInk(nearbyLoc, team)) {
                        nearbyTeamInk = true;
                        break;
                    }
                }
            }
        }

        if (nearbyTeamInk) {
            player.setFlying(false); // Prevent flying state
            player.setFallDistance(0); // Reset fall distance to avoid kick
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            Player player = (Player) event.getEntity();
            Location loc = player.getLocation().clone().add(0, -1, 0); // Check the block below the player
            String team = getPlayerTeam(player);

            if (team.equalsIgnoreCase("none")) {
                return; // Player is not part of any team
            }

            if (isTeamInk(loc, team)) {
                event.setCancelled(true); // Cancel fall damage if landed on team ink
            }
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getView().getTitle().equals(Component.text("Select Your Weapon").color(TextColor.color(0x800080)))) {
            event.setCancelled(true); // Prevent players from taking items out of the inventory
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                ItemStack clickedItem = event.getCurrentItem();
                ItemMeta meta = clickedItem.getItemMeta();

                if (meta.getPersistentDataContainer().has(splatPluginWeaponKey,
                        PersistentDataType.STRING)) {
                    String weaponId = meta.getPersistentDataContainer().get(splatPluginWeaponKey,
                            PersistentDataType.STRING);

                    // Check if the player already selected a weapon of the same type
                    Map<String, Object> weaponConfig = weaponList.get(weaponId);
                    if (weaponConfig != null && weaponConfig.containsKey("type")) {
                        String weaponType = weaponConfig.get("type").toString();
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getItemMeta() != null) {
                                ItemMeta itemMeta = item.getItemMeta();
                                if (itemMeta.getPersistentDataContainer().has(splatPluginWeaponKey,
                                        PersistentDataType.STRING)) {
                                    String existingWeaponId = itemMeta.getPersistentDataContainer()
                                            .get(splatPluginWeaponKey, PersistentDataType.STRING);
                                    Map<String, Object> existingWeaponConfig = weaponList
                                            .get(existingWeaponId);
                                    if (existingWeaponConfig != null
                                            && existingWeaponConfig.containsKey("type") &&
                                            existingWeaponConfig.get("type").toString()
                                                    .equals(weaponType)) {
                                        player.sendMessage(ChatColor.RED + "You have already selected a "
                                                + weaponType + " weapon!");
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    // Give the selected weapon to the player
                    player.getInventory().addItem(clickedItem.clone());
                    player.sendMessage(ChatColor.GREEN + "You have selected the weapon: " + weaponId);

                    // Ensure the player has a bomb and a weapon
                    boolean hasBomb = false;
                    boolean hasWeapon = false;

                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getItemMeta() != null) {
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta.getPersistentDataContainer().has(splatPluginWeaponKey,
                                    PersistentDataType.STRING)) {
                                String existingWeaponId = itemMeta.getPersistentDataContainer()
                                        .get(splatPluginWeaponKey, PersistentDataType.STRING);
                                Map<String, Object> existingWeaponConfig = weaponList.get(existingWeaponId);
                                if (existingWeaponConfig != null
                                        && existingWeaponConfig.containsKey("type")) {
                                    String weaponType = existingWeaponConfig.get("type").toString();
                                    if (weaponType.equalsIgnoreCase("weapon")) {
                                        hasWeapon = true;
                                    } else if (weaponType.equalsIgnoreCase("bomb")) {
                                        hasBomb = true;
                                    }
                                }
                            }
                        }
                    }

                    if (!hasWeapon || !hasBomb) {
                        player.sendMessage(ChatColor.RED + "You must select both a weapon and a bomb!");
                        return;
                    }

                    // Close the inventory
                    player.closeInventory();
                }
            }
        }
    }

    public Color getBukkitColorForMaterial(Material inkColorMaterial) {
        switch (inkColorMaterial) {
            case BLUE_CONCRETE:
            case BLUE_WOOL: // If you use wool for team colors
                return Color.BLUE;
            case RED_CONCRETE:
            case RED_WOOL:
                return Color.RED;
            case LIME_CONCRETE:
            case LIME_WOOL:
                return Color.LIME;
            case YELLOW_CONCRETE:
            case YELLOW_WOOL:
                return Color.YELLOW;
            case BLACK_CONCRETE:
            case BLACK_WOOL:
                return Color.BLACK;
            case WHITE_CONCRETE:
            case WHITE_WOOL:
                return Color.WHITE;
            case ORANGE_CONCRETE:
            case ORANGE_WOOL:
                return Color.ORANGE;
            case MAGENTA_CONCRETE:
            case MAGENTA_WOOL:
                return Color.FUCHSIA; // Closest Bukkit Color to Magenta
            case LIGHT_BLUE_CONCRETE:
            case LIGHT_BLUE_WOOL:
                return Color.AQUA; // Closest Bukkit Color to Light Blue
            case PURPLE_CONCRETE:
            case PURPLE_WOOL:
                return Color.PURPLE;
            case PINK_CONCRETE:
            case PINK_WOOL:
                return Color.FUCHSIA;
            case GRAY_CONCRETE:
            case GRAY_WOOL:
                return Color.GRAY;
            case LIGHT_GRAY_CONCRETE:
            case LIGHT_GRAY_WOOL:
                return Color.SILVER; // Closest Bukkit Color to Light Gray
            case CYAN_CONCRETE:
            case CYAN_WOOL:
                return Color.TEAL; // Closest Bukkit Color to Cyan
            case BROWN_CONCRETE:
            case BROWN_WOOL:
                return Color.fromRGB(139, 69, 19); // Custom RGB for Brown
            case GREEN_CONCRETE:
            case GREEN_WOOL:
                return Color.GREEN;
            default:
                // Fallback for any unmapped or unexpected material.
                // This ensures a Color object is *always* returned, preventing `dustOptions`
                // from being null.
                return Color.WHITE;
        }
    }

    public Material getWoolMaterialForColor(Color color) {
        if (color == null) {
            return Material.WHITE_WOOL; // Default or error handling for null color
        }

        // Direct comparisons for Bukkit's predefined Color constants
        if (color.equals(Color.BLUE)) {
            return Material.BLUE_WOOL;
        } else if (color.equals(Color.RED)) {
            return Material.RED_WOOL;
        } else if (color.equals(Color.LIME)) {
            return Material.LIME_WOOL;
        } else if (color.equals(Color.YELLOW)) {
            return Material.YELLOW_WOOL;
        } else if (color.equals(Color.BLACK)) {
            return Material.BLACK_WOOL;
        } else if (color.equals(Color.WHITE)) {
            return Material.WHITE_WOOL;
        } else if (color.equals(Color.ORANGE)) {
            return Material.ORANGE_WOOL;
        } else if (color.equals(Color.FUCHSIA)) { // Bukkit's representation of Magenta
            return Material.MAGENTA_WOOL;
        } else if (color.equals(Color.AQUA)) { // Bukkit's representation of Light Blue
            return Material.LIGHT_BLUE_WOOL;
        } else if (color.equals(Color.PURPLE)) {
            return Material.PURPLE_WOOL;
        } else if (color.equals(Color.FUCHSIA)) {
            return Material.PINK_WOOL;
        } else if (color.equals(Color.GRAY)) {
            return Material.GRAY_WOOL;
        } else if (color.equals(Color.SILVER)) { // Bukkit's representation of Light Gray
            return Material.LIGHT_GRAY_WOOL;
        } else if (color.equals(Color.TEAL)) { // Bukkit's representation of Cyan
            return Material.CYAN_WOOL;
        } else if (color.equals(Color.GREEN)) {
            return Material.GREEN_WOOL;
        }
        // For custom RGB colors like brown, compare by components or the exact RGB
        // value.
        // Note: Color.fromRGB(139, 69, 19) will be equal to itself.
        else if (color.equals(Color.fromRGB(139, 69, 19))) { // Matches your custom brown
            return Material.BROWN_WOOL;
        }

        // Default fallback if no match is found.
        // This is crucial to prevent NullPointerExceptions.
        return Material.WHITE_WOOL;
    }

    public void spawnBurstParticlesOnVisibleFaces(Location blockLoc, Material inkColorMaterial) {
        org.bukkit.World world = blockLoc.getWorld();
        Block block = blockLoc.getBlock();
        Color particleColor = getBukkitColorForMaterial(inkColorMaterial);

        // Common BlockFaces (excluding corners/edges for simplicity)
        BlockFace[] faces = {
                BlockFace.UP, BlockFace.DOWN,
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST
        };

        DustOptions dustOptions = null;
        if (particleColor != null) {
            dustOptions = new DustOptions(particleColor, 1.5F); // Larger size for burst
        }

        // Iterate through each possible face
        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);

            // Check if the face is "visible" (e.g., exposed to air or not a full solid
            // block)
            // You can refine this condition based on what you consider "inkable" surfaces.
            // For example, you might not want to ink fluids like water or lava.
            if (adjacentBlock.getType().isAir() || !adjacentBlock.getType().isSolid()) {
                // Calculate the precise location on the surface of the current face
                // A small offset (0.01) is used to ensure particles are directly on the
                // surface,
                // not floating or inside the block.
                Location particleSpawnLoc = getParticleLocationForFace(blockLoc, face, 0.01);

                // Spawn colored REDSTONE particles for the main splatter
                if (dustOptions != null) {
                    world.spawnParticle(Particle.DUST, particleSpawnLoc, 15, 0.2, 0.2, 0.2, 0.0, dustOptions);
                }

                // Also spawn some generic splash/water particles for a wet effect
                world.spawnParticle(Particle.SPLASH, particleSpawnLoc, 5, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    // --- Helper Method: getParticleLocationForFace (from previous discussion) ---
    // This is crucial for placing particles precisely on a block face.
    // Make sure this method is accessible where you call
    // spawnBurstParticlesOnVisibleFaces.
    /**
     * Calculates a precise location for a particle to be spawned on the surface of
     * a block face.
     * 
     * @param blockLoc The canonical (integer) location of the block.
     * @param face     The BlockFace to spawn particles on.
     * @param offset   A small offset (e.g., 0.01 or 0.05) to push the particle
     *                 slightly into the block
     *                 to ensure it appears on the surface, not floating outside.
     * @return The Location on the specified block face.
     */
    private Location getParticleLocationForFace(Location blockLoc, BlockFace face, double offset) {
        Location particleLoc = blockLoc.clone();
        particleLoc.add(0.5, 0.5, 0.5); // Move to the center of the block

        switch (face) {
            case UP:
                particleLoc.add(0, 0.5 - offset, 0);
                break;
            case DOWN:
                particleLoc.add(0, -0.5 + offset, 0);
                break;
            case NORTH:
                particleLoc.add(0, 0, -0.5 + offset);
                break;
            case SOUTH:
                particleLoc.add(0, 0, 0.5 - offset);
                break;
            case EAST:
                particleLoc.add(0.5 - offset, 0, 0);
                break;
            case WEST:
                particleLoc.add(-0.5 + offset, 0, 0);
                break;
            case SELF: // Fallback if no specific face (e.g., projectile hit exactly center)
            default:
                break;
        }
        return particleLoc;
    }

    private void ink(Location loc, Material inkMaterial) {

        Material currentType = loc.getBlock().getType();
        // do not ink barriers or lanterns
        if (currentType.equals(Material.BARRIER) || currentType.equals(Material.LANTERN)) {
            return;
        }
        if (!isInk(loc) || isEnemyInk(loc, getInkColorFromWool(inkMaterial))) {
            Map<String, Object> data = new HashMap<>();
            // Save blockdata from the current block state.
            Color color = getBukkitColorForMaterial(inkMaterial);
            if (isInk(loc)) {
                inkStorage.remove(loc);
            }
            inkStorage.put(loc.clone(), new InkRecord(color));
        }
        spawnBurstParticlesOnVisibleFaces(loc.clone(), inkMaterial);
        // if (currentType.name().endsWith("_CARPET")) {
        // // Attempt to convert the team wool color into a matching carpet type.
        // try {
        // Material teamCarpet = Material.valueOf(inkMaterial.name().replace("_WOOL",
        // "_CARPET"));
        // loc.getBlock().setType(teamCarpet);
        // } catch (IllegalArgumentException e) {
        // loc.getBlock().setType(inkMaterial);
        // }
        // } else if (currentType.name().endsWith("GLASS") ||
        // currentType.name().endsWith("GLASS_PANE")) {
        // if (currentType.name().endsWith("GLASS")) {
        // // Attempt to convert the team wool color into a matching stained glass type.
        // try {
        // Material stainedGlass = Material.valueOf(inkMaterial.name().replace("_WOOL",
        // "_STAINED_GLASS"));
        // loc.getBlock().setType(stainedGlass);
        // } catch (IllegalArgumentException e) {
        // loc.getBlock().setType(inkMaterial);
        // }
        // } else if (currentType.name().endsWith("GLASS_PANE")) {
        // // Attempt to convert the team wool color into a matching stained glass pane
        // // type.
        // try {
        // Material stainedGlassPane = Material
        // .valueOf(inkMaterial.name().replace("_WOOL", "_STAINED_GLASS_PANE"));
        // loc.getBlock().setBlockData(painKillerPills(loc.getBlock().getState(),
        // stainedGlassPane));
        // } catch (IllegalArgumentException e) {
        // loc.getBlock().setType(inkMaterial);
        // }
        // } else {
        // loc.getBlock().setType(inkMaterial);
        // }
        // } else {
        // loc.getBlock().setType(inkMaterial);
        // }
    }

    public String getInkColorFromWool(Material woolMaterial) {
        if (woolMaterial == null) {
            return "none"; // Default or error handling for null material
        }

        switch (woolMaterial) {
            case BLUE_WOOL:
                return "blue";
            case RED_WOOL:
                return "red";
            case LIME_WOOL:
                return "lime";
            case YELLOW_WOOL:
                return "yellow";
            case BLACK_WOOL:
                return "black";
            case WHITE_WOOL:
                return "white";
            case ORANGE_WOOL:
                return "orange";
            case MAGENTA_WOOL:
                return "magenta";
            case LIGHT_BLUE_WOOL:
                return "light_blue";
            case PURPLE_WOOL:
                return "purple";
            case PINK_WOOL:
                return "pink";
            case GRAY_WOOL:
                return "gray";
            case LIGHT_GRAY_WOOL:
                return "light_gray";
            case CYAN_WOOL:
                return "cyan";
            case BROWN_WOOL:
                return "brown";
            case GREEN_WOOL:
                return "green";
            default:
                return "none"; // Fallback for unmapped or unexpected material
        }
    }

    private void inkBlock(Location loc, Material inkMaterial, String weapon) {
        if (loc == null || inkMaterial == null) {
            return;
        }
        // Abort if the block is already air
        if (!isExposedToAir(loc)) {
            return;
        }
        getLogger().info("Inking block at location: " + loc + " with material: " + inkMaterial);
        // Save the original block type if not already stored

        // if weapon = splatterKey, then if an ink hits a same team ink block, it will
        // randomly go around until it finds a block that is not inked same.
        if (weapon.equals("splatterink") || weapon.equals("debugink") || weapon.equals("triangleink")) {
            // Check if the block is already inked with the same color
            if (isTeamInk(loc, getInkColorFromWool(inkMaterial))
                    || isAirLike(loc.getBlock().getType())) {
                // start searching for a nearby block that is not inked with the same color
                // but don't go too far, just check for a length of 3 blocks around it (this is
                // called a nerf)
                // so like this: start from the current location and then do a random move and
                // make count + 1. if count > 3, then restart
                // do this a max of 3 times before giving up
                int count = 0;

                for (int j = 0; j < 3; j++) {
                    Location newLoc = loc.clone();
                    for (int i = 0; i < 1; i++) {
                        int x = (int) (Math.random() * 3) - 1; // Randomly choose -1, 0, or 1
                        int y = (int) (Math.random() * 3) - 1;
                        int z = (int) (Math.random() * 3) - 1;
                        newLoc.add(x, y, z);
                        if (!isAirLike(newLoc.getBlock().getType())
                                && isExposedToAir(newLoc)) {
                            ink(newLoc, inkMaterial);
                        }
                    }
                }
                // If we reach here, it means we couldn't find a suitable block to ink
                ink(loc, inkMaterial);

            } else {
                ink(loc, inkMaterial);
            }
        } else {
            ink(loc, inkMaterial);
        }
    }

    @EventHandler
    public void onWallClimb(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (Location inkedLoc : new HashSet<>(inkStorage.keySet())) {
            InkRecord inkRecord = inkStorage.get(inkedLoc);
            if (inkRecord != null
                    && inkRecord.getColor().equals(getBukkitColorForMaterial(getWoolColor(getPlayerTeam(player))))) {
                Block block = inkedLoc.getBlock();
                if (block.getType().isSolid()) {
                    BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
                    for (BlockFace face : faces) {
                        Block adjacentBlock = block.getRelative(face);
                        if (adjacentBlock.getType().isAir()) {
                            BlockData ladderData = Material.LADDER.createBlockData();
                            if (ladderData instanceof org.bukkit.block.data.type.Ladder) {
                                org.bukkit.block.data.type.Ladder ladder = (org.bukkit.block.data.type.Ladder) ladderData;
                                ladder.setFacing(face);
                                if (player.isSneaking()
                                        && player.getLocation().distance(adjacentBlock.getLocation()) <= 2) {
                                    player.sendBlockChange(adjacentBlock.getLocation(), ladder);
                                } else {
                                    // discard and change back to original block
                                    player.sendBlockChange(adjacentBlock.getLocation(), adjacentBlock.getBlockData());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onPlayerJoin(final Audience player) {
        final Component header = Component.text("Splatoon").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
            .append(Component.text(" Server.").color(NamedTextColor.AQUA));
        final Component footer = Component.text("MOTD: battle the Roaring Knight today!").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD).decorate(TextDecoration.ITALIC); // No footer content
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    // here should be good
    // Ink Mechanics (Ink Regen)
    // on tick, regen ink for all players
    public void onInkRegen() {
        for (Entity entity : getServer().getWorlds().stream().flatMap(w -> w.getEntities().stream()).toList()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                ItemStack inkTank = null;
                for (ItemStack tank : player.getInventory().getContents()) {
                    if (tank != null && tank.getItemMeta() != null
                            && tank.getItemMeta().getPersistentDataContainer().has(inkTankKey,
                                    PersistentDataType.DOUBLE)) {
                        inkTank = tank;
                        break;
                    }
                }
                if (inkTank == null)
                    continue; // No ink tank found, skip this player

                ItemMeta tankMeta = inkTank.getItemMeta();
                if (tankMeta == null
                        || !tankMeta.getPersistentDataContainer().has(inkTankKey, PersistentDataType.DOUBLE))
                    continue;
                double inkAmount = tankMeta.getPersistentDataContainer().get(inkTankKey, PersistentDataType.DOUBLE);
                double regenAmount = 0.25;
                String team = getPlayerTeam(player);
                Material teamWool = getWoolColor(team);
                Location blockBelow = player.getLocation().clone().add(0, -1, 0);
                // Only count as ink if the block below is already tracked in inkStorage.
                if (isTeamInk(blockBelow, team)
                        || isTeamInk(blockBelow.clone().add(player.getLocation().getDirection().normalize()), team)
                        || isTeamInk(
                                blockBelow.clone().add(player.getLocation().getDirection().normalize().multiply(2)),
                                team)) {
                    regenAmount += 0.25;
                    if (player.isSneaking()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 20));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, true, false));
                        regenAmount += 0.25;
                    } else {
                        player.removePotionEffect(PotionEffectType.SPEED);
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
                if (isEnemyInk(blockBelow, team)) {
                    player.damage(0.5);
                    regenAmount = 0.0;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 1));
                }

                if (hasInfiniteInk(player)) {
                    regenAmount = 1000.0;
                }

                if (inkAmount < 100.0) { // Max ink amount is 100
                    inkAmount = Math.min(inkAmount + regenAmount, 100.0);
                    tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, inkAmount);
                    tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank " + (int) inkAmount + "/100");
                    inkTank.setItemMeta(tankMeta);
                    if (inkAmount == 100.0 && !hasInfiniteInk(player)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.GREEN + "Your Ink Tank is full!");
                    }
                }

                player.sendActionBar(ChatColor.AQUA + "Ink Tank: " + (int) inkAmount + "/100");
                Set<Location> inkedLocationsCopy = new HashSet<>(inkStorage.keySet());
                // Define a maximum render distance for particles. Lower this value to reduce
                // lag.
                final double MAX_PARTICLE_RENDER_DISTANCE = 64.0; // Reduced from 20.0 to 15.0 for better performance

                // Define a "sampling rate" for blocks. Higher values mean fewer blocks will
                // show particles.
                // A value of 2 means roughly half the blocks will show particles.
                // A value of 3 means roughly a third of the blocks will show particles, and so
                // on.
                final int BLOCK_SAMPLING_RATE = 1; // Only render particles for every Nth block

                for (Location inkedLoc : inkedLocationsCopy) {
                    InkRecord inkRecord = inkStorage.get(inkedLoc);
                    if (inkRecord == null || !player.getWorld().equals(inkedLoc.getWorld())) {
                        continue; // Skip if no record or in different world
                    }

                    // --- Optimization 1: Skip blocks too far away early ---
                    double distanceSquared = player.getLocation().distanceSquared(inkedLoc);
                    if (distanceSquared > (MAX_PARTICLE_RENDER_DISTANCE * MAX_PARTICLE_RENDER_DISTANCE)) {
                        continue; // Skip if beyond the max render distance
                    }

                    // --- Optimization 2: Block sampling ---
                    // This will skip drawing particles for a percentage of blocks, reducing overall
                    // particle count.
                    // Adjust BLOCK_SAMPLING_RATE to control density. Higher value = fewer
                    // particles.
                    if ((inkedLoc.getBlockX() + inkedLoc.getBlockY() + inkedLoc.getBlockZ())
                            % BLOCK_SAMPLING_RATE != 0) {
                        continue; // Skip this block if it doesn't meet the sampling criteria
                    }

                    double distance = Math.sqrt(distanceSquared); // Calculate actual distance after initial distance
                                                                  // check

                    int particleCount = 0;
                    double particleSpread = 0.0; // Offset for particle spread
                    float particleSize = 0.8F; // Keep consistent size for persistent ink

                    if (distance <= 5.0) { // Very close (within 5 blocks) - higher density
                        particleCount = 3; // Reduced from 4 for 2x2. More visible but less taxing.
                        particleSpread = 0.15; // Slightly tighter spread
                    } else if (distance <= 10.0) { // Medium distance (within 10 blocks) - medium density
                        particleCount = 1; // One particle per visible face
                        particleSpread = 0.1; // Small spread
                    } else { // Further distance (within 15 blocks, due to MAX_PARTICLE_RENDER_DISTANCE) -
                             // lowest density
                        // just say the damn thing is a wool block atp
                        player.sendBlockChange(inkedLoc, getWoolMaterialForColor(inkRecord.color).createBlockData());
                        continue;
                    }
                    player.sendBlockChange(inkedLoc, inkedLoc.getBlock().getBlockData());
                    // Only proceed if we actually decided to spawn particles
                    if (particleCount > 0) {
                        Color particleColor = inkRecord.color;
                        DustOptions dustOptions = new DustOptions(particleColor, particleSize);

                        Block currentInkBlock = inkedLoc.getBlock();
                        BlockFace[] faces = {
                                BlockFace.UP, BlockFace.DOWN,
                                BlockFace.NORTH, BlockFace.SOUTH,
                                BlockFace.EAST, BlockFace.WEST
                        };

                        for (BlockFace face : faces) {
                            Block adjacentBlock = currentInkBlock.getRelative(face);

                            // Only spawn particles on faces exposed to air or non-solid blocks
                            if (adjacentBlock.getType().isAir() || !adjacentBlock.getType().isSolid()) {
                                Location particleSpawnLoc = getParticleLocationForFace(inkedLoc, face, 0.01);

                                player.spawnParticle(Particle.DUST, particleSpawnLoc, particleCount,
                                        particleSpread, particleSpread, particleSpread, 0.0, dustOptions);
                            }
                        }
                    }
                }
            } else if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                Location blockBelow = living.getLocation().clone().add(0, -1, 0);
                if (isInk(blockBelow)) {
                    living.damage(0.5);
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 1));
                }
            }
        }

    }

    @EventHandler
    public void onInkHitBlock(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;
        Snowball inkShot = (Snowball) event.getEntity();

        if (inkShot.getCustomName() == null)
            return;
        if (!(inkShot.getShooter() instanceof Player))
            return;
        // if it's not in the weapon list
        boolean validInk = false;
        for (Map<String, Object> config : weaponList.values()) {
            if (config.containsKey("ink") && config.get("ink").equals(inkShot.getCustomName())) {
                validInk = true;
                break;
            }
        }
        if (!validInk) {
            return;
        }

        // debug time
        event.setCancelled(true);
        Player shooter = (Player) inkShot.getShooter();
        // may not always hit a block, so check if hitBlock is null
        if (event.getHitBlock() == null) {
            getLogger().info("Ink shot hit ???: " + inkShot.getCustomName());
            // handle team fighting
            // if it didn't hit a player then it hit a entity so just boost up the damage
            // and go
            if (event.getHitEntity() == null)
                return;
            getLogger().info("Ink shot hit entity: " + inkShot.getCustomName());
            if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
                Player hitPlayer = (Player) event.getHitEntity();
                String team = getPlayerTeam(shooter);
                String hitTeam = getPlayerTeam(hitPlayer);
                if (!team.equalsIgnoreCase(hitTeam)) {
                    // do damage to the player
                    double damage = 5.0;
                    // Get the ink identifier from the projectile's custom name
                    String customName = inkShot.getCustomName();
                    // Iterate through the weapon list to find the weapon with the matching ink
                    // property
                    for (Map<String, Object> config : weaponList.values()) {
                        if (config.containsKey("ink") && config.get("ink").equals(customName)) {
                            damage = ((Number) config.get("damage")).doubleValue();
                            break;
                        }
                    }
                    hitPlayer.damage(damage, shooter);
                } else {
                    // if the player is on the same team, just send a message
                }
            } else if (event.getHitEntity() != null) {
                LivingEntity entity = (LivingEntity) event.getHitEntity();
                // Handle ink sprinklers
                if (entity.getCustomName().endsWith("Ink Sprinkler")) {
                    {
                        // it starts with a name like "Marc's Ink Sprinkler" or "Friend's Ink Sprinkler"
                        String playerName = entity.getCustomName().replace("'s' Ink Sprinkler", "");
                        Player player = getServer().getPlayer(playerName);
                        if (player != null && player.isOnline()) {
                            // Ink the area around the entity hit by the ink shot
                            Location hitLocation = event.getHitEntity().getLocation();
                            Color team = getBukkitColorForMaterial(getWoolColor(getPlayerTeam(player)));
                            if (player != null && player.isOnline()) {
                                Color attackTeam = getBukkitColorForMaterial(getWoolColor(getPlayerTeam(shooter)));
                                if (!team.equals(attackTeam)) {
                                    entity.damage(5.0, shooter);
                                }
                            }
                        }
                    }

                    // Default behavior: just do damage
                    double damage = 5.0;
                    // Get the ink identifier from the projectile's custom name
                    String customName = inkShot.getCustomName();
                    // Iterate through the weapon list to find the weapon with the matching ink
                    // property
                    for (Map<String, Object> config : weaponList.values()) {
                        if (config.containsKey("ink") && config.get("ink").equals(customName)) {
                            damage = ((Number) config.get("damage")).doubleValue();
                            break;
                        }
                    }
                    entity.damage(damage, shooter);
                }

                return; // If it didn't hit a block, we don't need to do anything else
            }
        }
        getLogger().info("Ink shot hit block or entity: " + inkShot.getCustomName());
        Location hitLoc = event.getHitBlock().getLocation();
        String team = getPlayerTeam(shooter);

        if (team.equalsIgnoreCase("none"))
            return;
        // if (!isExposedToAir(hitLoc)) return;

        Material inkMaterial = getWoolColor(team);
        inkBlock(hitLoc, inkMaterial, inkShot.getCustomName());

        // spreadInk(hitLoc, inkMaterial); nvm i just had the best idea ever
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_WET_GRASS_PLACE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (isInk(loc)) {
            event.setCancelled(isEnabled());
            unink(loc);
        }
    }

    private boolean isExposedToAir(Location loc) {
        org.bukkit.World world = loc.clone().getWorld();
        return isAirLike(world.getBlockAt(loc.clone().add(1, 0, 0)).getType()) ||
                isAirLike(world.getBlockAt(loc.clone().add(-1, 0, 0)).getType()) ||
                isAirLike(world.getBlockAt(loc.clone().add(0, 1, 0)).getType()) ||
                isAirLike(world.getBlockAt(loc.clone().add(0, -1, 0)).getType()) ||
                isAirLike(world.getBlockAt(loc.clone().add(0, 0, 1)).getType()) ||
                isAirLike(world.getBlockAt(loc.clone().add(0, 0, -1)).getType());
    }

    private boolean isAirLike(Material material) {
        for (Material airMaterial : airLikeMaterials) {
            if (material == airMaterial) {
                return true;
            }
        }
        return false;
    }

    public boolean isInk(Location loc) {
        return inkStorage.containsKey(roundLocation(loc));
    }

    public boolean isTeamInk(Location loc, String team) {
        Material teamWool = getWoolColor(team);
        if (teamWool == null) {
            return false;
        }
        InkRecord record = inkStorage.get(roundLocation(loc));
        return record != null && record.color.equals(getBukkitColorForMaterial(teamWool));
    }

    public boolean isEnemyInk(Location loc, String team) {
        Material teamWool = getWoolColor(team);
        InkRecord record = inkStorage.get(roundLocation(loc));
        return record != null && (teamWool == null || !record.color.equals(getBukkitColorForMaterial(teamWool)));
    }

    private Location roundLocation(Location loc) {
        return new Location(loc.getWorld(),
                Math.round(loc.getX()),
                Math.round(loc.getY()),
                Math.round(loc.getZ()));
    }
}
