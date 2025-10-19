package io.github.tootertutor.ethyrial.spells.auralis;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Frostbite extends Spell implements AutoRegisterSpell {
    public Frostbite(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "frostbite",
                "Frostbite",
                "Releases a cone of cold that slows and damages foes.",
                20,
                120,
                SpellDomain.AURALIS);
    }

    @Override
    protected void onCast(Player caster) {

    }
}
