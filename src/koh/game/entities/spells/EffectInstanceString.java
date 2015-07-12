package koh.game.entities.spells;

import java.io.IOException;
import java.io.ObjectOutput;
import koh.protocol.client.BufUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class EffectInstanceString extends EffectInstance {

    @Override
    public byte SerializationIdentifier() {
        return 10;
    }

    public String text;

    public EffectInstanceString(EffectInstance Parent, String text) {
        super(Parent.effectUid, Parent.effectId, Parent.targetId, Parent.targetMask, Parent.duration, Parent.random, Parent.group, Parent.rawZone, Parent.delay, Parent.triggers, Parent.visibleInTooltip, Parent.visibleInFightLog, Parent.visibleInBuffUi);
        this.text = text;
    }

    @Override
    public EffectInstance Clone() {
        return new EffectInstanceString(this, this.text);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EffectInstanceString)) {
            return false;
        }
        EffectInstanceString rhs = (EffectInstanceString) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(text, rhs.text).
                isEquals();
    }

    public EffectInstanceString(IoBuffer buf) {
        super(buf);
        this.text = BufUtils.readUTF(buf);
    }

    @Override
    public void toBinary(IoBuffer buf) {
        super.toBinary(buf);
        BufUtils.writeUTF(buf, text);
    }

}
