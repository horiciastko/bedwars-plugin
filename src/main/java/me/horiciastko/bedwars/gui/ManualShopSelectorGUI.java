package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ManualShopSelectorGUI extends BaseGUI {

    private final Arena arena;

    public ManualShopSelectorGUI(Arena arena) {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "manual-shop-selector-title"), 3);
        this.arena = arena;
    }

    @Override
    public void setContents(Player player) {
        int[] slots = { 10, 11, 12, 13, 14, 15, 16 };
        int i = 0;
        for (Team team : arena.getTeams()) {
            if (i >= slots.length)
                break;
            ItemStack teamItem = new ItemBuilder(team.getMaterial())
                    .setName(team.getColor() + "§l" + team.getName())
                    .setLore(
                            "§7Select this team to set NPC position.",
                            "",
                            "§eLeft-click to set §aSHOP §eNPC",
                            "§bRight-click to set §bUPGRADE §eNPC")
                    .build();

            if (teamItem.getItemMeta() != null) {
                me.horiciastko.bedwars.utils.ItemTagUtils.setTag(teamItem, "team_name", team.getName());
            }
            inventory.setItem(slots[i++], teamItem);
        }

        inventory.setItem(22, new ItemBuilder(org.bukkit.Material.ARROW)
                .setName("§7Back")
                .build());
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (slot == 22) {
            new ShopManagementGUI(arena).open(player);
            return;
        }

        if (item != null && item.hasItemMeta()) {
            String teamName = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "team_name");
            if (teamName == null)
                return;
            Team target = null;
            for (Team team : arena.getTeams()) {
                if (team.getName().equalsIgnoreCase(teamName)) {
                    target = team;
                    break;
                }
            }

            if (target != null) {
                if (clickType.isLeftClick()) {
                    target.setShopLocation(player.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-manual-set-shop").replace("%color%", target.getColor().toString()).replace("%team%", target.getName()));
                } else {
                    target.setUpgradeLocation(player.getLocation());
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "shop-manual-set-upgrade").replace("%color%", target.getColor().toString()).replace("%team%", target.getName()));
                }
                BedWars.getInstance().getArenaManager().saveArena(arena);
                BedWars.getInstance().getVisualizationManager().showHolograms(player, arena);
                open(player);
            }
        }
    }
}
