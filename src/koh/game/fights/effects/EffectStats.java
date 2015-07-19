/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.game.fights.fighters.IllusionFighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectStats extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if(Target instanceof IllusionFighter){
                continue;//Roulette tue clone ...
            }
            EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, CastInfos.CellId, CastInfos.Chance, CastInfos.Effect, CastInfos.Caster, CastInfos.Targets,CastInfos.SpellLevel);
            BuffStats BuffStats = new BuffStats(SubInfos, Target);
            if (BuffStats.ApplyEffect(null, null) == -3) {
                return -3;
            }

            Target.Buffs.AddBuff(BuffStats);
        }

        return -1;
    }

}
