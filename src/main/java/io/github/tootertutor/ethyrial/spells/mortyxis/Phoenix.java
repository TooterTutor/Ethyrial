package io.github.tootertutor.ethyrial.spells.mortyxis;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class Phoenix extends Spell implements AutoRegisterSpell {

    protected Phoenix(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "phoenix",
                "Phoenix",
                "Revive another at the cost of your life",
                80,
                60,
                SpellDomain.MORTYXIS);
    }

    @Override
    protected void onCast(Player caster) {
        
    }

}
