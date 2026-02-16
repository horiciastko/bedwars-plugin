package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class TeamColorSelectorGUI extends BaseGUI {

    private final Arena arena;

    public TeamColorSelectorGUI(Arena arena) {
        super("§8Select Team Color", 3);
        this.arena = arena;
    }

    @Override
    public void setContents(Player player) {
        int slot = 0;
        for (ChatColor color : ChatColor.values()) {
            if (!color.isColor())
                continue;

            Material wool = getWoolFromColor(color);
            if (wool == null)
                continue;

            boolean exists = arena.getTeams().stream().anyMatch(t -> t.getColor() == color);
            if (exists)
                continue;

            inventory.setItem(slot++, new ItemBuilder(wool)
                    .setName(color + color.name())
                    .setLore("§eClick to create team")
                    .build());
            if (slot >= inventory.getSize())
                break;
        }

        inventory.setItem(26, new ItemBuilder(Material.ARROW).setName("§cBack").build());
    }

    private Material getWoolFromColor(ChatColor color) {
        return com.cryptomorin.xseries.XMaterial.matchXMaterial(color.name() + "_WOOL")
                .orElse(com.cryptomorin.xseries.XMaterial.WHITE_WOOL).parseMaterial();
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || !item.hasItemMeta())
            return;
        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            new TeamListGUI(arena).open(player);
            return;
        }

        String colorName = ChatColor.stripColor(name);
        try {
            ChatColor color = ChatColor.valueOf(colorName);
            String teamName = colorName.substring(0, 1).toUpperCase() + colorName.substring(1).toLowerCase();

            Team team = new Team(teamName, teamName, color, item.getType());
            arena.getTeams().add(team);
            BedWars.getInstance().getArenaManager().saveArena(arena);

            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-created").replace("%name%", teamName));
            new TeamListGUI(arena).open(player);
        } catch (Exception e) {
            player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "team-create-error"));
        }
    }
}
