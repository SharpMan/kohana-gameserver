/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.game.fights.effects.buff.BuffStatsByHit;
import koh.game.fights.fighters.IllusionFighter;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class EffectStats extends EffectBase {

    private static final int[] BLACKLISTED_EFFECTS = DAO.getSettings().getIntArray("Effect.BlacklistedByTriggers");

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            if(Target instanceof IllusionFighter){
                continue;//Roulette tue clone ...
            }
            EffectCast subInfos = new EffectCast(CastInfos.effectType, CastInfos.spellId, CastInfos.cellId, CastInfos.chance, CastInfos.effect, CastInfos.caster, CastInfos.targets,CastInfos.spellLevel);

            if(ArrayUtils.contains(BLACKLISTED_EFFECTS,CastInfos.effect.effectUid)){ //Feca special spell
                Target.getBuff().addBuff(new BuffStatsByHit(subInfos, Target));
                return -1;
            }

            BuffStats BuffStats = new BuffStats(subInfos, Target);
            if (BuffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(BuffStats);
        }

        return -1;
    }

}
