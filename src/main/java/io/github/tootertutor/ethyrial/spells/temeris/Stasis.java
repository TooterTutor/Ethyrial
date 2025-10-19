package io.github.tootertutor.ethyrial.spells.temeris;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Stasis extends Spell implements AutoRegisterSpell {
    public Stasis(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "stasis",
                "Stasis",
                "Stop an entity for a short duration.",
                60,
                800,
                SpellDomain.TEMERIS);
    }

    @Override
    protected void onCast(Player caster) {
        
    }
}
