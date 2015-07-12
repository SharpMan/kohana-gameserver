package koh.game.entities.spells;

import koh.protocol.client.BufUtils;
import static koh.protocol.client.BufUtils.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceInteger extends EffectInstance {

    public static final int classID = 2;

    @Override
    public byte SerializationIdentifier() {
        return 2;
    }

    public int value;

    public EffectInstanceInteger(int effectId, int value) {
        super(effectId);
        this.value = value;
    }

    public EffectInstanceInteger(EffectInstance Parent, int value) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.rawZone, Parent.delay, Parent.triggers, Parent.visibleInTooltip, Parent.visibleInFightLog, Parent.visibleInBuffUi);
        this.value = value;
    }

    public EffectInstanceInteger SetValue(int value) {
        this.value = value;
        return this;
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceInteger(this, this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceInteger)) {
            return false;
        }
        EffectInstanceInteger rhs = (EffectInstanceInteger) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(value, rhs.value).
                isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public EffectInstanceInteger(IoBuffer buf) {
        super(null);
        if (buf == null) {
            return;
        }
        this.visibleInTooltip = BufUtils.readBoolean(buf);
        this.random = buf.getInt();
        this.rawZone = readUTF(buf);
        this.targetId = buf.getInt();
        this.targetMask = readUTF(buf);
        this.effectId = buf.getInt();
        this.duration = buf.getInt();
        this.visibleInFightLog = BufUtils.readBoolean(buf);
        this.effectUid = buf.getInt();
        this.value = buf.getInt();
        this.visibleInBuffUi = BufUtils.readBoolean(buf);
        this.triggers = readUTF(buf);
        this.delay = buf.getInt();
        this.group = buf.getInt();
    }

    @Override
    public void toBinary(IoBuffer buf) {
        BufUtils.writeBoolean(buf, this.visibleInTooltip);
        buf.putInt(this.random);
        BufUtils.writeUTF(buf, rawZone);
        buf.putInt(this.targetId);
        BufUtils.writeUTF(buf, targetMask);
        buf.putInt(this.effectId);
        buf.putInt(this.duration);
        BufUtils.writeBoolean(buf, this.visibleInFightLog);
        buf.putInt(this.effectUid);
        buf.putInt(this.value);
        BufUtils.writeBoolean(buf, this.visibleInBuffUi);
        BufUtils.writeUTF(buf, triggers);
        buf.putInt(this.delay);
        buf.putInt(group);
    }

}
