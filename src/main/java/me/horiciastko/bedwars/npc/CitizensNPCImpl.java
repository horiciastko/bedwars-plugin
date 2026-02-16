package me.horiciastko.bedwars.npc;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.gui.JoinGUI;
import me.horiciastko.bedwars.gui.ShopGUI;
import me.horiciastko.bedwars.gui.UpgradeGUI;
import me.horiciastko.bedwars.models.Arena;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class CitizensNPCImpl implements BedWarsNPC {

    private final BedWars plugin;
    private final String type;
    private NPC npc;

    public CitizensNPCImpl(BedWars plugin, String type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public void spawn(Location location) {
        String title = plugin.getNpcManager().getConfig().getString("types." + type + ".title", type);
        String skin = plugin.getNpcManager().getConfig().getString("types." + type + ".skin", "");
        String entityTypeStr = plugin.getNpcManager().getConfig().getString("types." + type + ".entity-type", "PLAYER");

        EntityType entType = EntityType.PLAYER;
        try {
            entType = EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger()
                    .warning("Invalid entity type '" + entityTypeStr + "' for NPC " + type + ". Defaulting to PLAYER.");
        }

        npc = CitizensAPI.getNPCRegistry().createNPC(entType, title);
        npc.getOrAddTrait(LookClose.class).lookClose(true);

        if (entType == EntityType.PLAYER && !skin.isEmpty()) {
            npc.getOrAddTrait(net.citizensnpcs.trait.SkinTrait.class).setSkinName(skin);
        }

        List<String> descriptionLines = new java.util.ArrayList<>();
        if (plugin.getNpcManager().getConfig().isList("types." + type + ".description")) {
            descriptionLines = plugin.getNpcManager().getConfig().getStringList("types." + type + ".description");
        } else {
            String s = plugin.getNpcManager().getConfig().getString("types." + type + ".description", "");
            if (!s.isEmpty())
                descriptionLines.add(s);
        }

        if (!descriptionLines.isEmpty()) {

            try {
                net.citizensnpcs.trait.HologramTrait holo = npc
                        .getOrAddTrait(net.citizensnpcs.trait.HologramTrait.class);
                
                holo.clear();
                
                for (String line : descriptionLines) {
                    holo.addLine(line);
                }
            } catch (NoClassDefFoundError | Exception e) {
                plugin.getLogger().warning("Could not add Hologram description to NPC (Trait not found or error).");
            }
        }

        npc.spawn(location);
        if (npc.getEntity() != null) {
            try {
                npc.getEntity().addScoreboardTag("bw_npc");
            } catch (NoSuchMethodError | Exception ignored) {
            }
            plugin.getNpcManager().registerEntity(npc.getEntity().getUniqueId(), this);
        }

        net.citizensnpcs.api.trait.Trait identifierTrait = new net.citizensnpcs.api.trait.Trait("bw_" + type) {};
        npc.addTrait(identifierTrait);
    }

    @Override
    public void remove() {
        if (npc != null) {
            if (npc.getEntity() != null) {
                plugin.getNpcManager().unregisterEntity(npc.getEntity().getUniqueId());
            }
            npc.destroy();
        }
    }

    @Override
    public java.util.UUID getEntityUUID() {
        if (npc != null && npc.getEntity() != null) {
            return npc.getEntity().getUniqueId();
        }
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Location getLocation() {
        if (npc != null && npc.getEntity() != null) {
            return npc.getEntity().getLocation();
        }
        return null;
    }

    @Override
    public void onClick(Player player) {
        String actionType = type.toLowerCase();

        if (actionType.equalsIgnoreCase("shop")) {
            new ShopGUI().open(player);
            return;
        }
        if (actionType.equalsIgnoreCase("upgrades")) {
            new UpgradeGUI().open(player);
            return;
        }

        if (actionType.equalsIgnoreCase("join") || actionType.equalsIgnoreCase("play")) {
            new JoinGUI().open(player);
            return;
        }

        if (actionType.equalsIgnoreCase("solo")) {
            new me.horiciastko.bedwars.gui.ArenaSelectorGUI(Arena.ArenaMode.SOLO).open(player);
            return;
        }
        if (actionType.equalsIgnoreCase("duo") || actionType.equalsIgnoreCase("doubles")) {
            new me.horiciastko.bedwars.gui.ArenaSelectorGUI(Arena.ArenaMode.DUO).open(player);
            return;
        }
        if (actionType.equalsIgnoreCase("trio") || actionType.equalsIgnoreCase("3v3v3v3")) {
            new me.horiciastko.bedwars.gui.ArenaSelectorGUI(Arena.ArenaMode.TRIO).open(player);
            return;
        }
        if (actionType.equalsIgnoreCase("quad") || actionType.equalsIgnoreCase("squad")
                || actionType.equalsIgnoreCase("4v4v4v4")) {
            new me.horiciastko.bedwars.gui.ArenaSelectorGUI(Arena.ArenaMode.SQUAD).open(player);
            return;
        }

        String modeStr = plugin.getNpcManager().getConfig().getString("types." + type + ".mode");
        if (modeStr != null) {
            try {
                Arena.ArenaMode mode = Arena.ArenaMode.valueOf(modeStr.toUpperCase());
                new me.horiciastko.bedwars.gui.ArenaSelectorGUI(mode).open(player);
                return;
            } catch (IllegalArgumentException e) {
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "npc-invalid-mode").replace("%mode%", modeStr));
            }
        }
    }
}
