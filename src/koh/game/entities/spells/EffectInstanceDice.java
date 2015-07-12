package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import koh.protocol.client.BufUtils;
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
        this.visibleInTooltip = BufUtils.readBoolean(buf);
        this.random = buf.getInt();
        this.rawZone = readUTF(buf);
        this.targetId = buf.getInt();
        this.targetMask = readUTF(buf);
        this.effectId = buf.getInt();
        this.diceNum = buf.getInt();
        this.duration = buf.getInt();
        this.visibleInFightLog = BufUtils.readBoolean(buf);
        this.effectUid = buf.getInt();
        this.diceSide = buf.getInt();
        this.value = buf.getInt();
        this.visibleInBuffUi = BufUtils.readBoolean(buf);
        this.delay = buf.getInt();
        this.triggers = readUTF(buf);
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
    public void toBinary(IoBuffer buf) {
        BufUtils.writeBoolean(buf, visibleInTooltip);
        buf.putInt(this.random);
        writeUTF(buf, this.rawZone);
        buf.putInt(this.targetId);
        writeUTF(buf, this.targetMask);
        buf.putInt(this.effectId);
        buf.putInt(this.diceNum);
        buf.putInt(this.duration);
        BufUtils.writeBoolean(buf, visibleInFightLog);
        buf.putInt(this.effectUid);
        buf.putInt(this.diceSide);
        buf.putInt(this.value);
        BufUtils.writeBoolean(buf, visibleInBuffUi);
        buf.putInt(this.delay);
        writeUTF(buf, this.triggers);
        buf.putInt(this.group);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
