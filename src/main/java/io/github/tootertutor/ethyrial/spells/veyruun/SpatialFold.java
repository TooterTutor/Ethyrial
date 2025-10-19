package io.github.tootertutor.ethyrial.spells.veyruun;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class SpatialFold extends Spell implements AutoRegisterSpell {
    public SpatialFold(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "spatialfold",
                "Spatial Fold",
                "Swapping places with a target entity",
                40,
                200,
                SpellDomain.VEYRUUN);
    }

    @Override
    protected void onCast(Player caster) {

    }

}
