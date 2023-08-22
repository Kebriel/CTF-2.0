package kebriel.ctf.entity.components;

public enum EquipmentSlots {
    HAND(0), LEGGINGS(3), CHESTPLATE(2), BOOTS(4), HELMET(1);

    private final int id;

    EquipmentSlots(int id) {
        this.id = id;
    }

    public int getSlotID() {
        return id;
    }
}
