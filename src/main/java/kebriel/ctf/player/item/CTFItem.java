package kebriel.ctf.player.item;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.util.JavaUtil;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CTFItem implements Cloneable {

	public enum AttributeModifier {
		ATTACK_DAMAGE("attackDamage", -8824246394029642496L, 209714653484829986L),
		ARMOR("armor", -5404615309788020608L, 6034806145186744244L),

		;

		private final String id;
		private final long UUIDLeast;
		private final long UUIDMost;

		AttributeModifier(String id, long UUIDLeast, long UUIDMost) {
			this.id = id;
			this.UUIDLeast = UUIDLeast;
			this.UUIDMost = UUIDMost;
		}

		public String getID() {
			return "generic." + id;
		}

		public long getUUIDLeast() {
			return UUIDLeast;
		}

		public long getUUIDMost() {
			return UUIDMost;
		}
	}


	/*
	 * Volatile objects are preferred over AtomicReferences
	 * as all operations concerning these objects are basic
	 * read-write operations. As such, an AtomicReference offers
	 * not additional value that 'volatile' doesn't already
	 * offer, while posing a slightly greater performance
	 * concern
	 */
	private volatile Material type;
	private volatile int amount;
	private volatile String name;
	private volatile byte data;
	private List<String> lore;
	private volatile boolean unbreakable;
	private Map<Enchantment, Integer> enchantments;
	private Set<ItemFlag> flags;
	private Map<AttributeModifier, Double> attributes;

	// Item-specific fields, most items won't use these
	private volatile Color leatherColor;
	private volatile DyeColor bannerColor;
	private volatile String skullOwner;

	// For potions
	private volatile boolean splashPotion;
	private volatile PotionType potionColor;
	private Map<PotionEffectType, Pair<Integer, Integer>> potionEffects;
	private volatile boolean potionIsAmbient;
	
	public static CTFItem newItem(Material mat) {
		return new CTFItem(mat);
	}

	{
		amount = 1; // Default
		lore = new CopyOnWriteArrayList<>();
		flags = new ConcurrentSet<>();
		enchantments = new ConcurrentHashMap<>();
		attributes = new ConcurrentHashMap<>();
		potionEffects = new ConcurrentHashMap<>();
	}

	private CTFItem(Material mat) {
		type = mat;
	}
	
	public CTFItem setName(String name) {
		this.name = name;
		return this;
	}

	public CTFItem setName(Text name) {
		setName(name.toString());
		return this;
	}

	public CTFItem setData(byte data) {
		this.data = data;
		return this;
	}
	
	public CTFItem addLore(String... lines) {
		lore.addAll(Arrays.asList(lines));
		return this;
	}

	public CTFItem addLore(Text lines) {
		addLore(lines.build());
		return this;
	}
	
	public CTFItem setLore(int index, String newLine) {
		if(index < 0 || index >= lore.size())
			return this;

		lore.set(index, newLine);
		return this;
	}

	public CTFItem setLore(String... newLore) {
		setLore(Arrays.asList(newLore));
		return this;
	}

	public CTFItem setLore(List<String> newLore) {
		lore.clear();
		lore.addAll(newLore);
		return this;
	}
	
	public CTFItem setAmount(int amount) {
		this.amount = amount;
		return this;
	}
	
	public CTFItem setUnbreakable() {
		unbreakable = true;
		return this;
	}
	
	public CTFItem addEnchantment(Enchantment ench, int lvl) {
		enchantments.put(ench, lvl);
		return this;
	}
	
	public CTFItem hideAttributes() {
		addFlag(ItemFlag.HIDE_ATTRIBUTES);
		return this;
	}
	
	public CTFItem addFlag(ItemFlag flag) {
		flags.add(flag);
		return this;
	}

	public CTFItem addAttribute(AttributeModifier modifier, double value) {
		attributes.put(modifier, value);
		return this;
	}

	private ItemStack addAttribute(ItemStack item, AttributeModifier modifier, double value) {
		net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound itemNBT = stack.getTag();
		if(itemNBT == null)
			itemNBT = new NBTTagCompound();
		NBTTagList modifiers = itemNBT.getList("AttributeModifiers", 10);
		NBTTagCompound modifierCompound = new NBTTagCompound();
		modifierCompound.set("AttributeName", new NBTTagString(modifier.getID()));
		modifierCompound.set("Name", new NBTTagString(modifier.getID()));
		modifierCompound.set("Amount", new NBTTagDouble(value));
		modifierCompound.set("Operation", new NBTTagInt(0));
		modifierCompound.set("UUIDLeast", new NBTTagLong(modifier.UUIDLeast));
		modifierCompound.set("UUIDMost", new NBTTagLong(modifier.UUIDMost));
		modifiers.add(modifierCompound);
		itemNBT.set("AttributeModifiers", modifiers);
		stack.setTag(itemNBT);
		return CraftItemStack.asBukkitCopy(stack);
	}
	
	public CTFItem dyeLeather(Color color) {
		this.leatherColor = color;
		return this;
	}
	
	public CTFItem addAffectToPotion(PotionEffectType type, int strength, int duration, boolean potionIsAmbient) {
		potionEffects.put(type, Pair.of(strength, duration));
		this.potionIsAmbient = potionIsAmbient;
		return this;
	}
	
	public CTFItem addColorToPot(PotionType type) {
		this.potionColor = type;
		return this;
	}
	
	public CTFItem setPotionAsSplash() {
		splashPotion = true;
		return this;
	}
	
	public CTFItem setSkull(PlayerState player) {
		skullOwner = player.getName();
		return this;
	}

	public CTFItem setType(Material type) {
		this.type = type;
		return this;
	}

	public CTFItem setBannerColor(DyeColor color) {
		bannerColor = color;
		return this;
	}

	public int getAmount() {
		return amount;
	}

	protected Map<AttributeModifier, Double> getAttributes() {
		return attributes;
	}

	public ItemStack build() {
		ItemStack item = new ItemStack(type, amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);

		MaterialData data = item.getData();
		data.setData(this.data);
		item.setData(data);

		meta.setLore(lore);
		meta.spigot().setUnbreakable(unbreakable);

		for(Enchantment ench : enchantments.keySet())
			meta.addEnchant(ench, enchantments.get(ench), true);

		meta.addItemFlags(JavaUtil.typeArray(flags, ItemFlag.class));

		for(AttributeModifier attribute : attributes.keySet())
			item = addAttribute(item, attribute, attributes.get(attribute));

		if(leatherColor != null && meta instanceof LeatherArmorMeta leather)
			leather.setColor(Color.fromRGB(leatherColor.asRGB()));

		if(bannerColor != null)
			if(item.getType() == Material.BANNER && item.getItemMeta() instanceof BannerMeta bannerMeta)
				bannerMeta.setBaseColor(bannerColor);

		if(skullOwner != null && meta instanceof SkullMeta skull)
			skull.setOwner(skullOwner);

		if(meta instanceof PotionMeta potionMeta) {
			for(PotionEffectType pot : potionEffects.keySet()) {
				PotionEffect effect = new PotionEffect(pot, potionEffects.get(pot).getRight(), potionEffects.get(pot).getLeft(), potionIsAmbient, false);
				potionMeta.addCustomEffect(effect, true);
			}

			Potion potion = Potion.fromItemStack(item);
			potion.setSplash(splashPotion);
			potion.setType(potionColor);
			item = potion.toItemStack(amount);
		}

		item.setItemMeta(meta);
		return item;
	}

	@Override
	public CTFItem clone() {
		try {
			CTFItem cloned = (CTFItem) super.clone();
			cloned.lore = new CopyOnWriteArrayList<>(lore);
			cloned.enchantments = new ConcurrentHashMap<>(enchantments);
			cloned.flags = new CopyOnWriteArraySet<>(flags);
			cloned.attributes = new ConcurrentHashMap<>(attributes);
			cloned.potionEffects = new ConcurrentHashMap<>(potionEffects);
			return cloned;
		} catch(CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return this;
	}

}
