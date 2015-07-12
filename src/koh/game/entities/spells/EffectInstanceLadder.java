package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceLadder extends EffectInstanceCreature {

    @Override
    public byte SerializationIdentifier() {
        return 8;
    }

    public int monsterCount;

    public EffectInstanceLadder(EffectInstance Parent, int monsterFamilyId, int monsterCount) {
        super(Parent, monsterFamilyId);
        this.monsterCount = monsterCount;
    }
    
    @Override
    public EffectInstance Clone() {
        return new EffectInstanceLadder(this, this.monsterFamilyId,this.monsterCount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceLadder)) {
            return false;
        }
        EffectInstanceLadder rhs = (EffectInstanceLadder) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(monsterCount, rhs.monsterCount).
                isEquals();
    }

    public EffectInstanceLadder(IoBuffer buf) {
        super(buf);
        this.monsterCount = buf.getInt();
    }
    

    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        buf.putInt(monsterCount);
    }

}
