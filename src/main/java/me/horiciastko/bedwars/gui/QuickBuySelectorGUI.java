package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.utils.ItemBuilder;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class QuickBuySelectorGUI extends BaseGUI {

    private final int page;
    private final int targetSlot;

    public QuickBuySelectorGUI(int targetSlot, int page) {
        super(BedWars.getInstance().getLanguageManager().getMessage(null, "quick-buy-selector-title"), 6);
        this.targetSlot = targetSlot;
        this.page = page;
    }

    @Override
    public void setContents(Player player) {
        ConfigurationSection shopConfig = BedWars.getInstance().getLanguageManager().getConfig(player.getUniqueId(),
                "shop");
        if (shopConfig == null)
            shopConfig = BedWars.getInstance().getConfigManager().getShopConfig();

        ItemStack background = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        List<ItemEntry> allItems = new ArrayList<>();
        ConfigurationSection sections = shopConfig.getConfigurationSection("sections");
        if (sections != null) {
            for (String catKey : sections.getKeys(false)) {
                if (catKey.equalsIgnoreCase("quick_buy"))
                    continue;

                ConfigurationSection items = sections.getConfigurationSection(catKey + ".items");
                if (items != null) {
                    for (String itemKey : items.getKeys(false)) {
                        allItems.add(new ItemEntry(catKey, itemKey, items.getConfigurationSection(itemKey)));
                    }
                }
            }
        }

        int start = page * 28;
        int end = Math.min(start + 28, allItems.size());

        int[] guiSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = start; i < end; i++) {
            ItemEntry entry = allItems.get(i);
            int guiSlot = guiSlots[i - start];

            XMaterial xMat = XMaterial.matchXMaterial(entry.data.getString("material", "BARRIER"))
                    .orElse(XMaterial.BARRIER);
            String name = ChatColor.translateAlternateColorCodes('&', entry.data.getString("name", xMat.name()));

            ItemBuilder builder = new ItemBuilder(xMat).setName(name);
            builder.addLoreLine("§eClick to add to Quick Buy!");

            inventory.setItem(guiSlot, builder.build());
        }

        if (page > 0) {
            inventory.setItem(45, new ItemBuilder(XMaterial.ARROW).setName("§aPrevious Page").build());
        }
        if (end < allItems.size()) {
            inventory.setItem(53, new ItemBuilder(XMaterial.ARROW).setName("§aNext Page").build());
        }

        inventory.setItem(49, new ItemBuilder(XMaterial.BARRIER).setName("§cBack to Shop").build());
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (slot == 49) {
            new ShopGUI("quick_buy").open(player);
            return;
        }

        if (slot == 45 && page > 0) {
            new QuickBuySelectorGUI(targetSlot, page - 1).open(player);
            return;
        }

        if (slot == 53) {
            new QuickBuySelectorGUI(targetSlot, page + 1).open(player);
            return;
        }

        int[] guiSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int indexInPage = -1;
        for (int i = 0; i < guiSlots.length; i++) {
            if (guiSlots[i] == slot) {
                indexInPage = i;
                break;
            }
        }

        if (indexInPage != -1) {
            ConfigurationSection shopConfig = BedWars.getInstance().getLanguageManager().getConfig(player.getUniqueId(),
                    "shop");
            if (shopConfig == null)
                shopConfig = BedWars.getInstance().getConfigManager().getShopConfig();

            List<ItemEntry> allItems = new ArrayList<>();
            ConfigurationSection sections = shopConfig.getConfigurationSection("sections");
            if (sections != null) {
                for (String catKey : sections.getKeys(false)) {
                    if (catKey.equalsIgnoreCase("quick_buy"))
                        continue;
                    ConfigurationSection items = sections.getConfigurationSection(catKey + ".items");
                    if (items != null) {
                        for (String itemKey : items.getKeys(false)) {
                            allItems.add(new ItemEntry(catKey, itemKey, items.getConfigurationSection(itemKey)));
                        }
                    }
                }
            }

            int absoluteIndex = (page * 28) + indexInPage;
            if (absoluteIndex < allItems.size()) {
                ItemEntry selected = allItems.get(absoluteIndex);
                BedWars.getInstance().getShopManager().updateQuickBuy(player, targetSlot, selected.category,
                        selected.itemKey);
                BedWars.getInstance().getShopManager().setEditingSlot(player, -1);

                String itemName = ChatColor.translateAlternateColorCodes('&', selected.data.getString("name", "Item"));
                player.sendMessage(
                        BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "quick-buy-added")
                                .replace("%item%", itemName).replace("%slot%", String.valueOf(targetSlot)));

                new ShopGUI("quick_buy").open(player);
            }
        }
    }

    private static class ItemEntry {
        String category;
        String itemKey;
        ConfigurationSection data;

        ItemEntry(String category, String itemKey, ConfigurationSection data) {
            this.category = category;
            this.itemKey = itemKey;
            this.data = data;
        }
    }
}
