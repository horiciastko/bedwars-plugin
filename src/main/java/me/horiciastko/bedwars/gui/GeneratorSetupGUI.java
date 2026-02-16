package me.horiciastko.bedwars.gui;

import me.horiciastko.bedwars.BedWars;
import me.horiciastko.bedwars.models.Arena;
import me.horiciastko.bedwars.models.Team;
import me.horiciastko.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class GeneratorSetupGUI extends BaseGUI {

    private final Arena arena;
    private final Team team;

    public GeneratorSetupGUI(Arena arena) {
        super("§8Global Generators", 3);
        this.arena = arena;
        this.team = null;
    }

    public GeneratorSetupGUI(Arena arena, Team team) {
        super("§8Team Generators: " + team.getName(), 3);
        this.arena = arena;
        this.team = team;
    }

    @Override
    public void setContents(Player player) {
        if (team == null) {
            inventory.setItem(11, new ItemBuilder(Material.DIAMOND)
                    .setName("§aAdd Diamond Generator")
                    .setLore("§7Current Count: " + arena.getDiamondGenerators().size(), "",
                            "§eClick to add at your location")
                    .build());

            inventory.setItem(15, new ItemBuilder(Material.EMERALD)
                    .setName("§aAdd Emerald Generator")
                    .setLore("§7Current Count: " + arena.getEmeraldGenerators().size(), "",
                            "§eClick to add at your location")
                    .build());

            inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("§cClear All Global Generators")
                    .setLore("§7Removes ALL Diamond and Emerald generators")
                    .build());

            inventory.setItem(26, new ItemBuilder(Material.ARROW).setName("§cBack").build());

        } else {
            inventory.setItem(11, new ItemBuilder(Material.IRON_INGOT)
                    .setName("§aAdd Iron Generator")
                    .setLore("§7Click to add at your location", "§7(Usually same block as Gold)")
                    .build());

            inventory.setItem(15, new ItemBuilder(Material.GOLD_INGOT)
                    .setName("§aAdd Gold Generator")
                    .setLore("§7Click to add at your location")
                    .build());

            inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("§cClear Team Generators")
                    .setLore("§7Removes ALL generators for this team")
                    .build());

            inventory.setItem(26, new ItemBuilder(Material.ARROW).setName("§cBack").build());
        }

        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    @Override
    public void handleAction(Player player, int slot, ItemStack item, org.bukkit.event.inventory.ClickType clickType) {
        if (item == null || !item.hasItemMeta())
            return;
        String name = item.getItemMeta().getDisplayName();

        if (name.equals("§cBack")) {
            if (team == null)
                new ArenaSettingsGUI(arena).open(player);
            else
                new TeamSettingsGUI(arena, team).open(player);
            return;
        }

        if (team == null) {
            if (name.contains("Add Diamond Generator")) {
                arena.getDiamondGenerators().add(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-diamond-added-gui"));
                setContents(player);
            } else if (name.contains("Add Emerald Generator")) {
                arena.getEmeraldGenerators().add(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-emerald-added-gui"));
                setContents(player);
            } else if (name.contains("Clear All")) {
                arena.getDiamondGenerators().clear();
                arena.getEmeraldGenerators().clear();
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-global-cleared"));
                setContents(player);
            }
        } else {
            if (name.contains("Add Iron Generator")) {
                team.getGenerators().add(player.getLocation());

                team.getGenerators().add(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-team-added-iron"));
                setContents(player);

            } else if (name.contains("Add Gold Generator")) {
                team.getGenerators().add(player.getLocation());
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-team-added-gold"));
                setContents(player);
            } else if (name.contains("Clear Team")) {
                team.getGenerators().clear();
                player.sendMessage(BedWars.getInstance().getLanguageManager().getMessage(player.getUniqueId(), "generator-team-cleared"));
                setContents(player);
            }
        }
        BedWars.getInstance().getArenaManager().saveArena(arena);
    }
}
