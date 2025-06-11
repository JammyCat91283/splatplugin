package com.silent_herbert.splatplugin;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
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
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitTask;
// plugin command
import org.bukkit.command.PluginCommand;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

// runnable
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.TNTPrimed;

public class SplatPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Player, String> playerTeams = new HashMap<>();
    private final Map<Location, Material> inkStorage = new HashMap<>();
    private final NamespacedKey splatterKey = new NamespacedKey(this, "splatterweapon");
    // splatplugin-splatterink is the key for the splatter weapon ink
    private final NamespacedKey splatterInkKey = new NamespacedKey(this, "splatterink");
    // splatpluginweapon
    private final NamespacedKey splatPluginWeaponKey = new NamespacedKey(this, "splatpluginweapon");
    // testweapon
    private final NamespacedKey testWeaponKey = new NamespacedKey(this, "testweapon");
    // inktank
    private final NamespacedKey inkTankKey = new NamespacedKey(this, "inktank");
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
        if (!getDataFolder().exists() || !new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        saveDefaultConfig();
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
            if (args.length < 1) {
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
            for (Map.Entry<Location, Material> entry : inkStorage.entrySet()) {
                Location loc = entry.getKey();
                Material originalMaterial = entry.getValue();
                if (loc.getBlock().getType() == Material.AIR)
                    continue; // skip air blocks
                loc.getBlock().setType(originalMaterial);
            }
            player.sendMessage(ChatColor.GREEN + "All ink has been cleared!");
            return true;
        }
        return false; // If the command is not recognized, return false to indicate failure
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
    private void ShootWeapon(Player player, ItemStack item, String weapon) {
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
        if (inkAmount < cost) {
            // play a sound for insufficient ink
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }
        // reduce the ink amount in the tank and update its display name
        inkAmount -= cost;
        tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, inkAmount);
        tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank " + (int) inkAmount + "/100");
        inkTank.setItemMeta(tankMeta);

        // Launch ink projectiles according to weapon type
        if (weapon.equalsIgnoreCase("trianglesub")) {
            // Trianglesub: Shoot a TNT that travels ~6 blocks away.
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(1.5f);
            // Spawn a primed TNT slightly above the player's location
            TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
            tnt.setVelocity(direction);
            tnt.setFuseTicks(80); // Set fuse duration (80 ticks = 4 seconds)
            tnt.setCustomName("trianglesub");
            tnt.setCustomNameVisible(false);
            // set the owner so the ink will inking
            tnt.setSource(player);
            new BukkitRunnable() {
                int tickCount = 0;
                int interval = 20; // starting beep interval in ticks
                int nextBeepTick = interval;

                @Override
                public void run() {
                    tickCount++;
                    if (tickCount >= tnt.getFuseTicks()) {
                        for (int x = -3; x <= 3; x++) {
                            for (int y = -3; y <= 3; y++) {
                                for (int z = -3; z <= 3; z++) {
                                    if (Math.sqrt(x * x + y * y + z * z) <= 3) {
                                        Location spawnLoc = tnt.getLocation().clone().add(x, y, z);
                                        Vector direction = spawnLoc.toVector().subtract(tnt.getLocation().toVector());
                                        if (direction.length() != 0) {
                                            direction.normalize().multiply(0.5f);
                                        }
                                        Snowball ball = tnt.getWorld().spawn(spawnLoc, Snowball.class);
                                        ball.setVelocity(direction);
                                        ball.setCustomName(splatterInkKey.toString());
                                        ball.setCustomNameVisible(false);
                                        ball.setShooter(tnt.getSource());
                                    }
                                }
                            }
                        }
                        tnt.remove();
                        cancel();
                        return; // is this?? yes
                    }
                    if (tickCount >= nextBeepTick) {
                        tnt.getWorld().playSound(tnt.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0f, 1.0f);
                        interval = interval / 2;
                        nextBeepTick = tickCount + interval;
                    }
                }
            }.runTaskTimer(SplatPlugin.this, 1L, 1L);
        } else if (weapon.equalsIgnoreCase("splatterweapon") || weapon.equalsIgnoreCase("debugweapon")) {
            // Splatter / Debug Weapon: Launch 3x3x3 balls with slight random variations
            for (double offsetX : new double[] { -0.01, 0, 0.01 }) {
                for (double offsetY : new double[] { -0.01, 0, 0.01 }) {
                    for (double offsetZ : new double[] { -0.01, 0, 0.01 }) {
                        Vector direction = player.getLocation().getDirection().clone();
                        double randomOffsetX = (Math.random() * 0.02) - 0.01;
                        double randomOffsetY = (Math.random() * 0.02) - 0.01;
                        double randomOffsetZ = (Math.random() * 0.02) - 0.01;
                        direction
                                .add(new Vector(offsetX + randomOffsetX, offsetY + randomOffsetY,
                                        offsetZ + randomOffsetZ))
                                .normalize().multiply(force);
                        Snowball inkShot = player.launchProjectile(Snowball.class, direction);
                        inkShot.setCustomName(splatterInkKey.toString());
                        inkShot.setCustomNameVisible(false);
                        inkShot.setShooter(player);
                    }
                }
            }
        } else {
            // Other weapons: Launch a single projectile
            Vector direction = player.getLocation().getDirection().clone().normalize().multiply(force);
            Snowball inkShot = player.launchProjectile(Snowball.class, direction);
            inkShot.setCustomName(splatterInkKey.toString());
            inkShot.setCustomNameVisible(false);
            inkShot.setShooter(player);
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
        ShootWeapon(player, item, splatterWeapon);
        event.setCancelled(true);
    }

    private void inkBlock(Location loc, Material inkMaterial, String weapon) {
        if (loc == null || inkMaterial == null)
            return;
        // Abort if the block is already air
        if (loc.getBlock().getType() == Material.AIR)
            return;
        // Save the original block type if not already stored

        // if weapon = splatterKey, then if a ink hits a same team ink block, it will
        // randomly go around until it finds a block that is not inked same.
        if (weapon.equals(splatterInkKey.toString())) {
            // Check if the block is already inked with the same color
            if (loc.getBlock().getType() == inkMaterial) {
                // start searching for a nearby block that is not inked with the same color
                // but don't go too far, just check for a length of 3 blocks around it (this is
                // called a nerf)
                // so like this: start from the current location and then do a random move and
                // make count + 1. if count > 3, then restart
                // do this a max of 3 times before giving up
                int count = 0;

                for (int j = 0; j < 3; j++) {
                    Location newLoc = loc.clone();
                    for (int i = 0; i < 3; i++) {
                        int x = (int) (Math.random() * 3) - 1; // Randomly choose -1, 0, or 1
                        int y = (int) (Math.random() * 3) - 1;
                        int z = (int) (Math.random() * 3) - 1;
                        newLoc.add(x, y, z);

                        if (newLoc.getBlock().getType() != inkMaterial && newLoc.getBlock().getType() != Material.AIR
                                && isExposedToAir(newLoc)) {
                            if (!inkStorage.containsKey(newLoc)) {
                                inkStorage.put(newLoc, newLoc.getBlock().getType());
                            }
                            newLoc.getBlock().setType(inkMaterial);
                            return; // Successfully inked a new block
                        }
                    }
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

    // here should be good
    // Ink Mechanics (Ink Regen)
    // on tick, regen ink for all players
    public void onInkRegen() {
        for (Player player : getServer().getOnlinePlayers()) {
            ItemStack inkTank = null;
            for (ItemStack tank : player.getInventory().getContents()) {
                if (tank != null && tank.getItemMeta() != null
                        && tank.getItemMeta().getPersistentDataContainer().has(inkTankKey, PersistentDataType.DOUBLE)) {
                    inkTank = tank;
                    break;
                }
            }
            if (inkTank == null)
                continue; // No ink tank found, skip this player

            ItemMeta tankMeta = inkTank.getItemMeta();
            if (tankMeta == null || !tankMeta.getPersistentDataContainer().has(inkTankKey, PersistentDataType.DOUBLE))
                continue;

            double inkAmount = tankMeta.getPersistentDataContainer().get(inkTankKey, PersistentDataType.DOUBLE);
            double regenAmount = 0.25;
            String team = getPlayerTeam(player);
            Material teamWool = getWoolColor(team);
            Material below = player.getLocation().clone().add(0, -1, 0).getBlock().getType();
            if (teamWool != null && below == teamWool) {
                regenAmount += 0.25; // Regen faster on same team wool
            }
            // If stepping on opposing team's wool, damage the player and prevent ink
            // regeneration
            if (teamWool != null && below.toString().endsWith("_WOOL") && !below.equals(teamWool)) {
                player.damage(0.5); // Damage the player for being on opposing team wool
                regenAmount = 0.0;
            }
            if (inkAmount < 100.0) { // Max ink amount is 100
                inkAmount = Math.min(inkAmount + regenAmount, 100.0);
                tankMeta.getPersistentDataContainer().set(inkTankKey, PersistentDataType.DOUBLE, inkAmount);
                tankMeta.setDisplayName(ChatColor.AQUA + "Ink Tank " + (int) inkAmount + "/100");
                inkTank.setItemMeta(tankMeta);
                if (inkAmount == 100.0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "Your Ink Tank is full!");
                }
            }
            // Show the ink amount in the hotbar action bar
            player.sendActionBar(ChatColor.AQUA + "Ink Tank: " + (int) inkAmount + "/100");
            // Extra: if the player is sneaking on a same-team wool block, add speed and
            // invisibility effects
            if (player.isSneaking() && teamWool != null && below == teamWool) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 10));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, true, false));
            }
        }
    }

    @EventHandler
    public void onInkHitBlock(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;
        Snowball inkShot = (Snowball) event.getEntity();

        if (inkShot.getCustomName() == null || !inkShot.getCustomName().startsWith("splatplugin:"))
            return;
        if (!(inkShot.getShooter() instanceof Player))
            return;
        // debug time

        Player shooter = (Player) inkShot.getShooter();
        // may not always hit a block, so check if hitBlock is null
        if (event.getHitBlock() == null) {
            // handle team fighting
            // if it didn't hit a player then it hit a entity so just boost up the damage
            // and go
            if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
                Player hitPlayer = (Player) event.getHitEntity();
                String team = getPlayerTeam(shooter);
                String hitTeam = getPlayerTeam(hitPlayer);
                if (!team.equalsIgnoreCase(hitTeam)) {
                    // do damage to the player
                    if (inkShot.getCustomName().equals(splatterInkKey.toString())) {
                        // if the ink shot is a splatter ink, then do damage
                        hitPlayer.damage(0.625); // 0.625 * 8 = 5.0, which is the damage of the splatter weapon
                    } else {
                        // if it's not a splatter ink, just send a message
                        hitPlayer.damage(5.0);
                    }
                } else {
                    // if the player is on the same team, just send a message
                    event.setCancelled(true);
                }
            } else if (event.getHitEntity() != null) {
                // just do damage

            }
            return; // If it didn't hit a block, we don't need to do anything else
        }
        Location hitLoc = event.getHitBlock().getLocation();
        String team = getPlayerTeam(shooter);

        if (team.equalsIgnoreCase("none"))
            return;
        // if (!isExposedToAir(hitLoc)) return;

        Material inkMaterial = getWoolColor(team);
        inkBlock(hitLoc, inkMaterial, inkShot.getCustomName().toString());

        // spreadInk(hitLoc, inkMaterial); nvm i just had the best idea ever
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_WET_GRASS_PLACE, 1.0f, 1.0f);
    }

    private void spreadInk(Location loc, Material inkMaterial) {
        World world = loc.getWorld();

        if (world == null || inkMaterial == null)
            return;

        // loop blocks radius 2 around the hit location
        // for each block, check if it's not air and exposed to air then set it to the
        // ink material
        // don't overwrite existing ink blocks cause that would make the ink spread over
        // ink, permanently erasing the original block
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location newLoc = loc.clone().add(x, y, z);
                    if (newLoc.getBlock().getType() != Material.AIR && isExposedToAir(newLoc)) {

                        if (!inkStorage.containsKey(newLoc)) { // get data instead of just type so chests and other
                                                               // blocks can be restored
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

        if (team.equalsIgnoreCase("none"))
            return;

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

        if (team.equalsIgnoreCase("none"))
            return;

        Material teamWool = getWoolColor(team);
        if (teamWool != null && below == teamWool) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0, true, false));
        } else if (below.toString().endsWith("_WOOL")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 2));
        } else {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }
}
