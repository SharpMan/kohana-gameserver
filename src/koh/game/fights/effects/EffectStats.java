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
import koh.game.fights.effects.buff.BuffStatsByPortalTeleport;
import koh.game.fights.fighters.IllusionFighter;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class EffectStats extends EffectBase {

    private static final int[] BLACKLISTED_EFFECTS = DAO.getSettings().getIntArray("Effect.BlacklistedByTriggers");

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            if(target instanceof IllusionFighter){
                continue;//Roulette tue clone ...
            }
            if(target.getCarrierActorId() != 0){
                target = target.getCarrierActor();
            }
            final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            subInfos.glyphId = castInfos.glyphId;

            if(ArrayUtils.contains(BLACKLISTED_EFFECTS, castInfos.effect.effectUid)){ //Feca special spell
                target.getBuff().addBuff(new BuffStatsByHit(subInfos, target));
            }
            else if(castInfos.effect.effectId == 130941 //Focus eliatrope
                    || castInfos.effect.effectUid == 130945
                    || castInfos.effect.effectUid == 130949
                    || castInfos.effect.effectUid == 130953
                    || castInfos.effect.effectUid == 130957
                    || castInfos.effect.effectUid == 130961){
                target.getBuff().addBuff(new BuffStatsByPortalTeleport(subInfos, target));
            }
            else if (!target.getBuff().buffMaxStackReached(subInfos)) {

                final BuffStats buffStats = new BuffStats(subInfos, target);
                if (buffStats.applyEffect(null, null) == -3) {
                    return -3;
                }
                target.getBuff().addBuff(buffStats);

            }



        }

        return -1;
    }

}
