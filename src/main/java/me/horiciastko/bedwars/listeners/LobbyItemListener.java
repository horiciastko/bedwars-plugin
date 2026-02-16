package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyItemListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (isLobbyItem(item)) {
            Player player = event.getPlayer();
            if (BedWars.getInstance().getArenaManager().getPlayerArena(player) != null) {
                BedWars.getInstance().getArenaManager().leaveArena(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isLobbyItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (isLobbyItem(event.getCurrentItem()) || isLobbyItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (isLobbyItem(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isLobbyItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        String special = me.horiciastko.bedwars.utils.ItemTagUtils.getTag(item, "special_item");
        return "lobby_leave".equals(special);
    }
}
