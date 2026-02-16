package me.horiciastko.bedwars.listeners;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.npc.BedWarsNPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;

public class NPCListener implements Listener {

    private final BedWars plugin;

    public NPCListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        try {
            if (event.getHand() != EquipmentSlot.HAND)
                return;
        } catch (NoSuchMethodError ignored) {
        }

        Entity entity = event.getRightClicked();

        boolean isBwNpc = false;
        try {
            Set<String> tags = entity.getScoreboardTags();
            isBwNpc = tags.contains("bw_npc");
        } catch (NoSuchMethodError ignored) {
            isBwNpc = plugin.getNpcManager().getNPCByEntity(entity.getUniqueId()) != null;
        }

        if (isBwNpc) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            BedWarsNPC npc = plugin.getNpcManager().getNPCByEntity(entity.getUniqueId());
            if (npc != null) {
                npc.onClick(player);
            } else {
                try {
                    for (String tag : entity.getScoreboardTags()) {
                        if (tag.startsWith("bw_npc_type_")) {
                            String type = tag.substring("bw_npc_type_".length());
                            handleNpcClick(player, type);
                            return;
                        }
                    }
                } catch (NoSuchMethodError ignored) {
                }
            }
        }
    }
    private void handleNpcClick(Player player, String type) {
        if (type.equalsIgnoreCase("shop")) {
            new me.horiciastko.bedwars.gui.ShopGUI().open(player);
        } else if (type.equalsIgnoreCase("upgrades")) {
            new me.horiciastko.bedwars.gui.UpgradeGUI().open(player);
        } else {
            String modeStr = plugin.getNpcManager().getConfig().getString("types." + type + ".mode");
            if (modeStr != null) {
                try {
                    me.horiciastko.bedwars.models.Arena.ArenaMode mode = me.horiciastko.bedwars.models.Arena.ArenaMode
                            .valueOf(modeStr.toUpperCase());
                    joinQuickGame(player, mode);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "npc-invalid-mode").replace("%mode%", modeStr));
                }
            }
        }
    }

    private void joinQuickGame(Player player, me.horiciastko.bedwars.models.Arena.ArenaMode mode) {
        me.horiciastko.bedwars.models.Arena target = plugin.getArenaManager().getArenas().stream()
                .filter(a -> a.getMode() == mode
                        && a.getState() == me.horiciastko.bedwars.models.Arena.GameState.WAITING && a.isEnabled())
                .filter(a -> a.getPlayers().size() < a.getMaxPlayers())
                .findFirst().orElse(null);

        if (target != null) {
            plugin.getArenaManager().joinArena(player, target);
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "npc-no-games").replace("%mode%", mode.getDisplayName()));
        }
    }
}
