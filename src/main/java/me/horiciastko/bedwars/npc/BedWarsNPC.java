package me.horiciastko.bedwars.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface BedWarsNPC {
    void spawn(Location location);

    void remove();

    String getType();

    void onClick(Player player);

    UUID getEntityUUID();

    Location getLocation();
}
