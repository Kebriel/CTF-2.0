package kebriel.ctf.display.gui.component.inventory;

import kebriel.ctf.display.gui.component.button.GUIButton;
import kebriel.ctf.display.gui.component.button.GUIButtonPortal;
import kebriel.ctf.display.gui.component.button.GUIButtonPurchase;
import kebriel.ctf.display.gui.menu.Abilities;
import kebriel.ctf.display.gui.menu.Items;
import kebriel.ctf.display.gui.menu.Perks;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.ReflectionUtil;
import org.bukkit.Material;

public enum Slot implements PlayerSlot {
	
	ABILITY_1(SlotType.ABILITY, Stat.SELECTED_ABILITY1), ABILITY_2(SlotType.ABILITY, Stat.SELECTED_ABILITY2), ABILITY_3(SlotType.ABILITY, Stat.SELECTED_ABILITY3, Stat.UNLOCKED_SLOT_ABILITY3, 5000), ABILITY_4(SlotType.ABILITY, Stat.SELECTED_ABILITY4, Stat.UNLOCKED_SLOT_ABILITY4, 10000, 0, Stat.UNLOCKED_SLOT_ABILITY3),
	EXTRA_ITEM(SlotType.ITEM, Stat.SELECTED_EXTRA_ITEM1, Stat.UNLOCKED_SLOT_ITEM1, 8000),
	PERK_1(SlotType.PERK, Stat.SELECTED_PERK1, Stat.UNLOCKED_SLOT_PERK1, 10000, 50), PERK_2(SlotType.PERK, Stat.SELECTED_PERK2, Stat.UNLOCKED_SLOT_PERK2, 20000, 150, Stat.UNLOCKED_SLOT_PERK1);

	private Stat unlockableData;
	private final Stat selectSlot;
	private int price;
	private int requiredLevel;
	private final SlotType type;
	private Stat prereqSlot;

	Slot(SlotType type, Stat selectSlot, Stat unlockableData, int price) {
		this(type, selectSlot);
		this.unlockableData = unlockableData;
		this.price = price;
	}

	Slot(SlotType type, Stat selectSlot, Stat unlockableData, int price, int requiredLevel) {
		this(type, selectSlot, unlockableData, price);
		this.requiredLevel = requiredLevel;
	}

	Slot(SlotType type, Stat selectSlot, Stat unlockableData, int price, int requiredLevel, Stat prereqSlot) {
		this(type, selectSlot, unlockableData, price, requiredLevel);
		this.prereqSlot = prereqSlot;
	}

	Slot(SlotType type, Stat selectSlot) {
		this.type = type;
		this.selectSlot = selectSlot;
	}

	public GUIButton getButton(GameGUI gui) {
		CTFPlayer player = gui.getPlayer();
		return !mustBeUnlocked() || isUnlocked(player) ? new GUIButtonPortal(getMenuItem(player), gui, ReflectionUtil.makeNewInstance(type.getGUIType(), player))
				: new GUIButtonPurchase(this, gui);
	}

	@Override
	public SlotType getType() {
		return type;
	}

	@Override
	public Stat getSelectSlot() {
		return selectSlot;
	}

	@Override
	public Stat getPrereqSlot() {
		return prereqSlot;
	}

	@Override
	public Stat getUnlockData() {
		return unlockableData;
	}

	@Override
	public int getCost() {
		return price;
	}

	@Override
	public int getRequiredLevel() {
		return requiredLevel;
	}

	public enum SlotType {

		ABILITY(Material.DIAMOND_BLOCK, Text.get().green("Ability Slot"), Text.get().aqua("Click to select an ability"), Abilities.class),
		ITEM(Material.GOLD_BLOCK, Text.get().yellow("Extra Inventory Slot"), Text.get().aqua("Click to select an extra tool").newLine().aqua("or consumable to start with each life"), Items.class),
		PERK(Material.REDSTONE_BLOCK, Text.get().gold("Perk Slot"), Text.get().aqua("Click to select a powerful perk"), Perks.class);

		private final Material icon;
		private final Text name;
		private final Text description;
		private final Class<? extends GUIBase> gui;

		SlotType(Material icon, Text name, Text description, Class<? extends GUIBase> gui) {
			this.icon = icon;
			this.name = name;
			this.description = description;
			this.gui = gui;
		}

		public Material getIcon() {
			return icon;
		}

		public Text getName() {
			return name;
		}

		public Text getDescription() {
			return description;
		}

		public Class<? extends GUIBase> getGUIType() {
			return gui;
		}
	}

}
