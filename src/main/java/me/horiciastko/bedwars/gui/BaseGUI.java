package me.horiciastko.bedwars.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class BaseGUI implements InventoryHolder {

    protected final Inventory inventory;
    protected final String title;
    protected final int rows;

    public BaseGUI(String title, int rows) {
        this.title = title;
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9,
                org.bukkit.ChatColor.translateAlternateColorCodes('&', title));
    }

    public abstract void setContents(Player player);

    public void open(Player player) {
        inventory.clear();
        setContents(player);
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void fillBorders(ItemStack item) {
        for (int i = 0; i < 9; i++)
            inventory.setItem(i, item);
        for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++)
            inventory.setItem(i, item);
        for (int i = 0; i < inventory.getSize(); i += 9)
            inventory.setItem(i, item);
        for (int i = 8; i < inventory.getSize(); i += 9)
            inventory.setItem(i, item);
    }

    public abstract void handleAction(Player player, int slot, ItemStack item,
            org.bukkit.event.inventory.ClickType clickType);
}
