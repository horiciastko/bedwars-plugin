package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GeneratorManagementGUI extends BaseGUI {

    private final Arena arena;

    public GeneratorManagementGUI(Arena arena) {
        super("§8Global Generators: " + arena.getName(), 3);
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
        inventory.setItem(11, new ItemBuilder(Material.DIAMOND_BLOCK)
                .setName("§b§lDiamond Generators")
                .setLore(
                        "§7Current: §f" + arena.getDiamondGenerators().size(),
                        "",
                        "§eLeft-click to add generator at your position",
                        "§cRight-click to remove last added")
                .build());

        inventory.setItem(15, new ItemBuilder(Material.EMERALD_BLOCK)
                .setName("§a§lEmerald Generators")
                .setLore(
                        "§7Current: §f" + arena.getEmeraldGenerators().size(),
                        "",
                        "§eLeft-click to add generator at your position",
                        "§cRight-click to remove last added")
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

        if (slot == 11) {
            if (clickType == org.bukkit.event.inventory.ClickType.RIGHT) {
                if (!arena.getDiamondGenerators().isEmpty()) {
                    arena.getDiamondGenerators().remove(arena.getDiamondGenerators().size() - 1);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-diamond-removed"));
                }
            } else {
                arena.getDiamondGenerators().add(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-diamond-added-pos"));
            }
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        } else if (slot == 15) {
            if (clickType == org.bukkit.event.inventory.ClickType.RIGHT) {
                if (!arena.getEmeraldGenerators().isEmpty()) {
                    arena.getEmeraldGenerators().remove(arena.getEmeraldGenerators().size() - 1);
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-emerald-removed"));
                }
            } else {
                arena.getEmeraldGenerators().add(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-emerald-added-pos"));
            }
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        }
    }
}
