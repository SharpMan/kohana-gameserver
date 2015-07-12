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
public class EffectInstanceDate extends EffectInstance {

    public int Year;
    public byte Mounth, Day, Hour, Minute;

    @Override
    public byte SerializationIdentifier() {
        return 4;
    }

    public EffectInstanceDate(EffectInstance Parent, int Year, int Mounth, int Day, int Hour, int Minute) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.rawZone, Parent.delay, Parent.triggers, Parent.visibleInTooltip, Parent.visibleInFightLog, Parent.visibleInBuffUi);
        this.Year = Year;
        this.Mounth = (byte) Mounth;
        this.Day = (byte) Day;
        this.Hour = (byte) Hour;
        this.Minute = (byte) Minute;
    }

    public void SetDate(int Year, int Mounth, int Day, int Hour, int Minute) {
        this.Year = Year;
        this.Mounth = (byte) Mounth;
        this.Day = (byte) Day;
        this.Hour = (byte) Hour;
        this.Minute = (byte) Minute;
    }

    public EffectInstanceDate(IoBuffer buf) {
        super(buf);
        this.Year = buf.getInt();
        this.Mounth = buf.get();
        this.Day = buf.get();
        this.Hour = buf.get();
        this.Minute = buf.get();
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceDate(this, this.Year, this.Mounth, this.Day, this.Hour, this.Minute);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceDate)) {
            return false;
        }
        EffectInstanceDate rhs = (EffectInstanceDate) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(Year, rhs.Year).
                append(Mounth, rhs.Mounth).
                append(Day, rhs.Day).
                append(Hour, rhs.Hour).
                append(Minute, rhs.Minute).
                isEquals();
    }

    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        buf.putInt(Year);
        buf.put(Mounth);
        buf.put(Day);
        buf.put(Hour);
        buf.put(Minute);
    }

}
