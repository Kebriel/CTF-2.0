package kebriel.ctf.player.item;

import kebriel.ctf.player.item.InventoryProfile.InventoryProfileType;

public enum InvSlot {

    HOTBAR_1(0){
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.STATS_ITEM;
                case GAME -> Item.BASIC_SWORD;
            };
        }
    },
    HOTBAR_2(1) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.ABILITIES;
                case GAME -> Item.BASIC_BOW;
            };
        }
    },
    HOTBAR_3(2) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return null;
        }
    },
    HOTBAR_4(3) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.BLUE_QUEUE;
                case GAME -> null;
            };
        }
    },
    HOTBAR_5(4) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.CLEAR_QUEUE;
                case GAME -> null;
            };
        }
    },
    HOTBAR_6(5) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.RED_QUEUE;
                case GAME -> null;
            };
        }
    },
    HOTBAR_7(6) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return null;
        }
    },
    HOTBAR_8(7) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.COSMETICS;
                case GAME -> Item.ARROWS;
            };
        }
    },
    HOTBAR_9(8) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> Item.SETTINGS;
                case GAME -> Item.TRACKER;
            };
        }
    },
    HELMET(103) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> null;
                case GAME -> Item.HELMET;
            };
        }
    },
    CHESTPLATE(102) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> null;
                case GAME -> Item.CHESTPLATE;
            };
        }
    },
    LEGGINGS(101) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> null;
                case GAME -> Item.LEGGINGS;
            };
        }
    },
    BOOTS(100) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return switch(type) {
                case LOBBY -> null;
                case GAME -> Item.BOOTS;
            };
        }
    },
    EXTRA_ITEM(-1) {
        @Override
        public Item getDefaultFor(InventoryProfileType type) {
            return null;
        }
    };

    private final int rawSlot;

    InvSlot(int rawSlot) {
        this.rawSlot = rawSlot;
    }

    public abstract Item getDefaultFor(InventoryProfileType type);

    public int getRawSlot() {
        return rawSlot;
    }

}
