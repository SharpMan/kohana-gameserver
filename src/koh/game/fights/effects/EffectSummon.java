package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.StaticSummonedFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.game.fights.fighters.SummonedReplacerFighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

import java.util.Arrays;

/**
 * Created by Melancholia on 1/24/16.
 */
public class EffectSummon extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        // Possibilité de spawn une creature sur la case ?
        if(castInfos.effectType == StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION){
            if(castInfos.targets.isEmpty())
                return -1;
            castInfos.targets.forEach(target -> target.tryDie(castInfos.caster.getID(),true));
            //Now mask
        }

        MonsterTemplate monster = DAO.getMonsters().find(castInfos.effect.diceNum);
        // getTemplate de monstre existante
        if (monster != null) {
            final MonsterGrade monsterLevel = monster.getLevelOrNear(castInfos.effect.diceSide);
            if (monsterLevel != null) {
                if (castInfos.caster.getFight().isCellWalkable(castInfos.cellId)) {
                    final Fighter summon;
                    if(monster.isStatic())
                        summon = new StaticSummonedFighter(castInfos.getFight(),castInfos.caster,monsterLevel);
                    else
                        summon = (castInfos.effectType != StatsEnum.KILL_TARGET_TO_REPLACE_INVOCATION) ?
                            new SummonedFighter(castInfos.caster.getFight(), monsterLevel,castInfos.caster)
                            : new SummonedReplacerFighter(castInfos.caster.getFight(), monsterLevel,castInfos.caster,castInfos.targets.get(0).asSummon().getGrade());


                    summon.joinFight();
                    summon.getFight().joinFightTeam(summon, castInfos.caster.getTeam(), false, castInfos.cellId, true);
                    castInfos.caster.getFight().sendToField(Pl -> new GameActionFightSummonMessage(ActionIdEnum.ACTION_SUMMON_CREATURE, castInfos.caster.getID(), (GameFightFighterInformations) summon.getGameContextActorInformations(Pl)));
                    castInfos.caster.getFight().getFightWorker().summonFighter(summon);
                    Arrays.stream(monster.getSpellsOnSummons())
                            .mapToObj(sp -> DAO.getSpells().findSpell(sp).getLevelOrNear(1))
                            .forEach(sp -> castInfos.getFight().launchSpell(summon, sp, castInfos.cellId, true, true, true));
                }
            }
        }

        return -1;
    }



}
