package me.horiciastko.bedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializationUtils {

    public static String locationToString(Location loc) {
        if (loc == null)
            return null;

        String worldName = "unknown";
        try {
            if (loc.getWorld() != null) {
                worldName = loc.getWorld().getName();
            }
        } catch (IllegalArgumentException e) {
        }
        return worldName + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw()
                + "," + loc.getPitch();
    }

    public static Location stringToLocation(String s) {
        if (s == null || s.isEmpty())
            return null;
        String[] part = s.split(",");
        if (part.length < 4)
            return null;
        World world = Bukkit.getWorld(part[0]);
        if (world == null) {

            return null;
        }
        double x = Double.parseDouble(part[1]);
        double y = Double.parseDouble(part[2]);
        double z = Double.parseDouble(part[3]);
        float yaw = part.length > 4 ? Float.parseFloat(part[4]) : 0;
        float pitch = part.length > 5 ? Float.parseFloat(part[5]) : 0;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static double[] parseLocationCoords(String s) {
        if (s == null || s.isEmpty())
            return null;
        String[] part = s.split(",");
        if (part.length < 4)
            return null;
        try {
            double x = Double.parseDouble(part[1]);
            double y = Double.parseDouble(part[2]);
            double z = Double.parseDouble(part[3]);
            float yaw = part.length > 4 ? Float.parseFloat(part[4]) : 0;
            float pitch = part.length > 5 ? Float.parseFloat(part[5]) : 0;
            return new double[] { x, y, z, yaw, pitch };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String extractWorldName(String s) {
        if (s == null || s.isEmpty())
            return null;
        String[] part = s.split(",");
        if (part.length < 1)
            return null;
        return part[0];
    }

    public static Location stringToLocationWithWorldName(String s, String fallbackWorldName) {
        if (s == null || s.isEmpty())
            return null;
        String[] part = s.split(",");
        if (part.length < 4)
            return null;

        String worldName = part[0];
        if (worldName == null || worldName.isEmpty() || worldName.equals("unknown")) {
            worldName = fallbackWorldName;
        }

        World world = worldName != null ? Bukkit.getWorld(worldName) : null;

        try {
            double x = Double.parseDouble(part[1]);
            double y = Double.parseDouble(part[2]);
            double z = Double.parseDouble(part[3]);
            float yaw = part.length > 4 ? Float.parseFloat(part[4]) : 0;
            float pitch = part.length > 5 ? Float.parseFloat(part[5]) : 0;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
