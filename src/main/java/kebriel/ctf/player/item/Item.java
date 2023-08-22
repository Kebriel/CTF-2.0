package kebriel.ctf.player.item;

import kebriel.ctf.Constants;
import kebriel.ctf.display.gui.menu.Cosmetics;
import kebriel.ctf.display.gui.menu.Overview;
import kebriel.ctf.display.gui.menu.Tracker;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.util.ColorMappings;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public enum Item {

    // Lobby items
    STATS_ITEM(
            InvSlot.HOTBAR_1,
            () -> CTFItem.newItem(Material.SKULL_ITEM)
            .setName(Text.get().italicColor(ChatColor.GREEN, "stats"))
            .addLore(Text.get().aqua("Shows your game stats")),
            CTFItem::setSkull) {
        @Override
        public void onClick(CTFPlayer player) {
            player.getBukkitPlayer().performCommand("stats");
        }
    },
    ABILITIES(InvSlot.HOTBAR_2,
            () -> CTFItem.newItem(Material.BEACON)
                    .setName(Text.get().gold("Abilities & Perks"))
                    .addLore(Text.get().aqua("Select abilities and customize").newLine().aqua("your loadout"))) {
        @Override
        public void onClick(CTFPlayer player) {
            player.openMenu(new Overview(player));
        }
    },
    BLUE_QUEUE(InvSlot.HOTBAR_4,
            () -> CTFItem.newItem(Material.WOOL)
                    .setName(Text.get().blue("Blue Team"))
                    .addLore(Text.get().aqua("Enter the queue for the ").blue("Blue Team"))
                    .setData((byte)11)) {
        @Override
        public void onClick(CTFPlayer player) {
            Teams.getQueue(TeamColor.BLUE).attemptJoin(player);
        }
    },
    CLEAR_QUEUE(InvSlot.HOTBAR_5,
            () -> CTFItem.newItem(Material.WOOL)
                    .setName(Text.get().yellow("Clear Queue"))
                    .addLore(Text.get().aqua("Clear yourself from any queues to join a team"))) {
        @Override
        public void onClick(CTFPlayer player) {
            Teams.clearFromQueue(player);
        }
    },
    RED_QUEUE(InvSlot.HOTBAR_6,
            () -> CTFItem.newItem(Material.WOOL)
                    .setName(Text.get().red("Red Team"))
                    .addLore(Text.get().aqua("Enter the queue for the ").red("Red Team"))
                    .setData((byte)14)) {
        @Override
        public void onClick(CTFPlayer player) {
            Teams.getQueue(TeamColor.RED).attemptJoin(player);
        }
    },
    COSMETICS(InvSlot.HOTBAR_8,
            () -> CTFItem.newItem(Material.EMERALD)
                    .setName(Text.get().purple("Cosmetics"))
                    .addLore(Text.get().aqua("Browse game and lobby cosmetics"))) {
        @Override
        public void onClick(CTFPlayer player) {
            player.openMenu(new Cosmetics(player));
        }
    },
    SETTINGS(InvSlot.HOTBAR_9,
            () -> CTFItem.newItem(Material.DIODE)
                    .setName(Text.get().italicColor(ChatColor.WHITE, "/settings"))
                    .addLore(Text.get().aqua("Configure various settings to your liking"))) {
        @Override
        public void onClick(CTFPlayer player) {
            // WIP
        }
    },
    BASIC_SWORD(InvSlot.HOTBAR_1,
            () -> CTFItem.newItem(Material.STONE_SWORD)
                    .setName(Text.get().text("Basic Stone Sword"))
                    .setUnbreakable()),
    BASIC_BOW(InvSlot.HOTBAR_2,
            () -> CTFItem.newItem(Material.BOW)
                    .setName(Text.get().text("Basic Bow"))
                    .setUnbreakable()),
    ARROWS(InvSlot.HOTBAR_8,
            () -> CTFItem.newItem(Material.ARROW)
                    .setName(Text.get().text("Basic Arrows"))
                    .addLore(Text.get().green("Cannot be retrieved after").newLine().green("being shot"))
                    .setAmount(5)),
    TRACKER(InvSlot.HOTBAR_9,
            () -> CTFItem.newItem(Material.COMPASS)
                    .setName(Text.get().text("Tracking: "))
                    .addLore(Text.get().aqua("Click to select a flag to track"))) {
        @Override
        public void onClick(CTFPlayer player) {
            if(!GameStage.IN_MAP.get())
                return;

            player.openMenu(new Tracker(player));
        }
    },
    HELMET(InvSlot.HELMET,
            () -> CTFItem.newItem(Material.LEATHER_HELMET)
                    .setName(Text.get().text("Leather Helmet"))
                    .setUnbreakable(),
            (item, player) -> item.dyeLeather(ColorMappings.chatToDyeColor(player.getCTFPlayer().getTeam().getChatColor()).getColor())),
    CHESTPLATE(InvSlot.CHESTPLATE,
            () -> CTFItem.newItem(Material.IRON_CHESTPLATE)
                    .setName(Text.get().text("Iron Chestplate"))
                    .setUnbreakable()),
    LEGGINGS(InvSlot.LEGGINGS,
            () -> CTFItem.newItem(Material.LEATHER_LEGGINGS)
                    .setName(Text.get().text("Leather Leggings"))
                    .setUnbreakable(),
            (item, player) -> item.dyeLeather(ColorMappings.chatToDyeColor(player.getCTFPlayer().getTeam().getChatColor()).getColor())),
    BOOTS(InvSlot.BOOTS,
            () -> CTFItem.newItem(Material.LEATHER_BOOTS)
                    .setName(Text.get().text("Leather Boots"))
                    .setUnbreakable(),
            (item, player) -> item.dyeLeather(ColorMappings.chatToDyeColor(player.getCTFPlayer().getTeam().getChatColor()).getColor())),

    FIREBRAND(InvSlot.HOTBAR_1,
            () -> CTFItem.newItem(Material.WOOD_SWORD).setName(ChatColor.GOLD + "Firebrand").setUnbreakable().addFlag(ItemFlag.HIDE_UNBREAKABLE)),
    KEVLAR_VEST(InvSlot.CHESTPLATE,
            () -> CTFItem.newItem(Material.CHAINMAIL_CHESTPLATE).setName(ChatColor.GOLD + "Kevlar Mail")
                    .setUnbreakable()
                    .addFlag(ItemFlag.HIDE_UNBREAKABLE)
                    .addLore(ChatColor.YELLOW + "You receive " + ChatColor.AQUA + "45%" + ChatColor.YELLOW + " less damage from")
                    .addLore(ChatColor.YELLOW + "ranged attacks")),
    BANDAGE(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.PAPER).setName(Text.get().gold("Medical Bandage"))),
    GHOST_BLOCKS(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.GLASS).setName(Text.get().gold("Ghost Block")).setAmount(Constants.ITEM_BLOCKS_SPAWN_AMOUNT)),
    FISHING_ROD(InvSlot.HOTBAR_3,
            () -> CTFItem.newItem(Material.FISHING_ROD).setName(ChatColor.GOLD + "Battle Rod").setUnbreakable().addFlag(ItemFlag.HIDE_UNBREAKABLE)),
    GAPPLE(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.GOLDEN_APPLE).setName(ChatColor.GOLD + "Golden Apple")),
    KNOCKBACK_STICK(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.STICK).setName(ChatColor.GOLD + "Knockback Stick").addEnchantment(Enchantment.KNOCKBACK, Constants.ITEM_KB_STICK_STRENGTH)),
    PEARL(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.ENDER_PEARL).setName(ChatColor.GOLD + "Unstable Pearl")),
    SNOWBALLS(InvSlot.EXTRA_ITEM,
            () -> CTFItem.newItem(Material.SNOW_BALL).setName(ChatColor.GOLD + "Snowball").setAmount(16)
                    .addLore(Text.get().green("Inflicts ").gray("Slowness " + JavaUtil.asNumeral(Constants.ITEM_SNOWBALL_SLOW_STRENGTH) + " (" + Constants.ITEM_SNOWBALL_SLOW_DURATION + "s)"))),
    ROYALTY_CROWN(InvSlot.HELMET,
            () -> CTFItem.newItem(Material.GOLD_HELMET).setName(ChatColor.GOLD + "Royal Mantle")
                    .setUnbreakable().addFlag(ItemFlag.HIDE_UNBREAKABLE))
    ;

    private final InvSlot slot;
    // Prevents this enum from holding an actual CTFItem (mutable, and therefor prone to unexpected bugs in this context)
    private final Supplier<CTFItem> base;
    private BiFunction<CTFItem, PlayerState, CTFItem> applyItem;

    Item(InvSlot slot, Supplier<CTFItem> base, BiFunction<CTFItem, PlayerState, CTFItem> apply) {
        this(slot, base);
        this.applyItem = apply;
    }

    Item(InvSlot slot, Supplier<CTFItem> base) {
        this.slot = slot;
        this.base = base;
    }

    public void onClick(CTFPlayer player) {}

    public CTFItem getItem() {
        return base.get();
    }

    /**
     * Gives the provided player this item. Does so by first getting a new CTFItem
     * in accordance with this enum value's defined Supplier object, and then applying
     * whatever predefined player-specific effects (if present) were defined for this item,
     * before then setting the item to the corresponding inventory slot in the player's
     * inventory
     */
    public void giveItem(PlayerState player) {
        player.getInventory().setItem(slot.getRawSlot(), applyItem.apply(getItem(), player).build());
    }

    public ItemStack buildItem() {
        return base.get().build();
    }
}
