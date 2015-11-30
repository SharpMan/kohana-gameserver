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
            MonsterGrade MonsterLevel = Monster.GetLevelOrNear(CastInfos.Effect.diceSide);
            if (MonsterLevel != null) {
                if (CastInfos.Caster.Fight.IsCellWalkable(CastInfos.CellId)) {
                    BombFighter Bomb = new BombFighter(CastInfos.Caster.Fight, CastInfos.Caster, MonsterLevel);
                    Bomb.JoinFight();
                    Bomb.Fight.JoinFightTeam(Bomb, CastInfos.Caster.Team, false, CastInfos.CellId, true);
                    CastInfos.Caster.Fight.sendToField(new GameActionFightSummonMessage(1008, CastInfos.Caster.ID, (GameFightFighterInformations) Bomb.getGameContextActorInformations(null)));
                    CastInfos.Caster.Fight.myWorker.SummonFighter(Bomb);
                    CastInfos.Caster.GetActivableObjects().filter(Object -> Object instanceof FightBomb)
                            .filter(Bombe -> ArrayUtils.contains(((FightBomb)Bombe).Owner,Bomb))
                            .forEach(Bombe -> ((FightBomb)Bombe).FightCells()
                                              .filter(Cell -> Cell.hasFighter())
                                              .forEach(C -> {{
                                                  for(Fighter F : C.GetObjectsAsFighter()){
                                                      ((FightBomb)Bombe).LoadTargets(F);
                                                      ((FightBomb)Bombe).Activate(F);
                                                  }
                                              }}));
                } else {
                    //CastInfos.Caster.fight.AffectSpellTo(CastInfos.Caster, CastInfos.Caster.fight.GetCell(CastInfos.CellId).GetObjectsAsFighter()[0] , CastInfos.Effect.diceSide, SpellDAOImpl.bombs.get(CastInfos.Effect.diceNum).instantSpellId);
                    CastInfos.Caster.Fight.LaunchSpell(CastInfos.Caster, SpellDAOImpl.spells.get(SpellDAOImpl.bombs.get(CastInfos.Effect.diceNum).instantSpellId).SpellLevel(CastInfos.Effect.diceSide), (short) CastInfos.targetKnownCellId, true,true,true);
                }
            }
        }

        return -1;
    }

}
