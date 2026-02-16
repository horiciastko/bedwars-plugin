package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaLocationsGUI extends BaseGUI {

    private final Arena arena;

    public ArenaLocationsGUI(Arena arena) {
        super("§8§l✦ §9" + arena.getName() + " §8§l✦ §7Locations", 4);
        this.arena = arena;
    }

    @Override
    public void setContents(Player player) {
        fillBorders(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build());

        inventory.setItem(11, new ItemBuilder(Material.IRON_BLOCK)
                .setName("§fSet Arena §a§lPOS 1")
                .setLore("§7Current: " + formatLoc(arena.getPos1()), "", "§e▸ Click to set to your location")
                .build());

        inventory.setItem(12, new ItemBuilder(Material.IRON_BLOCK)
                .setName("§fSet Arena §a§lPOS 2")
                .setLore("§7Current: " + formatLoc(arena.getPos2()), "", "§e▸ Click to set to your location")
                .build());

        inventory.setItem(13, new ItemBuilder(Material.GOLD_BLOCK)
                .setName("§fSet Arena §e§lLOBBY SPAWN")
                .setLore("§7Where players wait before game", "§7Current: " + formatLoc(arena.getLobbyLocation()), "",
                        "§e▸ Click to set to your location")
                .build());

        inventory.setItem(21, new ItemBuilder(Material.RED_STAINED_GLASS)
                .setName("§cSet Lobby Remove §lPOS 1")
                .setLore("§7Area to clear when game starts", "§7Current: " + formatLoc(arena.getLobbyPos1()), "",
                        "§e▸ Click to set to your location")
                .build());

        inventory.setItem(22, new ItemBuilder(Material.RED_STAINED_GLASS)
                .setName("§cSet Lobby Remove §lPOS 2")
                .setLore("§7Area to clear when game starts", "§7Current: " + formatLoc(arena.getLobbyPos2()), "",
                        "§e▸ Click to set to your location")
                .build());

        inventory.setItem(31, new ItemBuilder(Material.ARROW)
                .setName("§6§l← Back")
                .build());
    }

    private String formatLoc(org.bukkit.Location loc) {
        if (loc == null)
            return "§cNot set";
        return String.format("§a%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        switch (slot) {
            case 11:
                arena.setPos1(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "location-pos1-set"));
                open(player);
                break;
            case 12:
                arena.setPos2(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "location-pos2-set"));
                open(player);
                break;
            case 13:
                arena.setLobbyLocation(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "location-lobby-set"));
                open(player);
                break;
            case 21:
                arena.setLobbyPos1(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "location-lobby-remove-pos1"));
                open(player);
                break;
            case 22:
                arena.setLobbyPos2(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "location-lobby-remove-pos2"));
                open(player);
                break;
            case 31:
                new ArenaSettingsGUI(arena).open(player);
                break;
        }
        BedWars.getInstance().getArenaManager().saveArena(arena);
    }
}
