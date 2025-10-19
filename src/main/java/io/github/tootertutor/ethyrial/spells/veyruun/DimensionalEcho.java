package io.github.tootertutor.ethyrial.spells.veyruun;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class DimensionalEcho extends Spell implements AutoRegisterSpell {
    public DimensionalEcho(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "dimensionalecho",
                "Dimensional Echo",
                "Summons a delayed spectral clone that mimics your actions.",
                30,
                100,
                SpellDomain.VEYRUUN);
    }

    @Override
    protected void onCast(Player caster) {

    }
}
