package me.horiciastko.bedwars.utils;

import org.bukkit.Bukkit;

public enum ServerVersion {
    V1_8(8),
    V1_9(9),
    V1_10(10),
    V1_11(11),
    V1_12(12),
    V1_13(13),
    V1_14(14),
    V1_15(15),
    V1_16(16),
    V1_17(17),
    V1_18(18),
    V1_19(19),
    V1_20(20),
    UNKNOWN(0);

    private final int minor;
    private static ServerVersion current;

    ServerVersion(int minor) {
        this.minor = minor;
    }

    public int getMinor() {
        return minor;
    }

    public static ServerVersion getCurrent() {
        if (current == null) {
            String version = Bukkit.getBukkitVersion().split("-")[0];
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                try {
                    int minor = Integer.parseInt(parts[1]);
                    for (ServerVersion sv : values()) {
                        if (sv.minor == minor) {
                            current = sv;
                            break;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (current == null)
                current = UNKNOWN;
        }
        return current;
    }

    public static boolean isAtLeast(ServerVersion version) {
        return getCurrent().minor >= version.minor && getCurrent() != UNKNOWN;
    }
}
