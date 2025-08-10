package io.github.tootertutor.ethyrial.menu.menus;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.data.PlayerData;
import io.github.tootertutor.ethyrial.data.WandLoadout;
import io.github.tootertutor.ethyrial.menu.MainMenu;
import io.github.tootertutor.ethyrial.menu.MenuManager;
import io.github.tootertutor.ethyrial.menu.MenuUtils;
import io.github.tootertutor.ethyrial.menu.PagedMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class WandLoadoutMenu extends PagedMenu<WandLoadout> {

    private final Ethyrial plugin = Ethyrial.getInstance();
    private final PlayerData data;
    private final String wandId;

    public WandLoadoutMenu(Player player, PlayerData data, String wandId) {
        super(player, 45, Component.text(wandId + " Loadouts").color(TextColor.color(0x226b57)));
        this.data = data;
        this.wandId = wandId;

        setThemeColor(DyeColor.PURPLE);
        setItemsPerPage(getLoadoutSlots().length); // 24
        setBackTarget(new MainMenu(player, data));

        loadLoadouts();
    }

    private void loadLoadouts() {
        plugin.getPlayerDAO().getWandLoadouts(player.getUniqueId(), wandId)
                .thenAccept(list -> {
                    // Optional: sort by name, push "Default" to top if you like
                    list.sort((a, b) -> {
                        if ("Default".equalsIgnoreCase(a.getName()))
                            return -1;
                        if ("Default".equalsIgnoreCase(b.getName()))
                            return 1;
                        return a.getName().compareToIgnoreCase(b.getName());
                    });

                    setItems(list);
                    Bukkit.getScheduler().runTask(plugin, this::refresh);
                });
    }

    @Override
    protected void renderPage(int startIndex) {
        clearItems();

        MenuUtils.applyBorder(getInventory(), DyeColor.BLACK, plugin, MenuUtils.BorderStyle.FULL);

        // Cyan posts (slots 3 and 5)
        setItem(3, MenuUtils.createGlassPane(DyeColor.CYAN, null, plugin));
        setItem(5, MenuUtils.createGlassPane(DyeColor.CYAN, null, plugin));

        // Info Book at slot 5 -> "Create New Loadout" via AnvilGUI helper
        setItem(4, MenuUtils.createItem(
                Material.WRITABLE_BOOK,
                Component.text("Create New Loadout", NamedTextColor.GREEN),
                player.getUniqueId(),
                List.of(Component.text("Save your current wand bindings", NamedTextColor.GRAY))), click -> {
                    MenuUtils.openRename(
                            player,
                            new ItemStack(Material.WRITABLE_BOOK),
                            "Create New Loadout", // title
                            "New Loadout", // initialText
                            typedName -> { // onConfirm
                                plugin.getPlayerDAO().loadWandBindings(player.getUniqueId(), wandId)
                                        .thenAccept(pair -> {
                                            var left = pair.left();
                                            var right = pair.right();
                                            if (left == null && right == null) {
                                                player.sendMessage(Component.text("Bind at least one spell first.",
                                                        NamedTextColor.RED));
                                                return;
                                            }
                                            plugin.getPlayerDAO().saveWandLoadout(
                                                    player.getUniqueId(), wandId, typedName, left, right)
                                                    .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                                                        player.sendMessage(Component.text("Saved loadout: " + typedName,
                                                                NamedTextColor.GREEN));
                                                        MenuManager.open(player,
                                                                new WandLoadoutMenu(player, data, wandId));
                                                    }));
                                        });
                            },
                            () -> Bukkit.getScheduler().runTask(plugin,
                                    () -> MenuManager.open(player, new WandLoadoutMenu(player, data, wandId))));
                });

        // Loadouts in the centered 3-row grid
        int[] slots = getLoadoutSlots();
        for (int i = 0; i < slots.length; i++) {
            int index = startIndex + i;
            if (index >= items.size())
                break;

            WandLoadout loadout = items.get(index);
            int slot = slots[i];

            setItem(slot, createLoadoutItem(loadout), event -> {
                if (event.isRightClick()) {
                    // Rename existing loadout via AnvilGUI helper
                    MenuUtils.openRename(
                            player,
                            new ItemStack(Material.BOOK),
                            "Rename Loadout", // title
                            loadout.getName(), // initialText
                            newName -> { // onConfirm
                                if (newName.equals(loadout.getName()))
                                    return;

                                plugin.getPlayerDAO().deleteWandLoadout(
                                        player.getUniqueId(), wandId, loadout.getName())
                                        .thenRun(() -> plugin.getPlayerDAO().saveWandLoadout(
                                                player.getUniqueId(), wandId, newName,
                                                loadout.getLeftSpell(), loadout.getRightSpell())
                                                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                                                    player.sendMessage(Component.text("Renamed loadout to " + newName,
                                                            NamedTextColor.GREEN));
                                                    MenuManager.open(player, new WandLoadoutMenu(player, data, wandId));
                                                })));
                            },
                            () -> Bukkit.getScheduler().runTask(plugin,
                                    () -> MenuManager.open(player, new WandLoadoutMenu(player, data, wandId))));

                } else if (event.isShiftClick() && event.isLeftClick()) {
                    // Delete
                    plugin.getPlayerDAO().deleteWandLoadout(player.getUniqueId(), wandId, loadout.getName())
                            .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage(Component.text("Deleted loadout: " + loadout.getName(),
                                        NamedTextColor.RED));
                                MenuManager.open(player, new WandLoadoutMenu(player, data, wandId));
                            }));

                } else {
                    // Apply
                    plugin.getPlayerDAO().saveWandBindings(player.getUniqueId(), wandId, true, loadout.getLeftSpell())
                            .thenRun(() -> plugin.getPlayerDAO().saveWandBindings(
                                    player.getUniqueId(), wandId, false, loadout.getRightSpell())
                                    .thenRun(() -> player.sendMessage(
                                            Component.text("Applied " + loadout.getName() + " to " + wandId + "!", NamedTextColor.GREEN))));
                }
            });
        }
    }

    private int[] getLoadoutSlots() {
        return new int[] {
                10, 11, 12, 13, 14, 15, 16, 17,
                19, 20, 21, 22, 23, 24, 25, 26,
                28, 29, 30, 31, 32, 33, 34, 35
        };
    }

    private ItemStack createLoadoutItem(WandLoadout loadout) {
        return MenuUtils.createItem(
                Material.BOOK,
                Component.text(loadout.getName(), NamedTextColor.BLUE),
                player.getUniqueId(),
                List.of(
                        Component.text(
                                "Left: " + (loadout.getLeftSpell() != null ? loadout.getLeftSpell().getKey() : "None"),
                                NamedTextColor.GRAY),
                        Component.text(
                                "Right: "
                                        + (loadout.getRightSpell() != null ? loadout.getRightSpell().getKey() : "None"),
                                NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Left Click to Apply", NamedTextColor.YELLOW),
                        Component.text("Right Click to Rename", NamedTextColor.YELLOW),
                        Component.text("Shift Left Click to Delete", NamedTextColor.RED)));
    }

}
