package io.github.tootertutor.ethyrial.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

/**
 * Base class for paged menus that span multiple pages.
 */
public abstract class PagedMenu<T> extends Menu {

    protected int page = 0;
    protected List<T> items = new ArrayList<>();
    protected int itemsPerPage = 10; // default value

    public PagedMenu(Player player, int size, Component title) {
        super(player, size, title);
    }

    /**
     * Set the items to display in the menu.
     * 
     * @param items the list of items
     */
    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * Set the number of items to display per page.
     * 
     * @param itemsPerPage number of items per page
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Called to render one page of items.
     *
     * @param startIndex start index of items for this page
     */
    protected abstract void renderPage(int startIndex);

    @Override
    public void render() {
        int start = page * itemsPerPage;
        renderPage(start);

        if (page > 0) {
            setItem(inventory.getSize() - 7, MenuUtils.createItem(Material.ARROW, Component.text("Previous Page")),
                    e -> {
                        page--;
                        MenuManager.open(player, this);
                    });
        }

        if ((page + 1) * itemsPerPage < items.size()) {
            setItem(inventory.getSize() - 1, MenuUtils.createItem(Material.ARROW, Component.text("Next Page")), e -> {
                page++;
                MenuManager.open(player, this);
            });
        }
    }

    /**
     * Refresh the menu by rendering and opening it again.
     */
    public void refresh() {
        MenuManager.open(player, this);
    }
}
