package kebriel.ctf.display.gui.component;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlaceholderValue {

    private ItemStack item;

    public PlaceholderValue(ItemStack item) {
        this.item = item;
    }

    public PlaceholderValue() {
    }

    public ItemStack getItem() {
        return item;
    }

    public static class MutablePlaceholderValue extends PlaceholderValue {

        private final List<ItemStack> mutableList;
        private int cursor;

        public MutablePlaceholderValue(List<ItemStack> list) {
            mutableList = list;
            cursor = -1;
        }

        @Override
        public ItemStack getItem() {
            if(cursor >= (mutableList.size()-1)) cursor = -1;
            return mutableList.get(cursor++);
        }
    }
}
