package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class UpgradeGUI extends BaseGUI {

    public UpgradeGUI() {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "upgrades-title"), 4);
    }

    @Override
    public void setContents(Player player) {
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null)
            return;
        Team team = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
        if (team == null)
            return;

        ConfigurationSection upgradesConfig = BedWars.getInstance().getLanguageManager().getConfig(player.getUniqueId(),
                "upgrades");
        if (upgradesConfig == null)
            upgradesConfig = BedWars.getInstance().getConfigManager().getUpgradesConfig(arena.getGroup());

        ConfigurationSection config = upgradesConfig.getConfigurationSection("upgrades");
        if (config == null)
            return;

        for (String key : config.getKeys(false)) {
            int currentLevel = team.getUpgradeLevel(key);
            int maxLevel = config.getInt(key + ".max-level");
            int nextLevel = currentLevel + 1;
            int slot = config.getInt(key + ".slot");
            Material icon = com.cryptomorin.xseries.XMaterial.matchXMaterial(config.getString(key + ".icon", "BARRIER"))
                    .orElse(com.cryptomorin.xseries.XMaterial.BARRIER).parseMaterial();
            String name = ChatColor.translateAlternateColorCodes('&', config.getString(key + ".name"));

            ItemBuilder builder = new ItemBuilder(icon).setName(name);

            if (nextLevel <= maxLevel) {
                ConfigurationSection tierData = config.getConfigurationSection(key + ".tiers." + nextLevel);
                if (tierData != null) {
                    int cost = tierData.getInt("cost");
                    String desc = tierData.getString("desc", "");
                    String desc2 = tierData.getString("desc2");

                    builder.addLoreLine(BedWars.getInstance().getLanguageManager()
                            .getMessage(player.getUniqueId(), "upgrade-current-level")
                            .replace("%level%",
                                    currentLevel == 0
                                            ? BedWars.getInstance().getLanguageManager().getMessage(
                                                    player.getUniqueId(),
                                                    "upgrade-none")
                                            : String.valueOf(currentLevel)));
                    builder.addLoreLine("");
                    builder.addLoreLine(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "upgrade-next-level"));
                    builder.addLoreLine("§f" + ChatColor.translateAlternateColorCodes('&', desc));
                    if (desc2 != null) {
                        builder.addLoreLine("§f" + ChatColor.translateAlternateColorCodes('&', desc2));
                    }
                    builder.addLoreLine("");
                    builder.addLoreLine(
                            BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "upgrade-cost")
                                    .replace("%amount%", String.valueOf(cost)));
                    builder.addLoreLine("");
                    builder.addLoreLine(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "upgrade-click-to-buy"));
                } else {
                    builder.addLoreLine("§cConfiguration Error");
                }
            } else {
                builder.addLoreLine(
                        BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "upgrade-max-level")
                                .replace("%level%", String.valueOf(currentLevel)));
                builder.addLoreLine("");
                builder.addLoreLine(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                        "upgrade-unlocked"));
                builder.addEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
                builder.hideAttributes();
            }

            inventory.setItem(slot, builder.build());
        }

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null)
            return;
        Team team = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
        if (team == null)
            return;

        ConfigurationSection baseConfig = BedWars.getInstance().getConfigManager().getUpgradesConfig(arena.getGroup());
        ConfigurationSection config = baseConfig.getConfigurationSection("upgrades");
        if (config == null)
            return;

        for (String key : config.getKeys(false)) {
            if (config.getInt(key + ".slot") == slot) {
                if (key.equals("traps")) {
                    new TrapsGUI().open(player);
                    return;
                }

                int currentLevel = team.getUpgradeLevel(key);
                int maxLevel = config.getInt(key + ".max-level");

                if (currentLevel >= maxLevel) {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "upgrade-already-max"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                int nextLevel = currentLevel + 1;
                ConfigurationSection tierData = config.getConfigurationSection(key + ".tiers." + nextLevel);
                int cost = tierData.getInt("cost");

                if (me.horiciastko.bedwars.utils.InventoryUtils.hasEnough(player, Material.DIAMOND, cost)) {
                    me.horiciastko.bedwars.utils.InventoryUtils.removeItems(player, Material.DIAMOND, cost);
                    team.setUpgradeLevel(key, nextLevel);

                    applyUpgradeEffects(team, key, nextLevel);

                    String msg = BedWars.getInstance().getLanguageManager().getMessage(null, "upgrade-applied");
                    String upgradeName = ChatColor.translateAlternateColorCodes('&', config.getString(key + ".name"));
                    String formatted = msg.replace("%upgrade%", upgradeName);

                    team.getMembers().forEach(m -> {
                        m.sendMessage(formatted);
                        m.playSound(m.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    });

                    open(player);
                } else {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(),
                            "upgrade-not-enough-diamonds"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
                return;
            }
        }
    }

    private void applyUpgradeEffects(Team team, String key, int level) {
        team.getMembers().forEach(p -> BedWars.getInstance().getGameManager().applyTeamUpgrades(p, team));

        if (key.equals("sharpness") || key.equals("protection")) {
            team.getMembers().forEach(p -> BedWars.getInstance().getGameManager().giveStartingKit(p, team));
        }
    }

}
