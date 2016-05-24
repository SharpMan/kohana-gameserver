package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPAEsquive;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPAEsquive extends EffectBase {

    private static final EffectSubPaAfterHealed CAENGAL = new EffectSubPaAfterHealed();

    @Override
    public int applyEffect(EffectCast castInfos) {
        if(!castInfos.isCAC && castInfos.spellLevel.getSpellId() == 112){
            return CAENGAL.applyEffect(castInfos);
        }
        final MutableInt damageValue = new MutableInt();
        if (castInfos.duration >= 1) {
            for (Fighter target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);
                subInfos.glyphId = castInfos.glyphId;
                final BuffSubPAEsquive buff = new BuffSubPAEsquive(subInfos, target);
                if (!target.getBuff().buffMaxStackReached(buff.castInfos)) {
                    buff.applyEffect(damageValue, null);
                    target.getBuff().addBuff(buff);
                }
            }
        } else {
            for (Fighter target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, 0, 0);
                subInfos.glyphId = castInfos.glyphId;
                final BuffSubPAEsquive buff = new BuffSubPAEsquive(subInfos, target);
                if (!target.getBuff().buffMaxStackReached(buff.castInfos)) {
                    buff.applyEffect(damageValue, null);
                    target.getBuff().addBuff(buff);
                }
            }
        }

        return -1;
    }

}
