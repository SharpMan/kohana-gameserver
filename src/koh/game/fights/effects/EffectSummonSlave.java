package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.*;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

/**
 * Created by Melancholia on 6/20/16.
 */
public class EffectSummonSlave extends EffectBase {
    @Override
    public int applyEffect(EffectCast castInfos) {
        if(castInfos.effectType == StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION_SLAVE){
            if(castInfos.targets.isEmpty() || castInfos.targets.stream().noneMatch(bf -> bf instanceof SummonedFighter || bf instanceof StaticFighter))
                return -1;
            castInfos.targets.forEach(target -> target.tryDie(castInfos.caster.getID(),true));
            //Now mask
        }
        if(castInfos.caster.getStats().getTotal(StatsEnum.ADD_SUMMON_LIMIT) <= 0){
            return -1;
        }
//3120
        if (castInfos.caster.getFight().isCellWalkable(castInfos.cellId)) {
            final MonsterTemplate monster = DAO.getMonsters().find(castInfos.effect.diceNum);
            final MonsterGrade monsterLevel = monster.getLevelOrNear(castInfos.effect.diceSide);
            final Fighter summon = castInfos.effectType != StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION_SLAVE ?
                    new SlaveFighter(castInfos.caster.getFight(), monsterLevel,castInfos.caster)
                    : new SlaveReplacerFighter(castInfos.caster.getFight(), monsterLevel,castInfos.caster,castInfos.targets.stream().filter(bf -> bf instanceof SummonedFighter).findFirst().get().asSummon().getGrade());
            summon.getStates().fakeState(FightStateEnum.ENRACINÉ,true);
            if(castInfos.effectType == StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION_SLAVE){
                //System.out.println(castInfos.targets.get(0).getBuff().delayedEffects.size());
                castInfos.targets.get(0).getBuff().delayedEffects.forEach(i -> i.second = 1);
                summon.getBuff().delayedEffects.addAll(castInfos.targets.get(0).getBuff().delayedEffects);
                summon.getBuff().delayedEffects.forEach(e -> e.first.targets.add(summon));
            }

            summon.joinFight();
            summon.getFight().joinFightTeam(summon, castInfos.caster.getTeam(), false, castInfos.cellId, true);
            castInfos.caster.getFight().sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_SUMMON_SLAVE, castInfos.caster.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
            castInfos.caster.getFight().getFightWorker().summonFighter(summon);

            castInfos.caster.getStats().getEffect(StatsEnum.ADD_SUMMON_LIMIT).base--;
            if (castInfos.caster instanceof CharacterFighter)
                castInfos.caster.send(castInfos.caster.asPlayer().getCharacterStatsListMessagePacket());
        }


        return -1;
    }
}
