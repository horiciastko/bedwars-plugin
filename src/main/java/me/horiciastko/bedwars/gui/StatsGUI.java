package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.PlayerStats;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatsGUI extends BaseGUI {

    public StatsGUI(Player target) {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "stats-gui-title") + " " + target.getName(), 3);
        setContents(target);
    }

    @Override
    public void setContents(Player player) {
        PlayerStats stats = BedWars.getInstance().getStatsManager().getStats(player.getUniqueId());

        ItemStack levelInfo = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§b§lLevel & Experience")
                .setLore(
                    "§7Current Level: §b" + stats.getLevel(),
                    "§7Rank: " + stats.getRank(),
                    "§7Total XP: §e" + stats.getExperience(),
                    "",
                    "§7Progress: §e" + stats.getCurrentLevelXp() + "§7/§e" + stats.getRequiredXpForNextLevel(),
                    stats.getProgressBar() + " §7" + String.format("%.1f", stats.getProgressPercent()) + "%",
                    "",
                    "§8Next Level: §b" + (stats.getLevel() + 1)
                )
                .build();
        inventory.setItem(4, levelInfo);

        ItemStack wins = new ItemBuilder(Material.DIAMOND)
                .setName("§bWins")
                .setLore("§7Total Wins: §e" + (stats != null ? stats.getWins() : 0))
                .build();
        inventory.setItem(10, wins);

        ItemStack kills = new ItemBuilder(Material.IRON_SWORD)
                .setName("§cKills")
                .setLore("§7Total Kills: §e" + (stats != null ? stats.getKills() : 0))
                .build();
        inventory.setItem(11, kills);

        ItemStack deaths = new ItemBuilder(Material.SKELETON_SKULL)
                .setName("§cDeaths")
                .setLore(
                    "§7Total Deaths: §e" + (stats != null ? stats.getDeaths() : 0),
                    "§7K/D Ratio: §e" + (stats.getDeaths() > 0 ? String.format("%.2f", (double)stats.getKills() / stats.getDeaths()) : stats.getKills())
                )
                .build();
        inventory.setItem(12, deaths);

        ItemStack finalKills = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("§5Final Kills")
                .setLore("§7Total Final Kills: §e" + (stats != null ? stats.getFinalKills() : 0))
                .build();
        inventory.setItem(13, finalKills);

        ItemStack beds = new ItemBuilder(Material.RED_BED)
                .setName("§aBeds Broken")
                .setLore("§7Total Beds Broken: §e" + (stats != null ? stats.getBedsBroken() : 0))
                .build();
        inventory.setItem(14, beds);

        int killXp = BedWars.getInstance().getLevelsManager().getXpForAction("kill");
        int finalKillXp = BedWars.getInstance().getLevelsManager().getXpForAction("final-kill");
        int bedBrokenXp = BedWars.getInstance().getLevelsManager().getXpForAction("bed-broken");
        int winXp = BedWars.getInstance().getLevelsManager().getXpForAction("win");

        ItemStack xpInfo = new ItemBuilder(Material.BOOK)
                .setName("§e§lXP Rewards")
                .setLore(
                    "§7Kill: §a+" + killXp + " XP",
                    "§7Final Kill: §a+" + finalKillXp + " XP",
                    "§7Bed Broken: §a+" + bedBrokenXp + " XP",
                    "§7Win: §a+" + winXp + " XP"
                )
                .build();
        inventory.setItem(16, xpInfo);

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
    }
}
