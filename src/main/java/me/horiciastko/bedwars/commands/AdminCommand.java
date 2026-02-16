package me.horiciastko.bedwars.commands;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements SubCommand {

    private final BedWars plugin;

    public AdminCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Administrator commands";
    }

    @Override
    public String getSyntax() {
        return "/bw admin arena create <name>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("bedwars.admin")) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-no-permission"));
            return;
        }

        if (args.length < 2) {
            sendAdminHelp(player);
            return;
        }

        if (args[1].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadAll();
            plugin.getScoreboardManager().reloadConfig();
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-reload-success"));
            return;
        }

        if (args[1].equalsIgnoreCase("setlobby")) {
            org.bukkit.Location loc = player.getLocation();
            plugin.getGameManager().setMainLobbyLocation(loc);
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-setlobby-success")
                .replace("%world%", loc.getWorld().getName())
                .replace("%x%", String.format("%.1f", loc.getX()))
                .replace("%y%", String.format("%.1f", loc.getY()))
                .replace("%z%", String.format("%.1f", loc.getZ())));
            return;
        }

        if (args[1].equalsIgnoreCase("lang")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-lang-usage"));
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-lang-available")
                        .replace("%languages%", String.join(", ", plugin.getLanguageManager().getAvailableLanguages())));
                return;
            }
            String targetLang = args[2];
            if (!plugin.getLanguageManager().isLanguageAvailable(targetLang)) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-lang-not-loaded")
                        .replace("%lang%", targetLang));
                return;
            }
            plugin.getConfig().set("default-language", targetLang);
            plugin.saveConfig();

            plugin.getConfigManager().reloadAll();
            plugin.getLanguageManager().setPlayerLanguage(player.getUniqueId(), targetLang);
            plugin.getNpcManager().refreshAllNPCs();

            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-lang-set")
                .replace("%lang%", targetLang));
            return;
        }

        if (args[1].equalsIgnoreCase("build")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-build-usage"));
                return;
            }
            boolean enable = args[2].equalsIgnoreCase("on");
            plugin.getGameManager().setBuildMode(player, enable);
            String status = enable ? plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-build-enabled")
                                  : plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-build-disabled");
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-build-toggled")
                .replace("%status%", status));
            return;
        }

        if (args[1].equalsIgnoreCase("npc")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-usage"));
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-types-list")
                        .replace("%types%", String.join(", ", plugin.getNpcManager().getAvailableTypes())));
                return;
            }

            if (args[2].equalsIgnoreCase("remove")) {
                me.horiciastko.bedwars.npc.BedWarsNPC nearest = null;

                org.bukkit.entity.Entity target = player.getTargetEntity(5);
                if (target != null) {
                    nearest = plugin.getNpcManager().getNPCByEntity(target.getUniqueId());
                }

                if (nearest == null) {
                    org.bukkit.util.Vector direction = player.getLocation().getDirection();
                    org.bukkit.Location eyeLoc = player.getEyeLocation();
                    double maxDistance = 5.0;
                    org.bukkit.entity.Entity nearestEntity = null;
                    
                    for (org.bukkit.entity.Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
                        me.horiciastko.bedwars.npc.BedWarsNPC npc = plugin.getNpcManager().getNPCByEntity(entity.getUniqueId());
                        if (npc != null) {
                            org.bukkit.util.Vector toEntity = entity.getLocation().toVector().subtract(eyeLoc.toVector());
                            if (toEntity.normalize().dot(direction) > 0.98) {
                                if (nearestEntity == null || eyeLoc.distance(entity.getLocation()) < eyeLoc.distance(nearestEntity.getLocation())) {
                                    nearestEntity = entity;
                                    nearest = npc;
                                }
                            }
                        }
                    }
                }

                if (nearest == null) {
                    nearest = plugin.getNpcManager().getNearestNPC(player, 5.0);
                }

                if (nearest != null) {
                    plugin.getNpcManager().removeNPC(nearest);
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-removed"));
                } else {
                    java.util.List<me.horiciastko.bedwars.logic.DatabaseManager.StandaloneNPCRecord> allNPCs = 
                        plugin.getNpcManager().getAllStandaloneNPCsFromDB();
                    
                    if (allNPCs.isEmpty()) {
                        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-none-in-db"));
                    } else {
                        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-list-header"));
                        for (me.horiciastko.bedwars.logic.DatabaseManager.StandaloneNPCRecord record : allNPCs) {
                            org.bukkit.Location loc = me.horiciastko.bedwars.utils.SerializationUtils.stringToLocation(record.getLocation());
                            String locationStr = loc != null 
                                ? String.format("%s: %.1f, %.1f, %.1f", 
                                    loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ())
                                : record.getLocation();
                            
                            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-list-entry")
                                .replace("%id%", String.valueOf(record.getId()))
                                .replace("%type%", record.getType())
                                .replace("%location%", locationStr));
                        }
                        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-list-footer"));
                    }
                }
                return;
            }

            String type = args[2].toLowerCase();
            List<String> validTypes = plugin.getNpcManager().getAvailableTypes();
            if (!validTypes.contains(type)) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-npc-invalid-type")
                    .replace("%types%", String.join(", ", validTypes)));
                return;
            }
            plugin.getSupportManager().createCitizensNPC(player, type);
            return;
        }

        if (args[1].equalsIgnoreCase("arena")) {
            if (args.length == 2) {
                new me.horiciastko.bedwars.gui.ArenaListGUI().open(player);
                return;
            }
        }

        if (args.length < 3) {
            sendAdminHelp(player);
            return;
        }

        if (args[1].equalsIgnoreCase("arena")) {
            if (args[2].equalsIgnoreCase("create")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-create-usage"));
                    return;
                }
                String name = args[3];
                if (plugin.getArenaManager().getArena(name) != null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-exists"));
                    return;
                }
                plugin.getArenaManager().addArena(new Arena(name));
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-created")
                    .replace("%name%", name));
            } else if (args[2].equalsIgnoreCase("edit")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-edit-usage"));
                    return;
                }
                String name = args[3];
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", name));
                    return;
                }
                plugin.getArenaManager().setEditArena(player, arena);
                new me.horiciastko.bedwars.gui.ArenaSettingsGUI(arena).open(player);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-edit-opened")
                    .replace("%name%", name));
            } else if (args[2].equalsIgnoreCase("enable")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-enable-usage"));
                    return;
                }
                String name = args[3];
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", name));
                    return;
                }
                arena.setEnabled(true);
                plugin.getArenaManager().saveArena(arena);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-enabled")
                    .replace("%name%", name));
            } else if (args[2].equalsIgnoreCase("disable")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-disable-usage"));
                    return;
                }
                String name = args[3];
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", name));
                    return;
                }
                arena.setEnabled(false);
                plugin.getArenaManager().saveArena(arena);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-disabled")
                    .replace("%name%", name));
            } else if (args[2].equalsIgnoreCase("delete")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-delete-usage"));
                    return;
                }
                String name = args[3];
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", name));
                    return;
                }
                plugin.getArenaManager().deleteArena(arena);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-deleted")
                    .replace("%name%", name));
            } else if (args[2].equalsIgnoreCase("group")) {
                if (args.length < 5) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-group-usage"));
                    return;
                }
                String name = args[3];
                String group = args[4];
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", name));
                    return;
                }

                if (!plugin.getConfig().contains("groups." + group)) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-group-warning")
                        .replace("%group%", group));
                }

                arena.setGroup(group);
                plugin.getArenaManager().saveArena(arena);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-group-set")
                    .replace("%name%", name)
                    .replace("%group%", group));
            } else {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-usage"));
            }
        } else if (args[1].equalsIgnoreCase("start")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-start-usage"));
                return;
            }
            String arenaName = args[2];
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                    .replace("%name%", arenaName));
                return;
            }
            if (arena.getPlayers().isEmpty()) {
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-start-no-players")
                    .replace("%name%", arenaName));
                return;
            }
            plugin.getGameManager().forceStart(arena);
            player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-start-forced")
                .replace("%name%", arena.getName()));
        } else if (args[1].equalsIgnoreCase("sign")) {
            if (args[2].equalsIgnoreCase("join")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-sign-join-usage"));
                    return;
                }
                String arenaName = args[3];
                Arena arena = plugin.getArenaManager().getArena(arenaName);
                if (arena == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-arena-not-found")
                        .replace("%name%", arenaName));
                    return;
                }

                org.bukkit.block.Block block = player.getTargetBlockExact(5);
                if (block == null || !block.getType().name().contains("SIGN")) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-sign-not-looking"));
                    return;
                }

                arena.getJoinSigns().add(block.getLocation());
                plugin.getArenaManager().saveArena(arena);
                plugin.getSignManager().updateSigns(arena);
                player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-sign-created")
                    .replace("%name%", arena.getName()));
            } else {
                sendAdminHelp(player);
            }
        } else {
            sendAdminHelp(player);
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (!player.hasPermission("bedwars.admin"))
            return new ArrayList<>();

        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("arena");
            options.add("sign");
            options.add("reload");
            options.add("start");
            options.add("npc");
            options.add("build");
            options.add("lang");
            options.add("setlobby");
            return filter(options, args[1]);
        }

        if (args.length == 3) {
            List<String> options = new ArrayList<>();
            if (args[1].equalsIgnoreCase("arena")) {
                options.add("create");
                options.add("edit");
                options.add("enable");
                options.add("disable");
                options.add("delete");
                options.add("group");
            } else if (args[1].equalsIgnoreCase("sign")) {
                options.add("join");
            } else if (args[1].equalsIgnoreCase("build")) {
                options.add("on");
                options.add("off");
            } else if (args[1].equalsIgnoreCase("npc")) {
                options.add("remove");
                options.addAll(plugin.getNpcManager().getAvailableTypes());
            } else if (args[1].equalsIgnoreCase("lang")) {
                options.addAll(plugin.getLanguageManager().getAvailableLanguages());
            }
            return filter(options, args[2]);
        }

        if (args.length == 4) {
            List<String> arenaNames = plugin.getArenaManager().getArenas().stream().map(Arena::getName)
                    .collect(Collectors.toList());
            return filter(new ArrayList<>(arenaNames), args[3]);
        }

        if (args.length == 5) {
            if (args[1].equalsIgnoreCase("arena") && args[2].equalsIgnoreCase("group")) {
                org.bukkit.configuration.ConfigurationSection groups = plugin.getConfig()
                        .getConfigurationSection("groups");
                if (groups != null) {
                    return filter(new ArrayList<>(groups.getKeys(false)), args[4]);
                }
                return filter(java.util.Collections.singletonList("Default"), args[4]);
            }
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(" ");
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-header"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-arena-create"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-arena-edit"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-arena-toggle"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-npc-create"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-npc-remove"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-build"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-sign"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-separator"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-reload"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-start"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-lang"));
        player.sendMessage(plugin.getLanguageManager().getMessage(player.getUniqueId(), "admin-help-setlobby"));
        player.sendMessage(" ");
    }
}
