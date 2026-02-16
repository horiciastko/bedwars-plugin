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
public class TrapsGUI extends BaseGUI {

    public TrapsGUI() {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "traps-gui-title"), 4);
    }

    @Override
    public void setContents(Player player) {
        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null)
            return;
        Team team = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
        if (team == null)
            return;

        ConfigurationSection upgradesConfig = BedWars.getInstance().getConfigManager()
                .getUpgradesConfig(arena.getGroup());
        ConfigurationSection trapTypes = upgradesConfig.getConfigurationSection("trap-types");
        if (trapTypes == null)
            return;

        int activeTraps = team.getActiveTraps().size();
        int limit = upgradesConfig.getInt("trap-settings.queue-limit", 3);
        int baseCost = upgradesConfig.getInt("trap-settings.base-cost", 1);
        int increment = upgradesConfig.getInt("trap-settings.increment-cost", 1);

        int currentCost = baseCost + (activeTraps * increment);

        for (String key : trapTypes.getKeys(false)) {
            ConfigurationSection trapData = trapTypes.getConfigurationSection(key);
            int slot = trapData.getInt("slot");
            Material icon = com.cryptomorin.xseries.XMaterial.matchXMaterial(trapData.getString("icon", "BARRIER"))
                    .orElse(com.cryptomorin.xseries.XMaterial.BARRIER).parseMaterial();
            String name = ChatColor.translateAlternateColorCodes('&', trapData.getString("name"));

            String desc = ChatColor.translateAlternateColorCodes('&', trapData.getString("desc", ""));
            String desc2 = trapData.getString("desc2");

            ItemBuilder builder = new ItemBuilder(icon).setName(name);
            if (!desc.isEmpty()) {
                builder.addLoreLine(desc);
            }
            if (desc2 != null && !desc2.isEmpty()) {
                builder.addLoreLine(ChatColor.translateAlternateColorCodes('&', desc2));
            }

            builder.addLoreLine("");
            builder.addLoreLine("§7Cost: §b" + currentCost + " Diamonds");
            builder.addLoreLine("");

            if (activeTraps >= limit) {
                builder.addLoreLine("§cQueue Full!");
            } else {
                builder.addLoreLine("§eClick to queue!");
            }

            inventory.setItem(slot, builder.build());
        }

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }

        inventory.setItem(31, new ItemBuilder(Material.ARROW).setName("§aGo Back").build());
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        if (slot == 31 && item.getType() == Material.ARROW) {
            new UpgradeGUI().open(player);
            return;
        }

        Arena arena = BedWars.getInstance().getArenaManager().getPlayerArena(player);
        if (arena == null)
            return;
        Team team = BedWars.getInstance().getGameManager().getPlayerTeam(arena, player);
        if (team == null)
            return;

        ConfigurationSection upgradesConfig = BedWars.getInstance().getConfigManager()
                .getUpgradesConfig(arena.getGroup());
        ConfigurationSection trapTypes = upgradesConfig.getConfigurationSection("trap-types");

        for (String key : trapTypes.getKeys(false)) {
            if (trapTypes.getInt(key + ".slot") == slot) {
                int activeTraps = team.getActiveTraps().size();
                int limit = upgradesConfig.getInt("trap-settings.queue-limit", 3);

                if (activeTraps >= limit) {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "trap-queue-full"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                int baseCost = upgradesConfig.getInt("trap-settings.base-cost", 1);
                int increment = upgradesConfig.getInt("trap-settings.increment-cost", 1);
                int cost = baseCost + (activeTraps * increment);

                if (me.horiciastko.bedwars.utils.InventoryUtils.hasEnough(player, Material.DIAMOND, cost)) {
                    me.horiciastko.bedwars.utils.InventoryUtils.removeItems(player, Material.DIAMOND, cost);

                    team.getActiveTraps().add(key);

                    String trapName = ChatColor.translateAlternateColorCodes('&', trapTypes.getString(key + ".name"));
                    String msg = BedWars.getInstance().getConfigManager().getMessagesConfig().getString("trap-queued",
                            "&a%trap% queued!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%trap%", trapName)));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                    setContents(player);
                } else {
                    player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "trap-not-enough-diamonds"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
                return;
            }
        }
    }
}
