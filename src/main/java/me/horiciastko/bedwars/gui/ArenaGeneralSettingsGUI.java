package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaGeneralSettingsGUI extends BaseGUI {

    private final Arena arena;

    public ArenaGeneralSettingsGUI(Arena arena) {
        super("§8General Settings: §1" + arena.getName(), 3);
        this.arena = arena;
    }

    @Override
    public void setContents(Player player) {
        inventory.setItem(10, new ItemBuilder(Material.LEVER)
                .setName("§e§lMin Players")
                .setLore(
                        "§7Current: §f" + arena.getMinPlayers(),
                        "",
                        "§eLeft-click to +1",
                        "§cRight-click to -1")
                .build());

        inventory.setItem(12, new ItemBuilder(Material.REPEATER)
                .setName("§e§lMax Players")
                .setLore(
                        "§7Current: §f" + arena.getMaxPlayers(),
                        "",
                        "§eLeft-click to +1",
                        "§cRight-click to -1")
                .build());

        inventory.setItem(14, new ItemBuilder(Material.PAPER)
                .setName("§e§lGame Mode")
                .setLore(
                        "§7Current: §b" + arena.getMode().getDisplayName(),
                        "",
                        "§eClick to cycle modes")
                .build());

        inventory.setItem(16, new ItemBuilder(Material.IRON_SWORD)
                .setName("§e§lPVP Mode")
                .setLore(
                        "§7Current: §a" + arena.getPvpMode().getDisplayName(),
                        "",
                        "§eClick to toggle 1.8 / 1.16 PVP")
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
            if (clickType.isLeftClick())
                arena.setMinPlayers(arena.getMinPlayers() + 1);
            else if (arena.getMinPlayers() > 2)
                arena.setMinPlayers(arena.getMinPlayers() - 1);
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        } else if (slot == 12) {
            if (clickType.isLeftClick())
                arena.setMaxPlayers(arena.getMaxPlayers() + 1);
            else if (arena.getMaxPlayers() > arena.getMinPlayers())
                arena.setMaxPlayers(arena.getMaxPlayers() - 1);
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        } else if (slot == 14) {
            Arena.ArenaMode[] modes = Arena.ArenaMode.values();
            int nextIndex = (arena.getMode().ordinal() + 1) % modes.length;
            arena.setMode(modes[nextIndex]);
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        } else if (slot == 16) {
            Arena.PvpMode[] modes = Arena.PvpMode.values();
            int nextIndex = (arena.getPvpMode().ordinal() + 1) % modes.length;
            arena.setPvpMode(modes[nextIndex]);
            BedWars.getInstance().getArenaManager().saveArena(arena);
            open(player);
        }
    }
}
