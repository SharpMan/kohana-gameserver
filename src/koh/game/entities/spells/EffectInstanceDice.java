package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import static koh.protocol.client.BufUtils.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceDice extends EffectInstanceInteger {

    public static final int classID = 3;

    @Override
    public byte SerializationIdentifier() {
        return 3;
    }

    public int diceNum;
    public int diceSide;

    public EffectInstanceDice(EffectInstance Parent, int value, int diceNum, int diceSide) {
        super(Parent, value);
        this.diceNum = diceNum;
        this.diceSide = diceSide;
    }

    public int[] GetValues() {
        return new int[]{
            this.diceNum,
            this.diceSide,
            this.value
        };
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceDice(this, this.value, this.diceNum, this.diceSide);
    }

    public EffectInstanceDice(IoBuffer buf) {
        super(null);
        this.random = buf.getInt();
        this.rawZone = readUTF(buf);
        this.targetId = buf.getInt();
        this.targetMask = readUTF(buf);
        this.effectId = buf.getInt();
        this.diceNum = buf.getInt();
        this.duration = buf.getInt();
        this.order = buf.getInt();
        this.effectUid = buf.getInt();
        this.hidden = readBoolean(buf);
        this.diceSide = buf.getInt();
        this.value = buf.getInt();
        this.triggers = readUTF(buf);
        this.delay = buf.getInt();
        this.group = buf.getInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceDice)) {
            return false;
        }
        EffectInstanceDice rhs = (EffectInstanceDice) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(diceNum, rhs.diceNum).
                append(diceSide, rhs.diceSide).
                isEquals();
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.random);
        writeUTF(objectOutput, this.rawZone);
        objectOutput.writeInt(this.targetId);
        writeUTF(objectOutput, this.targetMask);
        objectOutput.writeInt(this.effectId);
        objectOutput.writeInt(this.diceNum);
        objectOutput.writeInt(this.duration);
        objectOutput.writeInt(this.order);
        objectOutput.writeInt(this.effectUid);
        writeBoolean(objectOutput, this.hidden);
        objectOutput.writeInt(this.diceSide);
        objectOutput.writeInt(this.value);
        writeUTF(objectOutput, this.triggers);
        objectOutput.writeInt(this.delay);
        objectOutput.writeInt(this.group);
    }

    @Override
    public void toBinary(IoBuffer buf) {
        buf.putInt(this.random);
        writeUTF(buf, this.rawZone);
        buf.putInt(this.targetId);
        writeUTF(buf, this.targetMask);
        buf.putInt(this.effectId);
        buf.putInt(this.diceNum);
        buf.putInt(this.duration);
        buf.putInt(this.order);
        buf.putInt(this.effectUid);
        writeBoolean(buf, this.hidden);
        buf.putInt(this.diceSide);
        buf.putInt(this.value);
        writeUTF(buf, this.triggers);
        buf.putInt(this.delay);
        buf.putInt(this.group);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
