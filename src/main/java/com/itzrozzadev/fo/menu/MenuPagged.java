package com.itzrozzadev.fo.menu;

import com.itzrozzadev.fo.Common;
import com.itzrozzadev.fo.MathUtil;
import com.itzrozzadev.fo.PlayerUtil;
import com.itzrozzadev.fo.Valid;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.menu.button.Button;
import com.itzrozzadev.fo.menu.model.InventoryDrawer;
import com.itzrozzadev.fo.menu.model.ItemCreator;
import com.itzrozzadev.fo.remain.CompMaterial;
import com.itzrozzadev.fo.settings.SimpleLocalization;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * An advanced menu listing items with automatic page support
 *
 * @param <T> the item that each page consists of
 */
public abstract class MenuPagged<T> extends Menu {

	/**
	 * The pages by the page number, containing a list of items
	 */
	@Getter
	private final Map<Integer, List<T>> pages;

	/**
	 * The current page
	 */
	@Getter
	private int currentPage = 1;

	/**
	 * The next button automatically generated
	 */
	protected Button nextButton;

	/**
	 * The "go to previous page" button automatically generated
	 */
	protected Button prevButton;

	protected int nextPageSlot;
	protected int previousPageSlot;


	/**
	 * Create a new paged menu where each page has 3 rows + 1 bottom bar
	 *
	 * @param pages the pages
	 */
	protected MenuPagged(final Iterable<T> pages) {
		this(null, pages);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param parent the parent menu
	 * @param pages  the pages the pages
	 */
	protected MenuPagged(final Menu parent, final Iterable<T> pages) {
		this(null, parent, pages, false);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param parent
	 * @param pages
	 * @param returnMakesNewInstance
	 */
	protected MenuPagged(final Menu parent, final Iterable<T> pages, final boolean returnMakesNewInstance) {
		this(null, parent, pages, returnMakesNewInstance);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param pageSize size of the menu, a multiple of 9 (keep in mind we already add
	 *                 1 row there)
	 * @param pages    the pages
	 * @deprecated we recommend you don't set the page size for the menu to
	 * autocalculate
	 */
	@Deprecated
	protected MenuPagged(final int pageSize, final Iterable<T> pages) {
		this(pageSize, null, pages);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param pageSize size of the menu, a multiple of 9 (keep in mind we already add
	 *                 1 row there)
	 * @param parent   the parent menu
	 * @param pages    the pages the pages
	 * @deprecated we recommend you don't set the page size for the menu to
	 * autocalculate
	 */
	@Deprecated
	protected MenuPagged(final int pageSize, final Menu parent, final Iterable<T> pages) {
		this(pageSize, parent, pages, false);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param pageSize
	 * @param parent
	 * @param pages
	 * @param returnMakesNewInstance *
	 * @deprecated we recommend you don't set the page size for the menu to
	 * autocalculate
	 */
	@Deprecated
	protected MenuPagged(final int pageSize, final Menu parent, final Iterable<T> pages, final boolean returnMakesNewInstance) {
		this((Integer) pageSize, parent, pages, returnMakesNewInstance);
	}

	/**
	 * Create a new paged menu
	 *
	 * @param pageSize               size of the menu, a multiple of 9 (keep in mind we already add
	 *                               1 row there)
	 * @param parent                 the parent menu
	 * @param pages                  the pages the pages
	 * @param returnMakesNewInstance should we re-instatiate the parent menu when returning to it?
	 */
	private MenuPagged(final Integer pageSize, final Menu parent, final Iterable<T> pages, final boolean returnMakesNewInstance) {
		super(parent, returnMakesNewInstance);

		final int items = getItemAmount(pages);
		final int autoPageSize = pageSize != null ? pageSize : items <= 9 ? 9 * 1 : items <= 9 * 2 ? 9 * 2 : items <= 9 * 3 ? 9 * 3 : items <= 9 * 4 ? 9 * 4 : 9 * 5;

		this.currentPage = 1;
		this.pages = Common.fillPages(autoPageSize, pages);

		setSize(9 + autoPageSize);

		this.nextPageSlot = getSize() - 6;
		this.previousPageSlot = getSize() - 4;
		setButtons();
	}

	public MenuPagged(final Integer pageSize, final int bottomBarSize, final Menu parent, final Iterable<T> pages) {
		super(parent, false);
		this.currentPage = 1;
		this.pages = Common.fillPages(pageSize, pages);
		setSize(bottomBarSize + pageSize);
		setButtons(true);
	}

	private int getItemAmount(final Iterable<T> pages) {
		int amount = 0;

		for (final T t : pages)
			amount++;

		return amount;
	}

	// Render the next/prev buttons
	private void setButtons() {
		final boolean hasPages = this.pages.size() > 1;

		// Set previous button
		this.prevButton = hasPages ? formPreviousButton() : Button.makeEmpty();

		// Set next page button
		this.nextButton = hasPages ? formNextButton() : Button.makeEmpty();
	}

	public void setButtons(final boolean setAnyways) {
		final boolean hasPages = this.pages.size() > 1;
		// Set previous button
		this.prevButton = (setAnyways || hasPages) ? formPreviousButton() : Button.makeEmpty();

		// Set next page button
		this.nextButton = (setAnyways || hasPages) ? formNextButton() : Button.makeEmpty();
	}

	/**
	 * Return the button to list the previous page,
	 * override to customize it.
	 *
	 * @return
	 */
	private Button formPreviousButton() {
		return new Button() {
			final boolean canGo = MenuPagged.this.currentPage > 1;

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				if (this.canGo) {
					MenuPagged.this.currentPage = MathUtil.range(MenuPagged.this.currentPage - 1, 1, MenuPagged.this.pages.size());

					updatePage();
				}
			}

			@Override
			public ItemStack getItem() {
				final int previousPage = MenuPagged.this.currentPage - 1;

				return ItemCreator
						.of(this.canGo ? getHasPagesMaterial() : getNoPagesMaterial())
						.name(previousPage == 0 ? SimpleLocalization.Menu.PAGE_FIRST : SimpleLocalization.Menu.PAGE_PREVIOUS.replace("{page}", String.valueOf(previousPage)))
						.build().make();
			}
		};
	}

	protected CompMaterial getHasPagesMaterial() {
		return CompMaterial.LIME_DYE;
	}

	protected CompMaterial getNoPagesMaterial() {
		return CompMaterial.LIME_DYE;
	}

	/**
	 * Return the button to list the next page,
	 * override to customize it.
	 *
	 * @return
	 */
	private Button formNextButton() {
		return new Button() {
			final boolean canGo = MenuPagged.this.currentPage < MenuPagged.this.pages.size();

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				if (this.canGo) {
					MenuPagged.this.currentPage = MathUtil.range(MenuPagged.this.currentPage + 1, 1, MenuPagged.this.pages.size());
					updatePage();
				}
			}

			@Override
			public ItemStack getItem() {
				final boolean lastPage = MenuPagged.this.currentPage == MenuPagged.this.pages.size();

				return ItemCreator
						.of(this.canGo ? getHasPagesMaterial() : getNoPagesMaterial())
						.name(lastPage ? SimpleLocalization.Menu.PAGE_LAST : SimpleLocalization.Menu.PAGE_NEXT.replace("{page}", String.valueOf(MenuPagged.this.currentPage + 1)))
						.build().make();
			}
		};
	}

	// Reinits the menu and plays the anvil sound
	private void updatePage() {
		setButtons();
		redraw();
		registerButtons();

		Menu.getSound().play(getViewer());
		PlayerUtil.updateInventoryTitle(getViewer(), compileTitle0());
	}

	// Compile title and page numbers
	private String compileTitle0() {
		final boolean canAddNumbers = addPageNumbers() && this.pages.size() > 1;

		return getTitle() + (canAddNumbers ? " &8" + this.currentPage + "/" + this.pages.size() : "");
	}

	/**
	 * Automatically prepend the title with page numbers
	 * <p>
	 * Override for a custom last-minute implementation, but
	 * ensure to call the super method otherwise no title will
	 * be set in {@link InventoryDrawer}
	 *
	 * @param
	 */
	@Override
	protected final void onDisplay(final InventoryDrawer drawer) {
		drawer.setTitle(compileTitle0());
	}

	/**
	 * Return the {@link ItemStack} representation of an item on a certain page
	 * <p>
	 * Use {@link ItemCreator} for easy creation.
	 *
	 * @param item the given object, for example Arena
	 * @return the itemstack, for example diamond sword having arena name
	 */
	protected abstract ItemStack convertToItemStack(T item);

	/**
	 * Called automatically when an item is clicked
	 *
	 * @param player the player who clicked
	 * @param item   the clicked item
	 * @param click  the click type
	 */
	protected abstract void onPageClick(Player player, T item, ClickType click);

	/**
	 * Utility: Shall we send update packet when the menu is clicked?
	 *
	 * @return true by default
	 */
	protected boolean updateButtonOnClick() {
		return true;
	}

	/**
	 * Return true if you want our system to add page/totalPages suffix after
	 * your title, true by default
	 *
	 * @return
	 */
	protected boolean addPageNumbers() {
		return true;
	}

	/**
	 * Return if there are no items at all
	 *
	 * @return
	 */
	protected boolean isEmpty() {
		return this.pages.isEmpty() || this.pages.get(0).isEmpty();
	}

	/**
	 * Automatically get the correct item from the actual page, including
	 * prev/next buttons
	 *
	 * @param slot the slot
	 * @return the item, or null
	 */
	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot < getCurrentPageItems().size()) {
			final T object = getCurrentPageItems().get(slot);

			if (object != null)
				return convertToItemStack(object);
		}

		if (slot == this.nextPageSlot) {
			return this.prevButton.getItem();
		}

		if (slot == this.previousPageSlot)
			return this.nextButton.getItem();

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onMenuClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final ItemStack cursor, final ItemStack clicked, final boolean cancelled) {
		if (slot < getCurrentPageItems().size()) {
			final T obj = getCurrentPageItems().get(slot);

			if (obj != null) {
				final val prevType = player.getOpenInventory().getType();
				onPageClick(player, obj, click);

				if (updateButtonOnClick() && prevType == player.getOpenInventory().getType())
					player.getOpenInventory().getTopInventory().setItem(slot, getItemAt(slot));
			}
		}
	}

	// Do not allow override
	@Override
	public final void onButtonClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final Button button) {
		super.onButtonClick(player, slot, action, click, button);
	}

	// Do not allow override
	@Override
	public final void onMenuClick(final Player player, final int slot, final ItemStack clicked) {
		throw new FoException("Simplest click unsupported");
	}

	// Get all items in a page
	private List<T> getCurrentPageItems() {
		Valid.checkBoolean(this.pages.containsKey(this.currentPage - 1), "The menu has only " + this.pages.size() + " pages, not " + this.currentPage + "!");

		return this.pages.get(this.currentPage - 1);
	}
}