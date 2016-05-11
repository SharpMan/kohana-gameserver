package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPMEsquive;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPMEsquive extends EffectBase {


    private static final EffectSubPaAfterHealed CAENGAL = new EffectSubPaAfterHealed();

    @Override
    public int applyEffect(EffectCast castInfos) {
        if(castInfos.spellLevel.getSpellId() == 112){
            return CAENGAL.applyEffect(castInfos);
        }
        if (castInfos.duration >= 1) {
            for (Fighter Target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, castInfos.duration, 0);
                BuffSubPMEsquive buff = new BuffSubPMEsquive(subInfos, Target);
                buff.applyEffect(null, null);
                Target.getBuff().addBuff(buff);
            }
        } else {
            for (Fighter Target : castInfos.targets) {
                final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, (short) 0, 0, castInfos.effect, castInfos.caster, null, false, StatsEnum.NONE, 0, castInfos.spellLevel, 0, 0);

                final BuffSubPMEsquive buff = new BuffSubPMEsquive(subInfos, Target);
                buff.applyEffect(null, null);

                Target.getBuff().addBuff(buff);
            }
        }

        return -1;
    }

}
