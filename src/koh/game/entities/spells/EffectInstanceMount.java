package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceMount extends EffectInstance {

    @Override
    public byte SerializationIdentifier() {
        return 9;
    }

    public Double date;
    public int modelId;
    public int mountId;

    public EffectInstanceMount(EffectInstance Parent, Double date, int modelId, int mountId) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.rawZone, Parent.delay, Parent.triggers, Parent.visibleInTooltip, Parent.visibleInFightLog, Parent.visibleInBuffUi);
        this.date = date;
        this.modelId = modelId;
        this.mountId = mountId;
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceMount(this, this.date, this.modelId, this.mountId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceMount)) {
            return false;
        }
        EffectInstanceMount rhs = (EffectInstanceMount) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(date, rhs.date).
                append(modelId, rhs.modelId).
                append(mountId, rhs.mountId).
                isEquals();
    }

    public EffectInstanceMount(IoBuffer buf) {
        super(buf);
        this.date = buf.getDouble();
        this.modelId = buf.getInt();
        this.mountId = buf.getInt();
    }

    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        buf.putDouble(this.date);
        buf.putInt(this.modelId);
        buf.putInt(this.mountId);
    }

}
