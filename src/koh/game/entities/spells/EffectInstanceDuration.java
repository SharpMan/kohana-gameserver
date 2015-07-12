package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceDuration extends EffectInstance  {

    @Override
    public byte SerializationIdentifier() {
        return 7;
    }

    public int days;
    public byte hours, minutes;

    public EffectInstanceDuration(EffectInstance Parent, int days, byte hours, byte minutes) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.hidden, Parent.rawZone, Parent.delay, Parent.triggers, Parent.order);
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceDuration(this, this.days, this.hours, this.minutes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceDuration)) {
            return false;
        }
        EffectInstanceDuration rhs = (EffectInstanceDuration) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(days, rhs.days).
                append(hours, rhs.hours).
                append(minutes, rhs.minutes).
                isEquals();
    }

    public EffectInstanceDuration(IoBuffer buf) {
        super(buf);
        this.days = buf.getInt();
        this.hours = buf.get();
        this.minutes = buf.get();
    }
    
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        super.writeExternal(objectOutput);
        objectOutput.writeInt(this.days);
        objectOutput.writeByte(hours);
        objectOutput.writeByte(minutes);
    }

    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        buf.putInt(days);
        buf.put(hours);
        buf.put(minutes);
    }

}
