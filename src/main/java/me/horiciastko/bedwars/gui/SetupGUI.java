package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class SetupGUI extends BaseGUI {

    private final Arena arena;

    public SetupGUI(Arena arena) {
        super("§8Setup: " + arena.getName(), 5);
        this.arena = arena;
    }

    @Override
    public void setContents(Player player) {
        inventory.setItem(10, new ItemBuilder(Material.GRASS_BLOCK)
                .setName("§aSet Arena World")
                .setLore("§7Current: " + (arena.getWorldName() != null ? "§e" + arena.getWorldName() : "§cNot Set"),
                        "",
                        "§eClick to open world selector",
                        "§7You will be teleported to the selected world")
                .build());

        inventory.setItem(11, new ItemBuilder(Material.COMPASS)
                .setName("§aSet Waiting Lobby")
                .setLore("§7Current: " + (arena.getLobbyLocation() != null ? "§aSet" : "§cNot Set"),
                        "",
                        "§eClick to set to your location")
                .build());

        inventory.setItem(12, new ItemBuilder(com.cryptomorin.xseries.XMaterial.WOODEN_AXE)
                .setName("§aSet Arena Pos1")
                .setLore("§7Current: " + formatLocation(arena.getPos1()),
                        "",
                        "§eClick to set to your location",
                        "§7Right-click to clear")
                .build());

        inventory.setItem(13, new ItemBuilder(com.cryptomorin.xseries.XMaterial.GOLDEN_AXE)
                .setName("§aSet Arena Pos2")
                .setLore("§7Current: " + formatLocation(arena.getPos2()),
                        "",
                        "§eClick to set to your location",
                        "§7Right-click to clear")
                .build());

        boolean enabled = arena.isEnabled();
        inventory.setItem(16, new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(enabled ? "§aArena Enabled" : "§cArena Disabled")
                .setLore("§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"),
                        "",
                        "§eClick to toggle")
                .build());

        inventory.setItem(20, new ItemBuilder(Material.WHITE_BED)
                .setName("§aManage Teams")
                .setLore("§7Click to manage team spawns, beds, etc.",
                        "§7Teams: §e" + arena.getTeams().size())
                .build());

        inventory.setItem(24, new ItemBuilder(Material.DIAMOND)
                .setName("§aManage Generators")
                .setLore("§7Click to manage Diamond/Emerald generators",
                        "§7Diamond: §b" + arena.getDiamondGenerators().size(),
                        "§7Emerald: §a" + arena.getEmeraldGenerators().size())
                .build());

        inventory.setItem(29, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§aMin Players")
                .setLore("§7Current: §e" + arena.getMinPlayers(),
                        "",
                        "§aLeft-click: +1",
                        "§cRight-click: -1",
                        "§7Shift-click: ±5")
                .build());

        inventory.setItem(30, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§aMax Players")
                .setLore("§7Current: §e" + arena.getMaxPlayers(),
                        "",
                        "§aLeft-click: +1",
                        "§cRight-click: -1",
                        "§7Shift-click: ±5")
                .build());

        inventory.setItem(32, new ItemBuilder(Material.IRON_SWORD)
                .setName("§aGame Mode")
                .setLore("§7Current: §e" + arena.getMode().getDisplayName(),
                        "§7Players per team: §e" + arena.getMode().getPlayersPerTeam(),
                        "",
                        "§eClick to cycle modes")
                .build());

        inventory.setItem(33, new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("§aPvP Mode")
                .setLore("§7Current: §e" + arena.getPvpMode().getDisplayName(),
                        "",
                        "§7LEGACY_1_8: No attack cooldown",
                        "§7MODERN_1_16: Attack cooldown enabled",
                        "",
                        "§eClick to toggle")
                .build());

        inventory.setItem(40, new ItemBuilder(Material.EMERALD_BLOCK)
                .setName("§a§lSAVE AND EXIT")
                .setLore("§7Click to save changes and stop editing")
                .build());

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    private String formatLocation(org.bukkit.Location loc) {
        if (loc == null)
            return "§cNot Set";
        return String.format("§a%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || !item.hasItemMeta())
            return;
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Set Arena World")) {
            new WorldSelectorGUI(arena).open(player);
        } else if (name.contains("Set Waiting Lobby")) {
            arena.setLobbyLocation(player.getLocation());
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-lobby-set"));
            setContents(player);
        } else if (name.contains("Set Arena Pos1")) {
            if (clickType.isRightClick()) {
                arena.setPos1(null);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-pos1-cleared"));
            } else {
                arena.setPos1(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-pos1-set"));
            }
            setContents(player);
        } else if (name.contains("Set Arena Pos2")) {
            if (clickType.isRightClick()) {
                arena.setPos2(null);
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-pos2-cleared"));
            } else {
                arena.setPos2(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-pos2-set"));
            }
            setContents(player);
        } else if (name.contains("Arena Enabled") || name.contains("Arena Disabled")) {
            arena.setEnabled(!arena.isEnabled());
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), arena.isEnabled() ? "setup-arena-enabled" : "setup-arena-disabled"));
            setContents(player);
        } else if (name.contains("Manage Teams")) {
            new TeamListGUI(arena).open(player);
        } else if (name.contains("Manage Generators")) {
            new GeneratorSetupGUI(arena).open(player);
        } else if (name.contains("Min Players")) {
            int change = clickType.isShiftClick() ? 5 : 1;
            if (clickType.isRightClick())
                change = -change;
            int newValue = Math.max(1, arena.getMinPlayers() + change);
            arena.setMinPlayers(newValue);
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-min-players-set").replace("%value%", String.valueOf(newValue)));
            setContents(player);
        } else if (name.contains("Max Players")) {
            int change = clickType.isShiftClick() ? 5 : 1;
            if (clickType.isRightClick())
                change = -change;
            int newValue = Math.max(arena.getMinPlayers(), arena.getMaxPlayers() + change);
            arena.setMaxPlayers(newValue);
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-max-players-set").replace("%value%", String.valueOf(newValue)));
            setContents(player);
        } else if (name.contains("Game Mode")) {
            Arena.ArenaMode[] modes = Arena.ArenaMode.values();
            int currentIndex = arena.getMode().ordinal();
            int nextIndex = (currentIndex + 1) % modes.length;
            arena.setMode(modes[nextIndex]);
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-mode-set").replace("%mode%", arena.getMode().getDisplayName()));
            setContents(player);
        } else if (name.contains("PvP Mode")) {
            if (arena.getPvpMode() == Arena.PvpMode.LEGACY_1_8) {
                arena.setPvpMode(Arena.PvpMode.MODERN_1_16);
            } else {
                arena.setPvpMode(Arena.PvpMode.LEGACY_1_8);
            }
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-pvp-set").replace("%mode%", arena.getPvpMode().getDisplayName()));
            setContents(player);
        } else if (name.contains("SAVE AND EXIT")) {
            BedWars.getInstance().getArenaManager().saveArena(arena);
            BedWars.getInstance().getArenaManager().stopEditing(player);
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "setup-saved"));
            player.closeInventory();
        }
    }
}
