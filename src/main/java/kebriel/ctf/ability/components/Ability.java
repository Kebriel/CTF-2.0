package kebriel.ctf.ability.components;

import kebriel.ctf.display.gui.component.Selectable;

public interface Ability extends Selectable {

	Ability getInstance();
	AbilityType getType();
}
