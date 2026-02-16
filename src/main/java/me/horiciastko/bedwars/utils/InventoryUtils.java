package me.horiciastko.bedwars.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static boolean hasEnough(Player player, Material material, int amount) {
        if (amount <= 0)
            return true;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount)
                    return true;
            }
        }
        return false;
    }

    public static void removeItems(Player player, Material material, int amount) {
        if (amount <= 0)
            return;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    contents[i] = null;
                    if (amount <= 0)
                        break;
                }
            }
        }
        player.getInventory().setContents(contents);
    }

    public static boolean canFit(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return true;

        ItemStack[] contents = player.getInventory().getStorageContents();
        int remaining = item.getAmount();

        for (ItemStack slot : contents) {
            if (slot == null || slot.getType() == Material.AIR) {
                return true;
            }
            if (slot.isSimilar(item)) {
                remaining -= (slot.getMaxStackSize() - slot.getAmount());
                if (remaining <= 0)
                    return true;
            }
        }
        return false;
    }
}
