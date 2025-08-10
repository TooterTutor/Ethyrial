package io.github.tootertutor.ethyrial.menu;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.OpensNativeView; // your interface
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.wesjd.anvilgui.AnvilGUI;

public class RenameMenu extends Menu implements OpensNativeView {

    private final Consumer<String> renameCallback;
    private final ItemStack itemToRename;
    private final String initialName;

    public RenameMenu(Player player, ItemStack itemToRename, String initialName, Consumer<String> renameCallback) {
        // We won’t open this dummy inventory; MenuManager just needs a Menu instance to
        // track.
        super(player, 9, Component.text("Rename"));
        this.itemToRename = itemToRename;
        this.initialName = initialName;
        this.renameCallback = renameCallback;
    }

    @Override
    public void render() {
        /* no-op: AnvilGUI renders */ }

    @Override
    public void open() {
        // Let MenuManager register this as the active menu, then open native view
        MenuManager.open(player, this);
    }

    @Override
    public void openNative(Player player) {
        new AnvilGUI.Builder()
                .plugin(Ethyrial.getInstance())
                .title("Enter a name")
                .text(initialName != null ? initialName : "")
                .itemLeft(itemToRename) // shows the item being “renamed” on the left
                // Called when the player clicks a slot
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    final String typed = state.getText() == null ? "" : state.getText().trim();
                    if (typed.isEmpty()) {
                        state.getPlayer().sendMessage(Component.text("Name cannot be empty.", NamedTextColor.RED));
                        // keep the GUI open; nudge them to type again
                        return Arrays.asList(
                                AnvilGUI.ResponseAction.replaceInputText(initialName != null ? initialName : ""));
                    }

                    try {
                        // Call your consumer right here (on the main thread)
                        renameCallback.accept(typed);
                    } catch (Exception ex) {
                        state.getPlayer().sendMessage(Component.text("Rename failed.", NamedTextColor.RED));
                        ex.printStackTrace();
                    }

                    // Close the anvil and unregister this menu
                    MenuManager.close(player);
                    return Arrays.asList(AnvilGUI.ResponseAction.close());
                })
                // Runs when the inventory is closed (via ESC or our close action)
                .onClose(state -> {
                    // Ensure we’re not left registered as the active menu
                    MenuManager.close(player);
                })
                // Optional: prevent accidental ESC (you can remove this if you prefer freedom)
                // .preventClose()
                .open(player);
    }
}
