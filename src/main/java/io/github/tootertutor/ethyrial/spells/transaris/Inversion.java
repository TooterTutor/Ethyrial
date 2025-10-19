package io.github.tootertutor.ethyrial.spells.transaris;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Inversion extends Spell implements AutoRegisterSpell {
    public Inversion(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "inversion",
                "Inversion", "Reverses the target's active status effects.",
                30,
                120,
                SpellDomain.TRANSARIS);
    }

    @Override
    protected void onCast(Player caster) {

    }

}
