package kebriel.ctf.player.item;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InventoryProfile {

    public enum InventoryProfileType {
        LOBBY(GameStage.LOBBY), GAME(GameStage.IN_GAME, GameStage.POST_GAME);

        private final GameStage[] allowedWhen;

        InventoryProfileType(GameStage... allowedWhen) {
            this.allowedWhen = allowedWhen;
        }

        public boolean isCorrectStage() {
            boolean correct = false;
            for(GameStage stage : allowedWhen)
                correct = stage.get();
            return correct;
        }

        public GameStage[] getAllowedStages() {
            return allowedWhen;
        }
    }

    private final Map<InvSlot, Item> slots;
    private final Set<Item> extraItems;

    {
        slots = new ConcurrentHashMap<>();
        extraItems = new ConcurrentSet<>();
    }

    private final PlayerState player;
    private final InventoryProfileType type;

    public InventoryProfile(PlayerState player, InventoryProfileType type) {
        this.player = player;
        this.type = type;

        reset();
    }

    private void reset() {
        for(InvSlot inv : InvSlot.values())
            slots.put(inv, inv.getDefaultFor(type));
    }

    private void fetchFromAbilities() {
        for(ItemAbility invAb : player.getCTFPlayer().getSelected(ItemAbility.class))
            invAb.setItem(player);
    }

    public void setItemInSlot(InvSlot slot, Item item) {
        if(slot == InvSlot.EXTRA_ITEM) {
            extraItems.add(item);
            return;
        }
        slots.put(slot, item);
    }

    /**
     * Apply the code entailed within the provided Consumer to
     * the cached CTFItem in the provided slot
     */
    public void applyToItemInSlot(InvSlot slot, Consumer<CTFItem> apply) {
        apply.accept(getItemInSlot(slot));
    }

    /**
     * Gets the cached CTFItem in the specified slot. If this has not been
     * manually changed elsewhere in the code, the item returned will be a copy
     * of whatever this slot's default item is (if it has one). Otherwise, it
     * will be whatever the item has been set to be elsewhere (by inventory
     * abilities, for example).
     */
    public CTFItem getItemInSlot(InvSlot slot) {
        if(slot == InvSlot.EXTRA_ITEM)
            return null;
        return slots.get(slot).getItem();
    }

    public void prepare() {
        reset();
        fetchFromAbilities();
    }

    public void applyToPlayer() {
        MinecraftUtil.doSyncIfNot(() -> {
            for(InvSlot inv : slots.keySet())
                player.getInventory().setItem(inv.getRawSlot(), slots.get(inv).buildItem());
            for(Item i : extraItems)
                player.getInventory().addItem(i.buildItem());
        });
    }

    public void passInteract(PlayerInteractEvent event) {
        for(Item i : slots.values())
            i.onClick(player.getCTFPlayer());
    }

    public boolean shouldBeActive() {
        return type.isCorrectStage();
    }

    public InventoryProfileType getType() {
        return type;
    }
}
