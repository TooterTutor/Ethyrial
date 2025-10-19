package io.github.tootertutor.ethyrial.spells.noctyra;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Disorient extends Spell implements AutoRegisterSpell {

    protected Disorient(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "disorient",
                "Disorient",
                "Teleport away from danger and leave behind a convincing illusion",
                60,
                420,
                SpellDomain.NOCTYRA);
    }

    @Override
    protected void onCast(Player caster) {
        // Location location = caster.getLocation().clone();

    }

}
