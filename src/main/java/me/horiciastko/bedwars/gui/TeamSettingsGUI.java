package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class TeamSettingsGUI extends BaseGUI {

    private final Arena arena;
    private final Team team;

    public TeamSettingsGUI(Arena arena, Team team) {
        super("§8" + team.getColor() + "⬛ §8Team: " + team.getColor() + team.getDisplayName(), 3);
        this.arena = arena;
        this.team = team;
    }

    @Override
    public void open(Player player) {
        super.open(player);
        BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
    }

    @Override
    public void setContents(Player player) {
        Material teamGlass = getTeamGlass(team.getColor());
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemBuilder(teamGlass).setName(" ").build());
            inventory.setItem(18 + i, new ItemBuilder(teamGlass).setName(" ").build());
        }

        inventory.setItem(10, new ItemBuilder(Material.ENDER_PEARL)
                .setName(team.getColor() + "§lSet Team Spawn")
                .setLore("§7Current: " + formatLocation(team.getSpawnLocation()),
                        "",
                        "§aLeft-click §7to set to your location",
                        "§cRight-click §7to clear")
                .build());

        inventory.setItem(11, new ItemBuilder(getTeamBed(team.getColor()))
                .setName(team.getColor() + "§lSet Team Bed")
                .setLore("§7Current: " + formatLocation(team.getBedLocation()),
                        "",
                        "§eClick §7to auto-detect nearby bed",
                        "§7(within 5 blocks)")
                .build());

        inventory.setItem(12, new ItemBuilder(Material.IRON_BLOCK)
                .setName(team.getColor() + "§lSet Team Generator")
                .setLore("§7Generators: §e" + team.getGenerators().size(),
                        "",
                        "§aLeft-click §7to add generator here",
                        "§cRight-click §7to clear all generators")
                .build());

        inventory.setItem(14, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .setName("§a§lSet Shop NPC")
                .setLore("§7Current: " + formatLocation(team.getShopLocation()),
                        "",
                        "§aLeft-click §7to set to your location",
                        "§cRight-click §7to clear")
                .build());

        inventory.setItem(15, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§b§lSet Upgrade NPC")
                .setLore("§7Current: " + formatLocation(team.getUpgradeLocation()),
                        "",
                        "§aLeft-click §7to set to your location",
                        "§cRight-click §7to clear")
                .build());

        inventory.setItem(16, new ItemBuilder(team.getMaterial())
                .setName(team.getColor() + "§lTeam Info")
                .setLore("§7Color: " + team.getColor() + team.getColor().name(),
                        "§7Spawn: " + (team.getSpawnLocation() != null ? "§aSet" : "§cNot set"),
                        "§7Bed: " + (team.getBedLocation() != null ? "§aSet" : "§cNot set"),
                        "§7Shop: " + (team.getShopLocation() != null ? "§aSet" : "§cNot set"),
                        "§7Upgrade: " + (team.getUpgradeLocation() != null ? "§aSet" : "§cNot set"),
                        "§7Generators: §e" + team.getGenerators().size())
                .build());

        inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                .setName("§c§lBack to Teams")
                .setLore("§7Return to team list")
                .build());

        for (int i = 9; i < 18; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
            }
        }
    }

    private String formatLocation(org.bukkit.Location loc) {
        if (loc == null)
            return "§cNot Set";
        return String.format("§e%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }

    private Material getTeamGlass(org.bukkit.ChatColor color) {
        switch (color) {
            case RED:
                return Material.RED_STAINED_GLASS_PANE;
            case BLUE:
                return Material.BLUE_STAINED_GLASS_PANE;
            case GREEN:
                return Material.LIME_STAINED_GLASS_PANE;
            case YELLOW:
                return Material.YELLOW_STAINED_GLASS_PANE;
            case AQUA:
                return Material.CYAN_STAINED_GLASS_PANE;
            case WHITE:
                return Material.WHITE_STAINED_GLASS_PANE;
            case LIGHT_PURPLE:
                return Material.PINK_STAINED_GLASS_PANE;
            case GRAY:
                return Material.GRAY_STAINED_GLASS_PANE;
            default:
                return Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        }
    }

    private Material getTeamBed(org.bukkit.ChatColor color) {
        switch (color) {
            case RED:
                return Material.RED_BED;
            case BLUE:
                return Material.BLUE_BED;
            case GREEN:
                return Material.LIME_BED;
            case YELLOW:
                return Material.YELLOW_BED;
            case AQUA:
                return Material.CYAN_BED;
            case WHITE:
                return Material.WHITE_BED;
            case LIGHT_PURPLE:
                return Material.PINK_BED;
            case GRAY:
                return Material.GRAY_BED;
            default:
                return Material.WHITE_BED;
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || !item.hasItemMeta())
            return;

        boolean save = false;

        switch (slot) {
            case 22:
                new TeamManagementGUI(arena).open(player);
                return;
            case 10:
                if (clickType.isRightClick()) {
                    team.setSpawnLocation(null);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-spawn-cleared"));
                } else {
                    team.setSpawnLocation(player.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-spawn-set"));
                }
                save = true;
                break;
            case 11:
                Block bed = findNearbyBed(player.getLocation(), 5);
                if (bed != null) {
                    team.setBedLocation(bed.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-bed-detected").replace("%location%", formatLocation(bed.getLocation())));
                } else {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-bed-not-found"));
                }
                save = true;
                break;
            case 12:
                if (clickType.isRightClick()) {
                    team.getGenerators().clear();
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-generators-cleared"));
                } else {
                    team.getGenerators().add(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-generator-added"));
                }
                save = true;
                break;
            case 14:
                if (clickType.isRightClick()) {
                    team.setShopLocation(null);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-shop-cleared"));
                } else {
                    team.setShopLocation(player.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-shop-set"));
                }
                save = true;
                break;
            case 15:
                if (clickType.isRightClick()) {
                    team.setUpgradeLocation(null);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-upgrade-cleared"));
                } else {
                    team.setUpgradeLocation(player.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-upgrade-set"));
                }
                save = true;
                break;
        }

        if (save) {
            BedWars.getInstance().getArenaManager().saveArena(arena);
            BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
            open(player);
        }
    }

    private Block findNearbyBed(org.bukkit.Location location, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = location.clone().add(x, y, z).getBlock();
                    if (b.getType().name().contains("_BED")) {
                        return b;
                    }
                }
            }
        }
        return null;
    }
}
