package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopManagementGUI extends BaseGUI {

    private final Arena arena;

    public ShopManagementGUI(Arena arena) {
        super("§8Step 5: Shop Villagers", 3);
        this.arena = arena;
    }

    @Override
    public void open(Player player) {
        super.open(player);
        BedWars.getInstance().getArenaManager().setEditArena(player, arena);
        BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
    }

    @Override
    public void setContents(Player player) {
        inventory.setItem(10, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .setName("§a§lAuto SHOP NPC")
                .setLore(
                        "§7Automatically sets §aSHOP §7NPC",
                        "§7for the closest team base.",
                        "",
                        "§eClick to set at your position")
                .build());

        inventory.setItem(12, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§b§lAuto UPGRADE NPC")
                .setLore(
                        "§7Automatically sets §bUPGRADE §7NPC",
                        "§7for the closest team base.",
                        "",
                        "§eClick to set at your position")
                .build());

        inventory.setItem(14, new ItemBuilder(Material.CHEST)
                .setName("§e§lManual Selection")
                .setLore(
                        "§7Manually pick a team to set",
                        "§7their shop/upgrade NPC location.",
                        "",
                        "§eClick to pick team and set")
                .build());

        inventory.setItem(22, new ItemBuilder(Material.ARROW)
                .setName("§7Go Back")
                .build());

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build());
            }
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (slot == 22) {
            new ArenaSettingsGUI(arena).open(player);
            return;
        }

        if (slot == 10) {
            Team closest = findClosestTeam(player.getLocation());
            if (closest != null) {
                closest.setShopLocation(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-auto-set-shop").replace("%color%", closest.getColor().toString()).replace("%team%", closest.getName()));
                BedWars.getInstance().getArenaManager().saveArena(arena);
                BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
            } else {
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-no-team-nearby"));
            }
        } else if (slot == 12) {
            Team closest = findClosestTeam(player.getLocation());
            if (closest != null) {
                closest.setUpgradeLocation(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-auto-set-upgrade").replace("%color%", closest.getColor().toString()).replace("%team%", closest.getName()));
                BedWars.getInstance().getArenaManager().saveArena(arena);
                BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
            } else {
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-no-team-nearby"));
            }
        } else if (slot == 14) {
            new ManualShopSelectorGUI(arena).open(player);
        }
    }

    private Team findClosestTeam(org.bukkit.Location loc) {
        Team closest = null;
        double minDist = Double.MAX_VALUE;

        for (Team team : arena.getTeams()) {
            if (team.getSpawnLocation() != null) {
                double dist = team.getSpawnLocation().distanceSquared(loc);
                if (dist < minDist) {
                    minDist = dist;
                    closest = team;
                }
            }
            if (team.getBedLocation() != null) {
                double dist = team.getBedLocation().distanceSquared(loc);
                if (dist < minDist) {
                    minDist = dist;
                    closest = team;
                }
            }
        }

        return (minDist < 30 * 30) ? closest : null;
    }
}
