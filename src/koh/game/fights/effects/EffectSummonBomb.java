package koh.game.fights.effects;

import koh.game.dao.mysql.MonsterDAOImpl;
import koh.game.dao.mysql.SpellDAOImpl;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.layer.FightBomb;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class EffectSummonBomb extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Possibilité de spawn une creature sur la case ?

        MonsterTemplate Monster = MonsterDAOImpl.Cache.get(CastInfos.Effect.diceNum);
        // getTemplate de monstre existante
        if (Monster != null) {
            MonsterGrade MonsterLevel = Monster.getLevelOrNear(CastInfos.Effect.diceSide);
            if (MonsterLevel != null) {
                if (CastInfos.Caster.fight.isCellWalkable(CastInfos.CellId)) {
                    BombFighter Bomb = new BombFighter(CastInfos.Caster.fight, CastInfos.Caster, MonsterLevel);
                    Bomb.JoinFight();
                    Bomb.fight.joinFightTeam(Bomb, CastInfos.Caster.team, false, CastInfos.CellId, true);
                    CastInfos.Caster.fight.sendToField(new GameActionFightSummonMessage(1008, CastInfos.Caster.ID, (GameFightFighterInformations) Bomb.getGameContextActorInformations(null)));
                    CastInfos.Caster.fight.myWorker.summonFighter(Bomb);
                    CastInfos.Caster.getActivableObjects().filter(Object -> Object instanceof FightBomb)
                            .filter(Bombe -> ArrayUtils.contains(((FightBomb)Bombe).Owner,Bomb))
                            .forEach(Bombe -> ((FightBomb)Bombe).FightCells()
                                              .filter(Cell -> Cell.hasFighter())
                                              .forEach(C -> {{
                                                  for(Fighter F : C.GetObjectsAsFighter()){
                                                      ((FightBomb)Bombe).loadTargets(F);
                                                      ((FightBomb)Bombe).activate(F);
                                                  }
                                              }}));
                } else {
                    //CastInfos.Caster.fight.affectSpellTo(CastInfos.Caster, CastInfos.Caster.fight.getCell(CastInfos.getCellId).GetObjectsAsFighter()[0] , CastInfos.Effect.diceSide, SpellDAOImpl.bombs.get(CastInfos.Effect.diceNum).instantSpellId);
                    CastInfos.Caster.fight.launchSpell(CastInfos.Caster, SpellDAOImpl.spells.get(SpellDAOImpl.bombs.get(CastInfos.Effect.diceNum).instantSpellId).getSpellLevel(CastInfos.Effect.diceSide), (short) CastInfos.targetKnownCellId, true,true,true);
                }
            }
        }

        return -1;
    }

}
