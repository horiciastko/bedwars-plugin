package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaListGUI extends BaseGUI {

    public ArenaListGUI() {
        super("§8BedWars Arenas", 6);
    }

    @Override
    public void setContents(Player player) {
        int slot = 0;
        for (Arena arena : BedWars.getInstance().getArenaManager().getArenas()) {
            if (slot >= rows * 9)
                break;

            inventory.setItem(slot++, new ItemBuilder(Material.MAP)
                    .setName("§b§lArena: §f" + arena.getName())
                    .setLore(
                            "§7World: §e" + (arena.getWorldName() != null ? arena.getWorldName() : "§cNot set"),
                            "§7Teams: §e" + arena.getTeams().size(),
                            "§7Status: §e" + arena.getState(),
                            "",
                            "§eLeft-click to edit",
                            "§cRight-click to delete")
                    .build());
        }

        inventory.setItem(48, new ItemBuilder(Material.NETHER_STAR)
                .setName("§a§lCreate New Arena")
                .setLore("§7Click to create a new arena via command.").build());

        inventory.setItem(50, new ItemBuilder(Material.BEACON)
                .setName("§b§lSet Main Lobby")
                .setLore("§7Sets the global lobby location", "§7to your current position.", "", "§e▸ Click to set")
                .build());

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build());
            }
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (slot == 48) {
            player.closeInventory();
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "arena-list-create-hint"));
            return;
        }

        if (slot == 50) {
            BedWars.getInstance().getGameManager().setMainLobbyLocation(player.getLocation());
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "arena-list-lobby-set"));
            return;
        }

        if (slot < BedWars.getInstance().getArenaManager().getArenas().size()) {
            Arena arena = BedWars.getInstance().getArenaManager().getArenas().get(slot);
            if (clickType == org.bukkit.event.inventory.ClickType.RIGHT) {
                BedWars.getInstance().getArenaManager().deleteArena(arena);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "arena-list-removed").replace("%name%", arena.getName()));
                open(player);
            } else {
                new ArenaSettingsGUI(arena).open(player);
            }
        }
    }
}
