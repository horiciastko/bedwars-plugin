package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.gui.ShopGUI;
import me.horiciastko.bedwars.gui.UpgradeGUI;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;

@SuppressWarnings("deprecation")
public class GameListener implements Listener {

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (BedWars.getInstance().getGameManager().isInBuildMode(player)) {
            return;
        }

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (block.getType().name().endsWith("_BED")) {
            handleBedBreak(event, player, arena);
            return;
        } else {
            for (Team t : arena.getTeams()) {
                if (isInside(block.getLocation(), t.getBasePos1(), t.getBasePos2())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (arena.getPlacedBlocks().contains(block.getLocation())) {
                arena.getPlacedBlocks().remove(block.getLocation());
            } else {
                event.setCancelled(true);
                String msg = BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                        "interact-cant-break");
                player.sendMessage(msg);
            }
        }
    }

    private void handleBedBreak(BlockBreakEvent event, Player player, Arena arena) {
        Team victimTeam = null;
        for (Team team : arena.getTeams()) {
            if (isBedBlock(team.getBedLocation(), event.getBlock())) {
                victimTeam = team;
                break;
            }
        }

        if (victimTeam == null) {
            event.setCancelled(true);
            String msg = BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                    "interact-cant-break");
            player.sendMessage(msg);
            return;
        }

        if (victimTeam.isBedBroken()) {
            event.setCancelled(true);
            clearBedBlocks(event.getBlock());
            return;
        }

        Team attackerTeam = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);

        if (attackerTeam == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerTeam.getName().equals(victimTeam.getName())) {
            event.setCancelled(true);
            String msg = BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                    "interact-cant-destroy-bed");
            player.sendMessage(msg);
        } else {
            event.setCancelled(true);
            clearBedBlocks(event.getBlock());

            victimTeam.setBedBroken(true);

            String attackerColor = (attackerTeam.getColor() != null ? attackerTeam.getColor().toString() : "§f");

            String bedMsg = BedWars.getInstance().getLanguageManager().getMessage(null, "interact-bed-destroy-chat");
            String formattedMsg = bedMsg
                    .replace("{TeamColor}", victimTeam.getColor() != null ? victimTeam.getColor().toString() : "§f")
                    .replace("{TeamName}", victimTeam.getName())
                    .replace("{PlayerColor}", attackerColor)
                    .replace("{PlayerName}", player.getName());

            for (Player p : arena.getPlayers()) {
                p.sendMessage(formattedMsg);
                if (p.getUniqueId().equals(player.getUniqueId()) || attackerTeam.getMembers().contains(p)) {
                    BedWars.getInstance().getSoundManager().playSound(p, "bed-destroy");
                } else if (victimTeam.getMembers().contains(p)) {
                    BedWars.getInstance().sendTitle(p,
                            BedWars.getInstance().getLanguageManager().getMessage(p.getUniqueId(),
                                    "interact-bed-destroy-title"),
                            BedWars.getInstance().getLanguageManager().getMessage(p.getUniqueId(),
                                    "interact-bed-destroy-subtitle"),
                            10, 70, 20);
                    BedWars.getInstance().getSoundManager().playSound(p, "bed-destroy-own");
                } else {
                    BedWars.getInstance().getSoundManager().playSound(p, "bed-destroy");
                }
            }

            BedWars.getInstance().getStatsManager().addBedBroken(player.getUniqueId());
            BedWars.getInstance().getGameManager().getOrCreateSessionStats(arena, player.getUniqueId()).bedsBroken++;

            BedWars.getInstance().getVisualizationManager().spawnGameHolograms(arena);

            for (Player p : arena.getPlayers()) {
                BedWars.getInstance().getScoreboardManager().updateScoreboard(p);
            }
        }
    }

    private boolean isBedBlock(org.bukkit.Location bedLoc, Block block) {
        if (bedLoc == null)
            return false;
        return block.getLocation().distance(bedLoc) <= 1.5;
    }

    private void clearBedBlocks(Block block) {
        block.setType(Material.AIR);
        for (org.bukkit.block.BlockFace face : new org.bukkit.block.BlockFace[]{
                org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH,
                org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType().name().endsWith("_BED")) {
                adjacent.setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (BedWars.getInstance().getGameManager().isInBuildMode(player)) {
            return;
        }

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        for (Team t : arena.getTeams()) {
            if (isInside(event.getBlock().getLocation(), t.getBasePos1(), t.getBasePos2())) {
                event.setCancelled(true);
                return;
            }
        }

        for (org.bukkit.Location loc : arena.getDiamondGenerators()) {
            if (event.getBlock().getLocation().distance(loc) < 1.6) {
                event.setCancelled(true);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                        "interact-cant-build-generator"));
                return;
            }
        }
        for (org.bukkit.Location loc : arena.getEmeraldGenerators()) {
            if (event.getBlock().getLocation().distance(loc) < 1.6) {
                event.setCancelled(true);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                        "interact-cant-build-generator"));
                return;
            }
        }

        if (event.getBlock().getType() == Material.TNT) {
            event.setCancelled(true);
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.getItemInHand().setAmount(event.getItemInHand().getAmount() - 1);
            }
            org.bukkit.Location tntLoc = event.getBlock().getLocation().add(0.5, 0, 0.5);
            org.bukkit.entity.TNTPrimed tnt = event.getBlock().getWorld().spawn(tntLoc,
                    org.bukkit.entity.TNTPrimed.class);
            int fuse = 52;
            tnt.setFuseTicks(fuse);
            tnt.setSource(player);

            if (BedWars.getInstance().getConfig().getBoolean("game.tnt.timer-enabled", true)) {
                org.bukkit.entity.ArmorStand timer = tntLoc.getWorld().spawn(tntLoc.clone().add(0, 0.5, 0),
                        org.bukkit.entity.ArmorStand.class, as -> {
                            as.setVisible(false);
                            as.setGravity(false);
                            as.setSmall(true);
                            as.setMarker(true);
                            as.setCustomNameVisible(true);
                            as.setCustomName("§e" + String.format("%.1f", fuse / 20.0));
                        });

                new org.bukkit.scheduler.BukkitRunnable() {
                    int remaining = fuse;

                    @Override
                    public void run() {
                        if (tnt.isDead() || !tnt.isValid()) {
                            timer.remove();
                            this.cancel();
                            return;
                        }
                        remaining--;
                        if (remaining % 2 == 0) {
                            timer.setCustomName("§c" + String.format("%.1f", remaining / 20.0) + "s");
                        }
                        timer.teleport(tnt.getLocation().add(0, 1.2, 0));
                    }
                }.runTaskTimer(BedWars.getInstance(), 0L, 1L);
            }
            return;
        }

        if (event.getBlock().getType() == Material.CHEST) {
            ItemStack item = event.getItemInHand();
            if (item != null && item.hasItemMeta()) {
                String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "special_item");
                if ("tower".equals(special)) {
                    event.setCancelled(true);
                    if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                        item.setAmount(item.getAmount() - 1);
                    }
                    me.horiciastko.bedwars.utils.TowerBuilder.build(player, arena, event.getBlock().getLocation());
                    return;
                }
            }
        }

        arena.getPlacedBlocks().add(event.getBlock().getLocation());
    }

    private boolean isInside(org.bukkit.Location loc, org.bukkit.Location p1, org.bukkit.Location p2) {
        if (p1 == null || p2 == null || loc == null)
            return false;
        if (!loc.getWorld().getName().equals(p1.getWorld().getName()))
            return false;
        double minX = Math.min(p1.getX(), p2.getX());
        double maxX = Math.max(p1.getX(), p2.getX());
        double minY = Math.min(p1.getY(), p2.getY());
        double maxY = Math.max(p1.getY(), p2.getY());
        double minZ = Math.min(p1.getZ(), p2.getZ());
        double maxZ = Math.max(p1.getZ(), p2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);

        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            int voidY = BedWars.getInstance().getConfig().getInt("game.void-y-level", 0);
            if (player.getLocation().getY() < voidY) {
                Location lobby = BedWars.getInstance().getGameManager().getMainLobbyLocation();
                if (lobby != null) {
                    player.teleport(lobby);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "void-teleport-lobby"));
                }
            }
            return;
        }

        int voidY = BedWars.getInstance().getConfig().getInt("game.void-y-level", 0);
        if (player.getLocation().getY() < voidY) {
            if (player.getHealth() > 0) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1f, 1f);
                player.damage(0.1);
                BedWars.getInstance().getGameManager().handleDeath(player, arena, "fell into the void.");
            }
        }
        if (player.getGameMode() != org.bukkit.GameMode.SURVIVAL)
            return;

        Team myTeam = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
        for (Team t : arena.getTeams()) {
            if (myTeam != null && myTeam.getName().equals(t.getName()))
                continue;
            if (t.getActiveTraps().isEmpty())
                continue;

            if (isInside(player.getLocation(), t.getBasePos1(), t.getBasePos2())) {
                if (BedWars.getInstance().getGameManager().hasTrapImmunity(player.getUniqueId())) {
                    continue;
                }

                String trapKey = t.getActiveTraps().remove(0);
                triggerTrap(t, player, trapKey);
                break;
            }
        }
    }

    private void triggerTrap(Team defendingTeam, Player intruder, String trapKey) {
        ConfigurationSection trapData = BedWars.getInstance().getConfigManager().getUpgradesConfig()
                .getConfigurationSection("trap-types." + trapKey);
        if (trapData == null)
            return;

        String name = org.bukkit.ChatColor.translateAlternateColorCodes('&', trapData.getString("name", "Trap"));
        defendingTeam.getMembers().forEach(m -> {
            BedWars.getInstance().sendTitle(m,
                    BedWars.getInstance().getLanguageManager().getMessage(m.getUniqueId(), "trap-triggered-title"),
                    BedWars.getInstance().getLanguageManager().getMessage(m.getUniqueId(), "trap-triggered-subtitle")
                            .replace("%trap%", name),
                    10, 40, 10);
            m.playSound(m.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        String effects = trapData.getString("effect", "");
        if (effects.equals("ALARM")) {
            intruder.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
            intruder.addPotionEffect(
                    new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 10 * 20, 0));
        } else {
            for (String eff : effects.split(",")) {
                String[] parts = eff.split(":");
                String effectName = parts[0];
                int duration = Integer.parseInt(parts[1]) * 20;
                int amp = Integer.parseInt(parts[2]) - 1;
                intruder.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.getByName(effectName), duration, amp));
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);

        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            return;
        }

        Material type = event.getItemDrop().getItemStack().getType();
        String name = type.name();

        if (name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.endsWith("_PICKAXE") || name.endsWith("_AXE") || type == Material.SHEARS
                || name.contains("BED")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(org.bukkit.event.player.PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena != null && arena.getState() == Arena.GameState.IN_GAME) {
            BedWars.getInstance().getLanguageManager().localizeItem(player.getUniqueId(),
                    event.getItem().getItemStack());
        }

        if (event.getItem().getItemStack().getType().name().contains("BED")) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME)
            return;

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }

        if (event.getCurrentItem() != null) {
            BedWars.getInstance().getLanguageManager().localizeItem(player.getUniqueId(), event.getCurrentItem());
        }
        if (event.getCursor() != null) {
            BedWars.getInstance().getLanguageManager().localizeItem(player.getUniqueId(), event.getCursor());
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!(event.getRightClicked() instanceof Villager))
            return;
        Villager villager = (Villager) event.getRightClicked();

        if (!villager.getScoreboardTags().contains("bw_npc"))
            return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String name = villager.getCustomName();
        if (name == null)
            return;
        name = org.bukkit.ChatColor.stripColor(name);

        if (name.contains("SHOP")) {
            new ShopGUI().open(player);
        } else if (name.contains("UPGRADES")) {
            new UpgradeGUI().open(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && event.getClickedBlock() != null) {
            String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "special_item");

            if ("base_selection_tool".equals(special)) {
                Arena editArena = BedWars.getInstance().getArenaManager().getEditArena(player);
                if (editArena == null) {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "interact-edit-mode-required"));
                    event.setCancelled(true);
                    return;
                }

                Team targetTeam = findClosestTeam(editArena, event.getClickedBlock().getLocation());
                if (targetTeam == null) {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "interact-no-team-nearby"));
                    event.setCancelled(true);
                    return;
                }

                event.setCancelled(true);
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    targetTeam.setBasePos1(event.getClickedBlock().getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "interact-base-pos-set").replace("%pos%", "1")
                            .replace("%color%", targetTeam.getColor().toString())
                            .replace("%team%", targetTeam.getName()));
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    targetTeam.setBasePos2(event.getClickedBlock().getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "interact-base-pos-set").replace("%pos%", "2")
                            .replace("%color%", targetTeam.getColor().toString())
                            .replace("%team%", targetTeam.getName()));
                }
                BedWars.getInstance().getArenaManager().saveArena(editArena);
                return;
            }
        }

        if (!BedWars.getInstance().getGameManager().isInBuildMode(player) &&
                (arena == null || arena.getState() != Arena.GameState.IN_GAME)) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
                if (!event.getClickedBlock().getType().name().contains("SIGN")) {
                    event.setCancelled(true);
                }
            }
        }

        if (arena == null || arena.getState() != Arena.GameState.IN_GAME)
            return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType().name().contains("BED")) {
            event.setCancelled(true);
        }

        ItemStack itemStack = event.getItem();
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(itemStack, "special_item");
        if (special == null)
            special = "";

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (itemStack.getType() == Material.FIRE_CHARGE) {
                event.setCancelled(true);
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }

                org.bukkit.Location spawnLoc = player.getEyeLocation()
                        .add(player.getEyeLocation().getDirection().multiply(1.0));
                org.bukkit.entity.Fireball fireball = (org.bukkit.entity.Fireball) player.getWorld()
                        .spawnEntity(spawnLoc, org.bukkit.entity.EntityType.FIREBALL);

                fireball.setYield(2.5f);
                fireball.setIsIncendiary(false);
                fireball.setShooter(player);
                fireball.setVelocity(player.getEyeLocation().getDirection().multiply(1.2));

                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
            } else if (special.equals("tower")) {
                if (event.getClickedBlock() == null)
                    return;
                event.setCancelled(true);
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
                me.horiciastko.bedwars.utils.TowerBuilder.build(player, arena,
                        event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
            } else if (itemStack.getType() == Material.SNOWBALL || itemStack.getType().name().contains("SPAWN_EGG")) {
                if (event.getClickedBlock() == null)
                    return;
                event.setCancelled(true);
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }

                org.bukkit.entity.Entity entity;
                String key;
                if (itemStack.getType() == Material.SNOWBALL) {
                    entity = event.getClickedBlock().getWorld().spawn(
                            event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),
                            org.bukkit.entity.Silverfish.class);
                    key = "entity-bedbug";
                } else {
                    entity = event.getClickedBlock().getWorld().spawn(
                            event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),
                            org.bukkit.entity.IronGolem.class);
                    key = "entity-dream-defender";
                }

                String customName = BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), key);
                entity.setCustomName(customName);
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) entity;
                    le.setCustomNameVisible(true);
                }
                entity.addScoreboardTag("bw_mob");
                Team playerTeam = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
                if (playerTeam != null) {
                    entity.addScoreboardTag("team_" + playerTeam.getName());
                }
            } else if (itemStack.getType() == Material.DRAGON_EGG) {
                if (event.getClickedBlock() == null)
                    return;
                event.setCancelled(true);
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }

                Location spawnLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                BedWars.getInstance().getGameManager().spawnTeamDragon(player, arena, spawnLoc);
            } else if (itemStack.getType() == Material.MILK_BUCKET) {
                event.setCancelled(true);
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
                BedWars.getInstance().getGameManager().setTrapImmunity(player.getUniqueId(), 30);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                        "interact-magic-milk"));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_DRINK, 1f, 1f);
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Villager) {
            org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) event.getEntity();
            if (villager.getScoreboardTags().contains("bw_npc") || villager.getScoreboardTags().contains("bw_npc_shop")
                    || villager.getScoreboardTags().contains("bw_npc_upgrades")) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        try {
            if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                if (arena.getPvpMode() == Arena.PvpMode.LEGACY_1_8) {
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent edbe = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
            if (edbe.getDamager() instanceof org.bukkit.entity.TNTPrimed
                    || edbe.getDamager() instanceof org.bukkit.entity.Fireball) {

                boolean tntDmgEnabled = BedWars.getInstance().getConfig().getBoolean("game.tnt.damage-enabled", false);
                double tntDmg = BedWars.getInstance().getConfig().getDouble("game.tnt.damage-amount", 2.0);
                boolean fireballDmgEnabled = BedWars.getInstance().getConfig()
                        .getBoolean("game.fireball.damage-enabled", true);
                double fireballDmg = BedWars.getInstance().getConfig().getDouble("game.fireball.damage-amount", 4.0);

                double tntJump = BedWars.getInstance().getConfig().getDouble("game.tnt.jump-power", 2.5);
                double fbJump = BedWars.getInstance().getConfig().getDouble("game.fireball.jump-power", 2.0);

                double jumpPower = edbe.getDamager() instanceof org.bukkit.entity.TNTPrimed ? tntJump : fbJump;

                if (edbe.getDamager() instanceof org.bukkit.entity.TNTPrimed) {
                    double finalDmg = tntDmgEnabled ? tntDmg : 0;
                    if (player.getHealth() > finalDmg) {
                        event.setDamage(finalDmg);
                    } else if (tntDmgEnabled) {
                        event.setDamage(Math.min(player.getHealth() - 0.1, finalDmg));
                    } else {
                        event.setDamage(0);
                    }
                } else {
                    double finalDmg = fireballDmgEnabled ? fireballDmg : 0;
                    if (player.getHealth() > finalDmg) {
                        event.setDamage(finalDmg);
                    } else if (fireballDmgEnabled) {
                        event.setDamage(Math.min(player.getHealth() - 0.1, finalDmg));
                    } else {
                        event.setDamage(0);
                    }
                }

                org.bukkit.util.Vector direction = player.getLocation().toVector()
                        .subtract(edbe.getDamager().getLocation().toVector()).normalize();

                direction.setY(0.5);
                direction.normalize().multiply(jumpPower);

                player.setAllowFlight(true);
                player.setVelocity(player.getVelocity().add(direction));

                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE &&
                                    player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                                player.setAllowFlight(false);
                                player.setFlying(false);
                            }
                        }
                    }
                }.runTaskLater(BedWars.getInstance(), 40L);
            }
        }

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            String reason = "died.";
            if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
                reason = "fell into the void.";
            } else if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                org.bukkit.event.entity.EntityDamageByEntityEvent damageByEntity = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
                if (damageByEntity.getDamager() instanceof Player) {
                    Player killer = (Player) damageByEntity.getDamager();
                    reason = "was killed by " + killer.getName() + ".";
                } else if (damageByEntity.getDamager() instanceof org.bukkit.entity.Projectile) {
                    org.bukkit.entity.Projectile arrow = (org.bukkit.entity.Projectile) damageByEntity.getDamager();
                    if (arrow.getShooter() instanceof Player) {
                        Player shooter = (Player) arrow.getShooter();
                        reason = "was shot by " + shooter.getName() + ".";
                    }
                } else if (damageByEntity.getDamager() instanceof org.bukkit.entity.TNTPrimed) {
                    org.bukkit.entity.TNTPrimed tnt = (org.bukkit.entity.TNTPrimed) damageByEntity.getDamager();
                    if (tnt.getSource() instanceof Player) {
                        Player killer = (Player) tnt.getSource();
                        reason = "was blown up by " + killer.getName() + ".";
                    }
                } else if (damageByEntity.getDamager() instanceof org.bukkit.entity.Fireball) {
                    org.bukkit.entity.Fireball fireball = (org.bukkit.entity.Fireball) damageByEntity.getDamager();
                    if (fireball.getShooter() instanceof Player) {
                        Player killer = (Player) fireball.getShooter();
                        reason = "was fireballed by " + killer.getName() + ".";
                    }
                }
            }
            BedWars.getInstance().getGameManager().handleDeath(player, arena, reason);
        }

        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent edbe_fix = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
            Player damager = null;
            if (edbe_fix.getDamager() instanceof Player) {
                damager = (Player) edbe_fix.getDamager();
            } else if (edbe_fix.getDamager() instanceof org.bukkit.entity.Projectile) {
                org.bukkit.entity.Projectile proj = (org.bukkit.entity.Projectile) edbe_fix.getDamager();
                if (proj.getShooter() instanceof Player) {
                    damager = (Player) proj.getShooter();
                }
            } else if (edbe_fix.getDamager() instanceof org.bukkit.entity.TNTPrimed) {
                org.bukkit.entity.TNTPrimed tnt = (org.bukkit.entity.TNTPrimed) edbe_fix.getDamager();
                if (tnt.getSource() instanceof Player) {
                    damager = (Player) tnt.getSource();
                }
            }

            if (damager != null) {
                BedWars.getInstance().getGameManager().setLastDamager(player, damager);
            }
        }

        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent edbe = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
            if (edbe.getDamager().getScoreboardTags().contains("bw_mob")) {
                if (BedWars.getInstance().getConfig().getBoolean("game.prevent-own-mob-damage", true)) {
                    String mobTeamTag = edbe.getDamager().getScoreboardTags().stream()
                            .filter(t -> t.startsWith("team_")).findFirst().orElse(null);
                    me.horiciastko.bedwars.models.Team playerTeam = BedWars.getInstance().getGameManager()
                            .getPlayerTeam(arena, player);
                    if (mobTeamTag != null && playerTeam != null && mobTeamTag.equals("team_" + playerTeam.getName())) {
                        event.setCancelled(true);
                    }
                }
            }

            if (edbe.getEntity().getScoreboardTags().contains("bw_mob")
                    && edbe.getDamager() instanceof Player) {
                Player damager = (Player) edbe.getDamager();
                Team damagerTeam = BedWars.getInstance().getGameManager().getPlayerTeam(arena, damager);
                String mobTeamTag = edbe.getEntity().getScoreboardTags().stream()
                        .filter(t -> t.startsWith("team_")).findFirst().orElse(null);
                if (mobTeamTag != null && damagerTeam != null && mobTeamTag.equals("team_" + damagerTeam.getName())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        for (Arena arena : BedWars.getInstance().getArenaManager().getArenas()) {
            if (arena.getWorldName() != null && arena.getWorldName().equals(event.getWorld().getName())) {
                if (event.toWeatherState()) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        for (Arena arena : BedWars.getInstance().getArenaManager().getArenas()) {
            if (arena.getWorldName() != null && arena.getWorldName().equals(event.getLocation().getWorld().getName())) {
                if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM
                        || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                        || event.getEntityType() == EntityType.VILLAGER
                        || event.getEntityType() == EntityType.ARMOR_STAND
                        || event.getEntityType() == EntityType.ENDER_DRAGON
                        || event.getEntityType() == EntityType.SILVERFISH
                        || event.getEntityType() == EntityType.IRON_GOLEM) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityTarget(org.bukkit.event.entity.EntityTargetLivingEntityEvent event) {
        if (!event.getEntity().getScoreboardTags().contains("bw_mob"))
            return;

        String teamTag = event.getEntity().getScoreboardTags().stream()
                .filter(t -> t.startsWith("team_"))
                .findFirst().orElse(null);

        if (teamTag == null)
            return;

        if (event.getTarget() instanceof Player) {
            Player victim = (Player) event.getTarget();
            Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(victim);
            if (arena == null)
                return;
            Team victimTeam = BedWars.getInstance().getGameManager().getPlayerTeam(arena, victim);
            if (victimTeam != null && teamTag.equals("team_" + victimTeam.getName())) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity() instanceof org.bukkit.entity.IronGolem) {
                if (victimTeam != null && !teamTag.equals("team_" + victimTeam.getName())) {
                    if (victimTeam.getBasePos1() != null && victimTeam.getBasePos2() != null) {
                        Location loc = victim.getLocation();
                        Location p1 = victimTeam.getBasePos1();
                        Location p2 = victimTeam.getBasePos2();
                        double minX = Math.min(p1.getX(), p2.getX()) - 2;
                        double maxX = Math.max(p1.getX(), p2.getX()) + 2;
                        double minY = Math.min(p1.getY(), p2.getY()) - 2;
                        double maxY = Math.max(p1.getY(), p2.getY()) + 2;
                        double minZ = Math.min(p1.getZ(), p2.getZ()) - 2;
                        double maxZ = Math.max(p1.getZ(), p2.getZ()) + 2;

                        if (loc.getX() >= minX && loc.getX() <= maxX &&
                                loc.getY() >= minY && loc.getY() <= maxY &&
                                loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        } else if (event.getTarget() != null && event.getTarget().getScoreboardTags().contains("bw_mob")) {
            String targetTeamTag = event.getTarget().getScoreboardTags().stream()
                    .filter(t -> t.startsWith("team_"))
                    .findFirst().orElse(null);
            if (targetTeamTag != null && targetTeamTag.equalsIgnoreCase(teamTag)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Arena arena = null;
        for (Arena a : BedWars.getInstance().getArenaManager().getArenas()) {
            if (a.getWorldName() != null && a.getWorldName().equals(event.getLocation().getWorld().getName())) {
                arena = a;
                break;
            }
        }

        if (arena == null || arena.getState() != Arena.GameState.IN_GAME) {
            return;
        }

        java.util.Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block b = it.next();
            if (!arena.getPlacedBlocks().contains(b.getLocation())) {
                it.remove();
                continue;
            }

            if (b.getType().name().contains("GLASS") || b.getType().name().endsWith("_BED")) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onDragonExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderDragon || event.getEntityType() == EntityType.ENDER_DRAGON) {

        }
    }

    @EventHandler
    public void onDragonBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof EnderDragon || event.getEntityType() == EntityType.ENDER_DRAGON) {
        }
    }

    @EventHandler
    public void onProjectileLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;
        Player player = (Player) event.getEntity().getShooter();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME)
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta())
            return;
        if (event.getEntity() instanceof org.bukkit.entity.Egg) {
            boolean isBridgeEgg = false;
            ItemMeta im = item.getItemMeta();
            if (im != null) {
                String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "special_item");
                if ("bridge_egg".equals(special)) {
                    isBridgeEgg = true;
                }
            }

            if (isBridgeEgg) {
                Team team = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
                String colorName = (team != null && team.getColor() != null) ? team.getColor().name() : "WHITE";
                Material wool;
                wool = com.cryptomorin.xseries.XMaterial.matchXMaterial(colorName + "_WOOL")
                        .orElse(com.cryptomorin.xseries.XMaterial.WHITE_WOOL).parseMaterial();

                final Material finalWool = wool;
                new org.bukkit.scheduler.BukkitRunnable() {
                    private org.bukkit.Location lastLocation = null;

                    @Override
                    public void run() {
                        if (event.getEntity().isDead() || !event.getEntity().isValid()) {
                            this.cancel();
                            return;
                        }

                        org.bukkit.Location current = event.getEntity().getLocation();
                        if (lastLocation == null)
                            lastLocation = current.clone();

                        double dist = current.distance(lastLocation);
                        int steps = (int) Math.max(1, Math.ceil(dist * 2));
                        org.bukkit.util.Vector vec = current.clone().subtract(lastLocation).toVector()
                                .multiply(1.0 / steps);

                        for (int i = 0; i <= steps; i++) {
                            org.bukkit.Location interp = lastLocation.clone().add(vec.clone().multiply(i)).subtract(0,
                                    1,
                                    0);
                            placeBridgeBlocks(interp, finalWool, arena, event.getEntity().getVelocity());
                        }
                        lastLocation = current.clone();
                    }

                    private void placeBridgeBlocks(org.bukkit.Location loc, Material wool, Arena arena,
                            org.bukkit.util.Vector vel) {
                        org.bukkit.util.Vector dir = vel.clone().setY(0).normalize();
                        org.bukkit.util.Vector side = new org.bukkit.util.Vector(-dir.getZ(), 0, dir.getX());

                        for (int offset = -1; offset <= 1; offset++) {
                            org.bukkit.Location bLoc = loc.clone().add(side.clone().multiply(offset));
                            Block block = bLoc.getBlock();
                            if (block.getType() == Material.AIR || block.getType().name().contains("PLANT")) {
                                boolean hasPlayer = bLoc.getWorld()
                                        .getNearbyEntities(bLoc.clone().add(0.5, 0.5, 0.5), 0.3, 0.5, 0.3).stream()
                                        .anyMatch(e -> e instanceof Player);

                                if (!hasPlayer) {
                                    block.setType(wool);
                                    arena.getPlacedBlocks().add(block.getLocation());
                                }
                            }
                        }
                    }
                }.runTaskTimer(BedWars.getInstance(), 0L, 1L);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;
        Player player = (Player) event.getEntity().getShooter();
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null || arena.getState() != Arena.GameState.IN_GAME)
            return;

        if (event.getEntity() instanceof org.bukkit.entity.Snowball) {
            org.bukkit.entity.Silverfish fish = event.getEntity().getWorld().spawn(event.getEntity().getLocation(),
                    org.bukkit.entity.Silverfish.class);
            String fishName = BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                    "entity-bedbug");
            fish.setCustomName(fishName);
            fish.setCustomNameVisible(true);
            fish.addScoreboardTag("bw_mob");
            fish.addScoreboardTag(
                    "team_" + BedWars.getInstance().getGameManager().getPlayerTeam(arena, player).getName());
        }
    }

    @EventHandler
    public void onSpongePlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SPONGE)
            return;

        org.bukkit.Location loc = event.getBlock().getLocation();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (b.getType() == Material.WATER) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == org.bukkit.entity.EntityType.IRON_GOLEM
                || event.getEntityType() == org.bukkit.entity.EntityType.SILVERFISH) {
            if (event.getEntity().getScoreboardTags().contains("bw_mob")) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }

    private Team findClosestTeam(Arena arena, org.bukkit.Location loc) {
        Team closest = null;
        double distSq = Double.MAX_VALUE;
        for (Team t : arena.getTeams()) {
            if (t.getSpawnLocation() == null && t.getBedLocation() == null)
                continue;

            double d = Double.MAX_VALUE;
            if (t.getSpawnLocation() != null)
                d = Math.min(d, loc.distanceSquared(t.getSpawnLocation()));
            if (t.getBedLocation() != null)
                d = Math.min(d, loc.distanceSquared(t.getBedLocation()));

            if (d < distSq) {
                distSq = d;
                closest = t;
            }
        }
        return closest;
    }
}
