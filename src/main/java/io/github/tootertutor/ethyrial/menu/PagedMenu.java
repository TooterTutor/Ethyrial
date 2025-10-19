package io.github.tootertutor.ethyrial.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.tootertutor.ethyrial.Ethyrial;
import net.kyori.adventure.text.Component;

/**
 * Base class for paged menus that span multiple pages.
 */
public abstract class PagedMenu<T> extends Menu {

    protected int page = 0;
    protected List<T> items = new ArrayList<>();
    protected int itemsPerPage = 21;
    protected DyeColor themeColor = DyeColor.BLACK;
    protected boolean showBackButton = true;
    protected Menu backTarget;

    public PagedMenu(Player player, int size, Component title) {
        super(player, size, title);
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setThemeColor(DyeColor color) {
        this.themeColor = color;
    }

    public void setBackTarget(Menu backTarget) {
        this.backTarget = backTarget;
        this.showBackButton = true;
    }

    protected abstract void renderPage(int startIndex);

    @Override
    public void render() {
        MenuUtils.applyBorder(getInventory(), themeColor, Ethyrial.getInstance(),
                MenuUtils.BorderStyle.TOP, MenuUtils.BorderStyle.BOTTOM);

        int start = page * itemsPerPage;
        renderPage(start);

        int totalPages = getTotalPages();
        MenuUtils.setPaginationControls(this, page, totalPages, themeColor, backTarget);
    }

    public void refresh() {
        MenuManager.open(player, this);
    }

    public void nextPage() {
        if ((page + 1) * itemsPerPage < items.size()) {
            page++;
            refresh();
        }
    }

    public void previousPage() {
        if (page > 0) {
            page--;
            refresh();
        }
    }

    public int getPage() {
        return page;
    }

    public int getMaxPage() {
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    public void setMaxPages(int pages) {
        if (pages < 1)
            pages = 1;
        int count = Math.max(1, items.size());
        this.itemsPerPage = (int) Math.ceil((double) count / pages);
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / Math.max(1, itemsPerPage)));
    }

    public int getLastPageIndex() {
        return getTotalPages() - 1;
    }

    protected void fillRow(int row, ItemStack item) {
        int startSlot = row * 9;
        for (int i = 0; i < 9; i++) {
            setItem(startSlot + i, item, null); // optional: set click handler null
        }
    }

    protected void clearItems() {
        inventory.clear(); // or loop setItem(i, null, null) if you need click map reset
    }

}
