package me.horiciastko.bedwars.logic;

import lombok.RequiredArgsConstructor;
import me.horiciastko.bedwars.BedWars;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ShopManager {

    private final BedWars plugin;
    private final Map<UUID, Map<Integer, String[]>> quickBuyCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> editingSlot = new ConcurrentHashMap<>();

    public Map<Integer, String[]> getPlayerQuickBuy(Player player) {
        return quickBuyCache.computeIfAbsent(player.getUniqueId(),
                uuid -> plugin.getDatabaseManager().getPlayerQuickBuy(uuid));
    }

    public void updateQuickBuy(Player player, int slot, String category, String itemKey) {
        getPlayerQuickBuy(player).put(slot, new String[] { category, itemKey });
        plugin.getDatabaseManager().savePlayerQuickBuy(player.getUniqueId(), slot, category, itemKey);
    }

    public void removeQuickBuy(Player player, int slot) {
        getPlayerQuickBuy(player).remove(slot);
        plugin.getDatabaseManager().removePlayerQuickBuy(player.getUniqueId(), slot);
    }

    public void setEditingSlot(Player player, int slot) {
        if (slot == -1) {
            editingSlot.remove(player.getUniqueId());
        } else {
            editingSlot.put(player.getUniqueId(), slot);
        }
    }

    public Integer getEditingSlot(Player player) {
        return editingSlot.get(player.getUniqueId());
    }

    public boolean isEditing(Player player) {
        return editingSlot.containsKey(player.getUniqueId());
    }

    public void processPurchase() {
    }
}
