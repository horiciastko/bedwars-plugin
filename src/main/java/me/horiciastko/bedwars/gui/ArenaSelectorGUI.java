package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ArenaSelectorGUI extends BaseGUI {

    private final Arena.ArenaMode filterMode;

    public ArenaSelectorGUI() {
        this(null);
    }

    public ArenaSelectorGUI(Arena.ArenaMode mode) {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "arena-selector-title") + 
              (mode != null ? " (" + getModeDisplay(mode) + ")" : ""), 6);
        this.filterMode = mode;
    }

    private static String getModeDisplay(Arena.ArenaMode mode) {
        BedWars plugin = BedWars.getInstance();
        org.bukkit.configuration.ConfigurationSection modesSection = plugin.getConfig()
                .getConfigurationSection("join-gui.modes");

        if (modesSection != null) {
            for (String key : modesSection.getKeys(false)) {
                int playersPerTeam = modesSection.getInt(key + ".players-per-team");
                if (mode.getPlayersPerTeam() == playersPerTeam) {
                    String displayKey = "join-gui-" + key + "-display";
                    String display = plugin.getLanguageManager().getMessage(null, displayKey);
                    return org.bukkit.ChatColor.stripColor(
                            org.bukkit.ChatColor.translateAlternateColorCodes('&', display));
                }
            }
        }
        return mode.getDisplayName();
    }

    @Override
    public void setContents(Player player) {
        List<Arena> arenas = BedWars.getInstance().getArenaManager().getArenas();

        if (!player.hasPermission("bedwars.admin")) {
            arenas = arenas.stream()
                    .filter(Arena::isEnabled)
                    .collect(Collectors.toList());
        }

        if (filterMode != null) {
            arenas = arenas.stream()
                    .filter(a -> a.getMode() == filterMode)
                    .collect(Collectors.toList());
        }

        Map<String, List<Arena>> grouped = arenas.stream().collect(Collectors.groupingBy(Arena::getGroup));

        int slot = 10;
        for (Map.Entry<String, List<Arena>> entry : grouped.entrySet()) {
            List<Arena> groupArenas = entry.getValue();

            for (Arena arena : groupArenas) {
                if (slot > 43)
                    break;
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
                if (slot % 9 == 8) {
                    slot += 2;
                }

                inventory.setItem(slot++, createArenaItem(arena));
            }
        }

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    private ItemStack createArenaItem(Arena arena) {
        java.util.UUID viewer = null;
        Material icon;
        String stateName;
        if (!arena.isEnabled()) {
            icon = com.cryptomorin.xseries.XMaterial.GRAY_TERRACOTTA.parseMaterial();
            stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-disabled");
        } else {
            switch (arena.getState()) {
                case WAITING:
                    icon = com.cryptomorin.xseries.XMaterial.LIME_TERRACOTTA.parseMaterial();
                stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-waiting");
                    break;
                case STARTING:
                    icon = com.cryptomorin.xseries.XMaterial.YELLOW_TERRACOTTA.parseMaterial();
                stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-starting");
                    break;
                case IN_GAME:
                    icon = com.cryptomorin.xseries.XMaterial.RED_TERRACOTTA.parseMaterial();
                stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-ingame");
                    break;
                case ENDING:
                    icon = com.cryptomorin.xseries.XMaterial.ORANGE_TERRACOTTA.parseMaterial();
                stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-ending");
                    break;
                default:
                    icon = com.cryptomorin.xseries.XMaterial.TERRACOTTA.parseMaterial();
                stateName = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-state-unknown");
                    break;
            }
        }

        String name = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-item-name")
            .replace("%arena%", arena.getName());
        String loreGroup = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-lore-group")
            .replace("%group%", arena.getGroup());
        String loreMode = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-lore-mode")
            .replace("%mode%", getModeDisplay(arena.getMode()));
        String lorePlayers = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-lore-players")
            .replace("%current%", String.valueOf(arena.getPlayers().size()))
            .replace("%max%", String.valueOf(arena.getMaxPlayers()));
        String loreStatus = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-lore-status")
            .replace("%status%", stateName);
        String loreClick = BedWars.getInstance().getLanguageManager().getMessage(viewer, "arena-selector-lore-click");

        return new ItemBuilder(icon)
            .setName(name)
                .setLore(
                loreGroup,
                loreMode,
                lorePlayers,
                loreStatus,
                        "",
                loreClick)
                .build();
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || item.getType().name().contains("GLASS_PANE"))
            return;

        if (item.getItemMeta() == null)
            return;
        String displayName = item.getItemMeta().getDisplayName();
        String rawName = org.bukkit.ChatColor.stripColor(displayName);
        String arenaName = rawName;

        int separator = rawName.indexOf(':');
        if (separator >= 0 && separator + 1 < rawName.length()) {
            arenaName = rawName.substring(separator + 1).trim();
        }

        Arena arena = BedWars.getInstance().getArenaManager().getArena(arenaName);
        if (arena == null) {
            for (Arena candidate : BedWars.getInstance().getArenaManager().getArenas()) {
                if (rawName.equalsIgnoreCase(candidate.getName()) || rawName.endsWith(candidate.getName())) {
                    arena = candidate;
                    break;
                }
            }
        }

        if (arena != null) {
            BedWars.getInstance().getArenaManager().joinArena(player, arena);
            player.closeInventory();
        }
    }
}
