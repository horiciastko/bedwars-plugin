package me.horiciastko.bedwars.npc;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.logic.DatabaseManager;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.SerializationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NPCManager {

    private final BedWars plugin;
    private final Map<Arena, List<BedWarsNPC>> activeNPCs = new HashMap<>();
    private final Map<UUID, BedWarsNPC> npcLookup = new HashMap<>();
    private final Map<BedWarsNPC, Integer> standaloneNpcIds = new HashMap<>();

    public NPCManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void spawnNPCs(Arena arena) {
        List<BedWarsNPC> npcs = new ArrayList<>();

        for (Team team : arena.getTeams()) {
            if (team.getShopLocation() != null) {
                BedWarsNPC npc = createNPC(arena, team.getShopLocation(), "shop");
                if (npc != null)
                    npcs.add(npc);
            }
            if (team.getUpgradeLocation() != null) {
                BedWarsNPC npc = createNPC(arena, team.getUpgradeLocation(), "upgrades");
                if (npc != null)
                    npcs.add(npc);
            }
        }

        activeNPCs.put(arena, npcs);

        String npcType = plugin.getSupportManager().isCitizensEnabled() ? "Citizens" : "Vanilla";
        plugin.getLogger()
                .info("Spawned " + npcs.size() + " NPCs for arena " + arena.getName() + " using " + npcType + " mode.");
    }


    private BedWarsNPC createNPC(Arena arena, Location location, String type) {
        BedWarsNPC npc;

        if (plugin.getSupportManager().isCitizensEnabled()) {
            npc = new CitizensNPCImpl(plugin, type);
        } else {
            npc = new VanillaNPCImpl(plugin, type);
        }

        if (npc != null) {
            npc.spawn(location);
        }
        return npc;
    }

    public BedWarsNPC createStandaloneNPC(Location location, String type) {
        return createStandaloneNPC(location, type, true);
    }

    private BedWarsNPC createStandaloneNPC(Location location, String type, boolean persistInDatabase) {
        BedWarsNPC npc;

        if (plugin.getSupportManager().isCitizensEnabled()) {
            npc = new CitizensNPCImpl(plugin, type);
        } else {
            npc = new VanillaNPCImpl(plugin, type);
        }

        if (npc != null) {
            npc.spawn(location);
            if (persistInDatabase) {
                int id = plugin.getDatabaseManager().saveStandaloneNPC(type,
                        SerializationUtils.locationToString(location));
                if (id > 0) {
                    standaloneNpcIds.put(npc, id);
                }
            }
        }
        return npc;
    }

    private BedWarsNPC createStandaloneNPCFromDatabase(int id, Location location, String type) {
        BedWarsNPC npc = createStandaloneNPC(location, type, false);
        if (npc != null) {
            standaloneNpcIds.put(npc, id);
        }
        return npc;
    }

    public void removeNPCs(Arena arena) {
        List<BedWarsNPC> npcs = activeNPCs.remove(arena);
        if (npcs != null) {
            npcs.forEach(BedWarsNPC::remove);
        }
    }

    public void registerEntity(UUID uuid, BedWarsNPC npc) {
        npcLookup.put(uuid, npc);
    }

    public void unregisterEntity(UUID uuid) {
        npcLookup.remove(uuid);
    }

    public BedWarsNPC getNPCByEntity(UUID uuid) {
        return npcLookup.get(uuid);
    }

    public BedWarsNPC getNearestNPC(org.bukkit.entity.Player player, double maxDistance) {
        org.bukkit.Location eyeLoc = player.getEyeLocation();
        org.bukkit.util.Vector direction = eyeLoc.getDirection();
        
        BedWarsNPC nearest = null;
        double nearestDist = maxDistance;
        
        for (BedWarsNPC npc : npcLookup.values()) {
            org.bukkit.Location npcLoc = npc.getLocation();
            if (npcLoc == null || !npcLoc.getWorld().equals(eyeLoc.getWorld())) {
                continue;
            }
            
            double distance = eyeLoc.distance(npcLoc);
            if (distance > maxDistance) {
                continue;
            }
            
            org.bukkit.util.Vector toNPC = npcLoc.toVector().subtract(eyeLoc.toVector()).normalize();
            double dot = toNPC.dot(direction);
            
            if (dot > 0.98 && distance < nearestDist) {
                nearest = npc;
                nearestDist = distance;
            }
        }
        
        return nearest;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfigManager().getNpcConfig();
    }


    public List<String> getAvailableTypes() {
        org.bukkit.configuration.ConfigurationSection section = getConfig().getConfigurationSection("types");
        if (section == null)
            return new ArrayList<>();
        return new ArrayList<>(section.getKeys(false));
    }

    public boolean isUsingCitizens() {
        return plugin.getSupportManager().isCitizensEnabled();
    }

    public void removeNPC(BedWarsNPC npc) {
        if (npc == null) {
            return;
        }

        Integer standaloneId = standaloneNpcIds.remove(npc);
        if (standaloneId != null) {
            plugin.getDatabaseManager().deleteStandaloneNPC(standaloneId);
        }

        for (List<BedWarsNPC> npcs : activeNPCs.values()) {
            npcs.remove(npc);
        }

        npc.remove();
    }

    public void loadStandaloneNPCsFromDatabase() {
        List<DatabaseManager.StandaloneNPCRecord> records = plugin.getDatabaseManager().loadStandaloneNPCs();
        int loaded = 0;

        for (DatabaseManager.StandaloneNPCRecord record : records) {
            Location location = SerializationUtils.stringToLocation(record.getLocation());
            if (location == null || location.getWorld() == null) {
                plugin.getLogger().warning("Skipping standalone NPC id=" + record.getId()
                        + " due to invalid location/world.");
                continue;
            }

            BedWarsNPC npc = createStandaloneNPCFromDatabase(record.getId(), location, record.getType());
            if (npc != null) {
                loaded++;
            }
        }

        if (loaded > 0) {
            plugin.getLogger().info("Loaded " + loaded + " standalone NPCs from database.");
        }
    }

    public void removeAllNPCs() {
        Set<BedWarsNPC> allNpcs = new HashSet<>(npcLookup.values());
        for (List<BedWarsNPC> npcs : activeNPCs.values()) {
            allNpcs.addAll(npcs);
        }
        allNpcs.forEach(BedWarsNPC::remove);
        activeNPCs.clear();
        npcLookup.clear();
        standaloneNpcIds.clear();
    }

    public void refreshAllNPCs() {
        List<Arena> arenasToRespawn = new ArrayList<>(activeNPCs.keySet());

        Set<BedWarsNPC> arenaNpcs = new HashSet<>();
        for (List<BedWarsNPC> npcs : activeNPCs.values()) {
            arenaNpcs.addAll(npcs);
        }

        Set<BedWarsNPC> uniqueNpcs = new HashSet<>(npcLookup.values());
        List<Map.Entry<Integer, Map.Entry<Location, String>>> standaloneSnapshots = new ArrayList<>();
        for (Map.Entry<BedWarsNPC, Integer> entry : new HashMap<>(standaloneNpcIds).entrySet()) {
            BedWarsNPC npc = entry.getKey();
            if (npc != null && !arenaNpcs.contains(npc)) {
                Location loc = npc.getLocation();
                if (loc != null) {
                    standaloneSnapshots.add(new AbstractMap.SimpleEntry<>(entry.getValue(),
                            new AbstractMap.SimpleEntry<>(loc.clone(), npc.getType())));
                }
            }
        }

        removeAllNPCs();

        for (Arena arena : arenasToRespawn) {
            spawnNPCs(arena);
        }

        for (Map.Entry<Integer, Map.Entry<Location, String>> snapshot : standaloneSnapshots) {
            BedWarsNPC npc = createStandaloneNPC(snapshot.getValue().getKey(), snapshot.getValue().getValue(), false);
            if (npc != null) {
                standaloneNpcIds.put(npc, snapshot.getKey());
            }
        }
    }

    public List<DatabaseManager.StandaloneNPCRecord> getAllStandaloneNPCsFromDB() {
        return plugin.getDatabaseManager().loadStandaloneNPCs();
    }

    public Map<BedWarsNPC, Integer> getLoadedStandaloneNPCs() {
        return new HashMap<>(standaloneNpcIds);
    }
}
