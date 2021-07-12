package com.itzrozzadev.fo.menu;

import com.itzrozzadev.fo.*;
import com.itzrozzadev.fo.constants.FoConstants;
import com.itzrozzadev.fo.event.MenuOpenEvent;
import com.itzrozzadev.fo.exception.FoException;
import com.itzrozzadev.fo.menu.button.Button;
import com.itzrozzadev.fo.menu.button.Button.DummyButton;
import com.itzrozzadev.fo.menu.button.ButtonReturnBack;
import com.itzrozzadev.fo.menu.model.InventoryDrawer;
import com.itzrozzadev.fo.menu.model.ItemCreator;
import com.itzrozzadev.fo.menu.model.MenuClickLocation;
import com.itzrozzadev.fo.model.SimpleSound;
import com.itzrozzadev.fo.plugin.SimplePlugin;
import com.itzrozzadev.fo.remain.CompMaterial;
import com.itzrozzadev.fo.remain.CompMetadata;
import com.itzrozzadev.fo.remain.CompSound;
import com.itzrozzadev.fo.settings.SimpleLocalization;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * The core class of Menu. Represents a simple menu.
 */
public abstract class Menu {

	// --------------------------------------------------------------------------------
	// Static
	// --------------------------------------------------------------------------------

	/**
	 * The default sound when switching between menus.
	 */
	@Getter
	@Setter
	private static SimpleSound sound = new SimpleSound(CompSound.NOTE_STICKS.getSound(), .4F);

	/**
	 * Should we animate menu titles?
	 */
	@Getter
	@Setter
	private static boolean titleAnimationEnabled = true;

	/**
	 * The default duration of the new animated title before
	 * it is reverted back to the old one
	 * <p>
	 * Used in {#updateInventoryTitle(Menu, Player, String, String)}
	 */
	@Setter
	private static int titleAnimationDurationTicks = 20;

	/**
	 * A placeholder to represent that no item should be displayed/returned
	 */
	protected static final ItemStack NO_ITEM = null;

	// --------------------------------------------------------------------------------
	// Actual class
	// --------------------------------------------------------------------------------

	/**
	 * Automatically registered Buttons in this menu (using reflection)
	 */
	private final List<Button> registeredButtons = new ArrayList<>();

	/**
	 * The register responsible for scanning the class and making buttons
	 * function
	 */
	private boolean buttonsRegistered = false;

	/**
	 * Parent menu
	 */
	private final Menu parent;

	/**
	 * The return button to the previous menu, null if none
	 */
	private final Button returnButton;

	// --------------------------------------------------------------------------------
	// Other constructors
	// --------------------------------------------------------------------------------

	/**
	 * The inventory title of the menu, colors & are supported
	 */
	private String title = "&0Menu";

	/**
	 * The size of the menu
	 */
	private Integer size = 9 * 3;

	/**
	 * The description of the menu
	 */
	@Getter(value = AccessLevel.PROTECTED)
	private String[] info = null;

	/**
	 * The viewer of this menu, is null until {@link #displayTo(Player)} is called
	 */
	private Player viewer;

	/**
	 * Debug option to render empty spaces as glass panel having the slot id visible
	 */
	private boolean slotNumbersVisible;

	/**
	 * Create a new menu without parent menu with the size of 9*3
	 *
	 * <p>
	 * You are encouraged to change the size and title of this menu in your
	 * constructor by calling {@link #setTitle(String)} and
	 * {@link #setSize(Integer)}
	 *
	 * <p>
	 * NB: The {@link #getViewer()} of this menu is yet null!
	 */
	protected Menu() {
		this(null);
	}

	/**
	 * Create a new menu with parent menu with the size of 9*3
	 *
	 * <p>
	 * You are encouraged to change the size and title of this menu in your
	 * constructor by calling {@link #setTitle(String)} and
	 * {@link #setSize(Integer)}
	 *
	 * <p>
	 * NB: The {@link #getViewer()} of this menu is yet null!
	 *
	 * @param parent the parent menu
	 */
	protected Menu(final Menu parent) {
		this(parent, true);
	}

	/**
	 * Create a new menu with parent menu with the size of 9*3
	 *
	 * <p>
	 * You are encouraged to change the size and title of this menu in your
	 * constructor by calling {@link #setTitle(String)} and
	 * {@link #setSize(Integer)}
	 *
	 * <p>
	 * NB: The {@link #getViewer()} of this menu is yet null!
	 *
	 * @param parent                 the parent
	 * @param returnMakesNewInstance should we re-instatiate the parent menu when
	 *                               returning to it?
	 */
	protected Menu(final Menu parent, final boolean returnMakesNewInstance) {
		this.parent = parent;
		this.returnButton = parent != null ? new ButtonReturnBack(this) : Button.makeEmpty();
	}

	public void onBackClick(final Player player, final Menu menu) {
		menu.getParent().displayTo(player);
	}

	/**
	 * Returns the current menu for player
	 *
	 * @param player the player
	 * @return the menu, or null if none
	 */
	public static Menu getMenu(final Player player) {
		return getMenu0(player, FoConstants.NBT.TAG_MENU_CURRENT);
	}

	/**
	 * Returns the previous menu for player
	 *
	 * @param player the player
	 * @return the menu, or none
	 */
	public static Menu getPreviousMenu(final Player player) {
		return getMenu0(player, FoConstants.NBT.TAG_MENU_PREVIOUS);
	}

	// Returns the menu associated with the players metadata, or null
	private static Menu getMenu0(final Player player, final String tag) {
		if (player.hasMetadata(tag)) {
			final Menu menu = (Menu) player.getMetadata(tag).get(0).value();
			Valid.checkNotNull(menu, "Menu missing from " + player.getName() + "'s metadata '" + tag + "' tag!");

			return menu;
		}

		return null;
	}

	// --------------------------------------------------------------------------------
	// Reflection to make life easier
	// --------------------------------------------------------------------------------

	/**
	 * Scans the menu class this menu extends and registers buttons
	 */
	protected final void registerButtons() {
		this.registeredButtons.clear();

		// Register buttons explicitly given
		{
			final List<Button> buttons = getButtonsToAutoRegister();

			if (buttons != null)
				this.registeredButtons.addAll(buttons);
		}

		// Register buttons declared as fields
		{
			Class<?> lookup = getClass();

			do
				for (final Field f : lookup.getDeclaredFields())
					registerButton0(f);
			while (Menu.class.isAssignableFrom(lookup = lookup.getSuperclass()));
		}
	}

	// Scans the class and register fields that extend Button class
	private void registerButton0(final Field field) {
		field.setAccessible(true);

		final Class<?> type = field.getType();

		if (Button.class.isAssignableFrom(type)) {
			final Button button = (Button) ReflectionUtil.getFieldContent(field, this);

			Valid.checkNotNull(button, "Null button field named " + field.getName() + " in " + this);
			this.registeredButtons.add(button);

		} else if (Button[].class.isAssignableFrom(type)) {
			Valid.checkBoolean(Modifier.isFinal(field.getModifiers()), "Report / Button[] field must be final: " + field);
			final Button[] buttons = (Button[]) ReflectionUtil.getFieldContent(field, this);

			Valid.checkBoolean(buttons != null && buttons.length > 0, "Null " + field.getName() + "[] in " + this);
			this.registeredButtons.addAll(Arrays.asList(buttons));
		}
	}

	/*
	 * Utility method to register buttons if they yet have not been registered
	 *
	 * This method will only register them once until the server is reset
	 */
	private void registerButtonsIfHasnt() {
		if (!this.buttonsRegistered) {
			registerButtons();
			this.buttonsRegistered = true;
		}
	}

	/**
	 * Returns a list of buttons that should be registered manually.
	 * <p>
	 * NOTICE: Button fields in your class are registered automatically, do not add
	 * them here
	 *
	 * @return button list, null by default
	 */
	protected List<Button> getButtonsToAutoRegister() {
		return null;
	}

	/**
	 * Attempts to find a clickable registered button in this menu having the same
	 * icon as the given item stack
	 *
	 * @param fromItem the itemstack to compare to
	 * @return the buttor or null if not found
	 */
	protected final Button getButton(final ItemStack fromItem) {
		registerButtonsIfHasnt();

		if (fromItem != null)
			for (final Button button : this.registeredButtons) {
				Valid.checkNotNull(button, "Menu button is null at " + getClass().getSimpleName());
				Valid.checkNotNull(button.getItem(), "Menu " + getTitle() + " contained button " + button + " with empty item!");

				if (ItemUtil.isSimilar(fromItem, button.getItem()))
					return button;
			}

		return null;
	}

	/**
	 * Return a new instance of this menu
	 *
	 * <p>
	 * You must override this in certain cases
	 *
	 * @return the new instance, of null
	 * @throws if new instance could not be made, for example when the menu is
	 *            taking constructor params
	 */
	public Menu newInstance() {
		try {
			return getParent();
		} catch (final Throwable th) {
			try {
				return ReflectionUtil.instantiate(getClass());
			} catch (final Throwable t) {
				try {
					final Object parent = getClass().getMethod("getParent").invoke(getClass());

					if (parent != null)
						return ReflectionUtil.instantiate(getClass(), parent);
				} catch (final Throwable ignored) {
				}

				t.printStackTrace();
			}

			throw new FoException("Could not instantiate menu of " + getClass() + ", override the method 'newInstance()' or ensure you have a public constructor which takes only one parameter ");
		}
	}

	// --------------------------------------------------------------------------------
	// Rendering the menu
	// --------------------------------------------------------------------------------

	/**
	 * Display this menu to the player
	 *
	 * @param player the player
	 */
	public final void displayTo(final Player player) {
		displayTo(player, false);
	}

	public final void displayTo(final Player player, final boolean ignoreConversing) {
		Valid.checkNotNull(this.size, "Size not set in " + this + " (call setSize in your constructor)");
		Valid.checkNotNull(this.title, "Title not set in " + this + " (call setTitle in your constructor)");

		this.viewer = player;
		registerButtonsIfHasnt();

		// Draw the menu
		final InventoryDrawer drawer = InventoryDrawer.of(this.size, this.title);

		// Compile bottom bar
		compileBottomBar0().forEach(drawer::setItem);

		// Set items defined by classes upstream
		for (int i = 0; i < drawer.getSize(); i++) {
			final ItemStack item = getItemAt(i);

			if (item != null && item.getType() != CompMaterial.AIR.getMaterial() && !drawer.isSet(i))
				try {
					drawer.setItem(i, makeMenuItem(item));
				} catch (final Throwable t) {
					drawer.setItem(i, item);
				}
		}

		// Allow last minute modifications
		onDisplay(drawer);

		// Render empty slots as slot numbers if enabled
		debugSlotNumbers(drawer);

		// Call event after items have been set to allow to get them
		if (!Common.callEvent(new MenuOpenEvent(this, drawer, player)))
			return;

		// Prevent menu in conversation
		if (player.isConversing()) {
			if (!ignoreConversing) {
				player.sendRawMessage(Common.colorize(SimpleLocalization.Menu.CANNOT_OPEN_DURING_CONVERSATION));
				return;
			}
		}

		// Play the pop sound
		sound.play(player);

		// Register previous menu if exists
		{
			final Menu previous = getMenu(player);
			if (previous != null)
				player.setMetadata(FoConstants.NBT.TAG_MENU_PREVIOUS, new FixedMetadataValue(SimplePlugin.getInstance(), previous));
		}

		// Register current menu
		Common.runLater(1, () -> {
			drawer.display(player);
			player.setMetadata(FoConstants.NBT.TAG_MENU_CURRENT, new FixedMetadataValue(SimplePlugin.getInstance(), Menu.this));
		});
	}

	private ItemStack makeMenuItem(final ItemStack itemStack) {
		return CompMetadata.makeMenuItem(itemStack);
	}

	/**
	 * Sets all empty slots to light gray pane or adds a slot number to existing
	 * items lores if {@link #slotNumbersVisible} is true
	 *
	 * @param drawer
	 */
	private void debugSlotNumbers(final InventoryDrawer drawer) {
		if (this.slotNumbersVisible)
			for (int slot = 0; slot < drawer.getSize(); slot++) {
				final ItemStack item = drawer.getItem(slot);
				if (item == null)
					drawer.setItem(slot, ItemCreator.of(CompMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, "Slot #" + slot).build().make());
			}
	}

	/**
	 * Called automatically before the menu is displayed but after all items have
	 * been drawed
	 *
	 * <p>
	 * Override for custom last-minute modifications
	 *
	 * @param drawer the drawer
	 */
	protected void onDisplay(final InventoryDrawer drawer) {
	}

	/**
	 * Redraws and refreshes all buttons
	 */
	public final void restartMenu() {
		restartMenu(null);
	}

	/**
	 * Redraws and re-register all buttons while sending a title animation to the
	 * player
	 *
	 * @param animatedTitle the animated title
	 */
	public final void restartMenu(final String animatedTitle) {
		registerButtons();
		redraw();

		if (animatedTitle != null)
			animateTitle(animatedTitle);
	}

	/**
	 * Redraws the bottom bar and updates inventory
	 */
	protected final void redraw() {
		final Inventory inv = getViewer().getOpenInventory().getTopInventory();
		Valid.checkBoolean(inv.getType() == InventoryType.CHEST, getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").");

		for (int i = 0; i < this.size; i++) {
			final ItemStack item = getItemAt(i);

			Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position (" + i + ") > inv size (" + inv.getSize() + ")");
			inv.setItem(i, item);
		}

		compileBottomBar0().forEach(inv::setItem);
		getViewer().updateInventory();
	}

	protected final void redraw(final Inventory inv) {
		Valid.checkBoolean(inv.getType() == InventoryType.CHEST, getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").");
		for (int i = 0; i < this.size; i++) {
			final ItemStack item = inv.getItem(i);

			Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position (" + i + ") > inv size (" + inv.getSize() + ")");
			inv.setItem(i, item);
		}

		compileBottomBar0().forEach(inv::setItem);
		getViewer().updateInventory();
	}

	/**
	 * Draws the bottom bar for the player inventory
	 *
	 * @return
	 */
	private Map<Integer, ItemStack> compileBottomBar0() {
		final Map<Integer, ItemStack> items = new HashMap<>();

		if (addInfoButton() && getInfo() != null)
			items.put(getInfoButtonPosition(), Button.makeInfo(getInfo()).getItem());

		if (addReturnButton() && !(this.returnButton instanceof DummyButton))
			items.put(getReturnButtonPosition(), this.returnButton.getItem());

		return items;
	}

	/**
	 * Animate the title of this menu
	 *
	 * <p>
	 * Automatically reverts back to the old title after 1 second
	 *
	 * @param title the title to animate
	 */
	public final void animateTitle(final String title) {
		if (titleAnimationEnabled)
			PlayerUtil.updateInventoryTitle(this, getViewer(), title, getTitle(), titleAnimationDurationTicks);
	}

	public final void updateItem(final int slot, final CompMaterial material) {
		final Inventory inv = getViewer().getOpenInventory().getTopInventory();
		Valid.checkBoolean(inv.getType() == InventoryType.CHEST, getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").");

		for (int i = 0; i < this.size; i++) {
			final ItemStack item = inv.getItem(i);
			if (slot == i) {
				if (item != null)
					item.setType(material.getMaterial());
			}
			Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position (" + i + ") > inv size (" + inv.getSize() + ")");
			inv.setItem(i, item);
		}
		redraw(inv);
	}

	public final void updateLore(final int slot, final String... lore) {
		updateLore(slot, Arrays.asList(lore));
	}

	public final void updateLore(final int slot, final List<String> lore) {
		final Inventory inv = getViewer().getOpenInventory().getTopInventory();
		Valid.checkBoolean(inv.getType() == InventoryType.CHEST, getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").");

		for (int i = 0; i < this.size; i++) {
			final ItemStack item = inv.getItem(i);
			if (slot == i) {
				if (item != null) {
					final ItemMeta meta = item.getItemMeta();
					assert meta != null;
					final List<String> updatedLore = new ArrayList<>();
					for (String s : lore) {
						if (!s.startsWith("&"))
							s = "&7" + s;
						updatedLore.add(s);
					}
					meta.setLore(Common.colorize(updatedLore));
					item.setItemMeta(meta);
				}
			}
			Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position (" + i + ") > inv size (" + inv.getSize() + ")");
			inv.setItem(i, item);
		}

		redraw(inv);
	}

	// --------------------------------------------------------------------------------
	// Convenience messenger functions
	// --------------------------------------------------------------------------------

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param messages
	 */
	public void tell(final String... messages) {
		Common.tell(this.viewer, messages);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellInfo(final String message) {
		Messenger.info(this.viewer, message);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellSuccess(final String message) {
		Messenger.success(this.viewer, message);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellWarn(final String message) {
		Messenger.warn(this.viewer, message);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellError(final String message) {
		Messenger.error(this.viewer, message);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellQuestion(final String message) {
		Messenger.question(this.viewer, message);
	}

	/**
	 * Send a message to the {@link #getViewer()}
	 *
	 * @param message
	 */
	public void tellAnnounce(final String message) {
		Messenger.announce(this.viewer, message);
	}

	// --------------------------------------------------------------------------------
	// Menu functions
	// --------------------------------------------------------------------------------

	/**
	 * Returns the item at a certain slot
	 *
	 * @param slot the slow
	 * @return the item, or null if no icon at the given slot (default)
	 */
	public ItemStack getItemAt(final int slot) {
		return null;
	}

	/**
	 * Get the info button position
	 *
	 * @return the slot which info buttons is located on
	 */
	protected int getInfoButtonPosition() {
		return this.size - 9;
	}

	/**
	 * Should we automatically add the return button to the bottom left corner?
	 *
	 * @return true if the return button should be added, true by default
	 */
	protected boolean addReturnButton() {
		return true;
	}

	/**
	 * Should we automatically add an info button {@link #getInfo()} at the
	 * {@link #getInfoButtonPosition()} ?
	 *
	 * @return
	 */
	protected boolean addInfoButton() {
		return true;
	}

	/**
	 * Get the return button position
	 *
	 * @return the slot which return buttons is located on
	 */
	protected int getReturnButtonPosition() {
		return this.size - 1;
	}

	/**
	 * Calculates the center slot of this menu
	 *
	 * <p>
	 * Credits to Gober at
	 * https://www.spigotmc.org/threads/get-the-center-slot-of-a-menu.379586/
	 *
	 * @return the estimated center slot
	 */
	protected final int getCenterSlot() {
		final int pos = this.size / 2;

		return this.size % 2 == 1 ? pos : pos - 5;
	}

	/**
	 * Should we prevent the click or drag?
	 *
	 * @param location the click location
	 * @param slot     the slot
	 * @param clicked  the clicked item
	 * @param cursor   the cursor
	 * @return if the action is cancelled in the {@link InventoryClickEvent}, false
	 * by default
	 * @deprecated sometimes does not work correctly due to flaws in server to
	 * client packet communication - do not rely on this
	 */
	@Deprecated
	protected boolean isActionAllowed(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor) {
		return false;
	}

	/**
	 * The title of this menu
	 *
	 * @return the menu title
	 */
	public final String getTitle() {
		return this.title;
	}

	/**
	 * Sets the title of this inventory, this change is not reflected in client, you
	 * must call {@link #restartMenu()} to take change
	 *
	 * @param title the new title
	 */
	protected final void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Return the parent menu or null
	 *
	 * @return
	 */
	public final Menu getParent() {
		return this.parent;
	}

	/**
	 * Get the size of this menu
	 *
	 * @return
	 */
	public final Integer getSize() {
		return this.size;
	}

	/**
	 * Sets the size of this menu (without updating the player container - if you
	 * want to update it call {@link #restartMenu()})
	 *
	 * @param size
	 */
	protected final void setSize(final Integer size) {
		this.size = size;
	}

	/**
	 * Set the menu's description
	 *
	 * <p>
	 * Used to create an info bottom in bottom left corner, see
	 * {@link Button#makeInfo(String...)}
	 *
	 * @param info the info to set
	 */
	protected final void setInfo(final String... info) {
		this.info = info;
	}

	/**
	 * Get the viewer that this instance of this menu is associated with
	 *
	 * @return the viewer of this instance, or null
	 */
	protected final Player getViewer() {
		return this.viewer;
	}

	/**
	 * Sets the viewer for this instance of this menu
	 *
	 * @param viewer
	 */
	protected final void setViewer(final Player viewer) {
		this.viewer = viewer;
	}

	/**
	 * Return the top opened inventory if viewer exists
	 *
	 * @return
	 */
	protected final Inventory getInventory() {
		Valid.checkNotNull(this.viewer, "Cannot get inventory when there is no viewer!");

		final Inventory topInventory = this.viewer.getOpenInventory().getTopInventory();
		Valid.checkNotNull(topInventory, "Top inventory is null!");

		return topInventory;
	}

	/**
	 * Get the open inventory content to match the array length, cloning items
	 * preventing ID mismatch in yaml files
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	protected final ItemStack[] getContent(final int from, final int to) {
		final ItemStack[] content = getInventory().getContents();
		final ItemStack[] copy = new ItemStack[content.length];

		for (int i = from; i < copy.length; i++) {
			final ItemStack item = content[i];

			copy[i] = item != null ? item.clone() : null;
		}

		return Arrays.copyOfRange(copy, from, to);
	}

	/**
	 * If you wonder what slot numbers does each empty slot in your menu has then
	 * set this to true in your constructor
	 *
	 * <p>
	 * Only takes change when used in constructor or before calling
	 * {@link #displayTo(Player)} and cannot be updated in {@link #restartMenu()}
	 *
	 * @param visible
	 */
	protected final void setSlotNumbersVisible() {
		this.slotNumbersVisible = true;
	}

	// --------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------

	/**
	 * Called automatically when the menu is clicked.
	 *
	 * <p>
	 * By default we call the shorter {@link #onMenuClick(Player, int, ItemStack)}
	 * method.
	 *
	 * @param player    the player
	 * @param slot      the slot
	 * @param action    the action
	 * @param click     the click
	 * @param cursor    the cursor
	 * @param clicked   the item clicked
	 * @param cancelled is the event cancelled?
	 */
	protected void onMenuClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final ItemStack cursor, final ItemStack clicked, final boolean cancelled) {
		this.onMenuClick(player, slot, clicked);
	}

	/**
	 * Called automatically when the menu is clicked
	 *
	 * @param player  the player
	 * @param slot    the slot
	 * @param clicked the item clicked
	 */
	protected void onMenuClick(final Player player, final int slot, final ItemStack clicked) {
	}

	/**
	 * Called automatically when a registered button is clicked
	 *
	 * <p>
	 * By default this method parses the click into
	 * {@link Button#onClickedInMenu(Player, Menu, ClickType)}
	 *
	 * @param player the player
	 * @param slot   the slot
	 * @param action the action
	 * @param click  the click
	 * @param button the button
	 */
	protected void onButtonClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final Button button) {
		button.onClickedInMenu(player, this, click);
	}

	/**
	 * Called automatically when the menu is closed
	 *
	 * @param player    the player
	 * @param inventory the menu inventory that is being closed
	 */
	protected void onMenuClose(final Player player, final Inventory inventory) {
	}

	@Override
	public final boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{}";
	}
}
