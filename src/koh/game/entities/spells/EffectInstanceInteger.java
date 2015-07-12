package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import koh.protocol.client.BufUtils;
import static koh.protocol.client.BufUtils.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceInteger extends EffectInstance{

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
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.hidden, Parent.rawZone, Parent.delay, Parent.triggers, Parent.order);
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
        this.random = buf.getInt();
        this.rawZone = readUTF(buf);
        this.targetId = buf.getInt();
        this.targetMask = readUTF(buf);
        this.effectId = buf.getInt();
        this.duration = buf.getInt();
        this.order = buf.getInt();
        this.effectUid = buf.getInt();
        this.hidden = readBoolean(buf);
        this.value = buf.getInt();
        this.delay = buf.getInt();
        this.triggers = readUTF(buf);
        this.group = buf.getInt();
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.random);
        writeUTF(objectOutput, this.rawZone);
        objectOutput.writeInt(this.targetId);
        writeUTF(objectOutput, this.targetMask);
        objectOutput.writeInt(this.effectId);
        objectOutput.writeInt(this.duration);
        objectOutput.writeInt(this.order);
        objectOutput.writeInt(this.effectUid);
        writeBoolean(objectOutput, this.hidden);
        objectOutput.writeInt(this.value);
        objectOutput.writeInt(this.delay);
        writeUTF(objectOutput, this.triggers);
        objectOutput.writeInt(this.group);
    }

    @Override
    public void toBinary(IoBuffer buf) {
        buf.putInt(this.random);
        BufUtils.writeUTF(buf, rawZone);
        buf.putInt(this.targetId);
        BufUtils.writeUTF(buf, targetMask);
        buf.putInt(this.effectId);
        buf.putInt(this.duration);
        buf.putInt(this.order);
        buf.putInt(this.effectUid);
        BufUtils.writeBoolean(buf, hidden);
        buf.putInt(this.value);
        buf.putInt(this.delay);
        BufUtils.writeUTF(buf, triggers);
        buf.putInt(group);
    }
    

}
