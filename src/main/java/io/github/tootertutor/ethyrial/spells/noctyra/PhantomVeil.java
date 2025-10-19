package io.github.tootertutor.ethyrial.spells.noctyra;

import org.bukkit.entity.Player;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;
import io.github.tootertutor.ethyrial.spells.SpellDomain;

public class PhantomVeil extends Spell implements AutoRegisterSpell {
    public PhantomVeil(Ethyrial plugin) {
        super(Ethyrial.getInstance(),
                "phantomveil",
                "Phantom Veil",
                "Become invisible and leave behind a distracting illusion.",
                70,
                500,
                SpellDomain.NOCTYRA);
    }

    @Override
    protected void onCast(Player caster) {
        
    }

}
