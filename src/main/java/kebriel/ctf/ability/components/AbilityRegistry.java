package kebriel.ctf.ability.components;

import java.util.HashSet;
import java.util.Set;

import kebriel.ctf.ability.AbilityAlchemist;

public class AbilityRegistry {

	private static final Set<Ability> registered = new HashSet<>();

	static {
		registered.add(new AbilityAlchemist());
	}

	public static Set<Ability> get() {
		return registered;
	}

	public static Set<Ability> get(AbilityType filter) {
		Set<Ability> result = new HashSet<>();
		for(Ability a : registered)
			if(a.getType() == filter)
				result.add(a);
		return result;
	}
}
