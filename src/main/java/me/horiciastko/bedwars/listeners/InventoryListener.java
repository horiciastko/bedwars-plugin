package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.gui.BaseGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null)
            return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            BaseGUI gui = (BaseGUI) holder;
            event.setCancelled(true);

            if (event.getClickedInventory().equals(event.getInventory())) {
                gui.handleAction(player, event.getSlot(), event.getCurrentItem(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (event.getInventory().getHolder() instanceof BaseGUI) {
                if (event.getInventory().getHolder() instanceof me.horiciastko.bedwars.gui.ShopGUI) {
                    me.horiciastko.bedwars.BedWars.getInstance().getShopManager().setEditingSlot(player, -1);
                }

                org.bukkit.Bukkit.getScheduler().runTaskLater(me.horiciastko.bedwars.BedWars.getInstance(), () -> {
                    if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BaseGUI) &&
                            me.horiciastko.bedwars.BedWars.getInstance().getArenaManager()
                                    .getEditArena(player) == null) {
                        me.horiciastko.bedwars.BedWars.getInstance().getVisualizationManager().hideHolograms(player);
                    }
                }, 1L);
            }
        }
    }
}
