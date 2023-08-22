package kebriel.ctf.display.cosmetic.component;

import java.util.HashSet;
import java.util.Set;

import kebriel.ctf.display.cosmetic.AuraCrown;
import kebriel.ctf.display.cosmetic.AuraDemon;
import kebriel.ctf.display.cosmetic.AuraFlaming;
import kebriel.ctf.display.cosmetic.AuraVoid;
import kebriel.ctf.display.cosmetic.AuraWings;

public class CosmeticRegistry {

	private static final Set<Cosmetic> registered = new HashSet<>();

	static {
		registered.add(new AuraCrown());
		registered.add(new AuraDemon());
		registered.add(new AuraFlaming());
		registered.add(new AuraVoid());
		registered.add(new AuraWings());
	}

	public static Set<Cosmetic> getRegistered() {
		return registered;
	}

	public static Set<Cosmetic> get(CosmeticType filter) {
		Set<Cosmetic> result = new HashSet<>();
		for(Cosmetic c : registered)
			if(c.getType() == filter)
				result.add(c);
		return result;
	}

}
