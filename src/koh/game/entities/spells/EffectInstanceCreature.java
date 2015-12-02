package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceCreature extends EffectInstance {

    @Override
    public byte serializationIdentifier() {
        return 6;
    }

    public int monsterFamilyId;

    public EffectInstanceCreature(EffectInstance Parent, int monsterFamilyId) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.rawZone, Parent.delay, Parent.triggers, Parent.visibleInTooltip, Parent.visibleInFightLog, Parent.visibleInBuffUi);
        this.monsterFamilyId = monsterFamilyId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceCreature)) {
            return false;
        }
        EffectInstanceCreature rhs = (EffectInstanceCreature) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                appendSuper(super.equals(obj)).
                append(monsterFamilyId, rhs.monsterFamilyId).
                isEquals();
    }

    public EffectInstanceCreature(IoBuffer buf) {
        super(buf);
        this.monsterFamilyId = buf.getInt();
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceCreature(this, this.monsterFamilyId);
    }


    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        buf.putInt(monsterFamilyId);
    }

}
