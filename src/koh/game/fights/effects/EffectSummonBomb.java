package koh.game.fights.effects;

import koh.game.dao.DAO;
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

        MonsterTemplate Monster = DAO.getMonsters().find(CastInfos.Effect.diceNum);
        // getTemplate de monstre existante
        if (Monster != null) {
            MonsterGrade MonsterLevel = Monster.getLevelOrNear(CastInfos.Effect.diceSide);
            if (MonsterLevel != null) {
                if (CastInfos.caster.fight.isCellWalkable(CastInfos.CellId)) {
                    BombFighter Bomb = new BombFighter(CastInfos.caster.fight, CastInfos.caster, MonsterLevel);
                    Bomb.JoinFight();
                    Bomb.fight.joinFightTeam(Bomb, CastInfos.caster.team, false, CastInfos.CellId, true);
                    CastInfos.caster.fight.sendToField(new GameActionFightSummonMessage(1008, CastInfos.caster.getID(), (GameFightFighterInformations) Bomb.getGameContextActorInformations(null)));
                    CastInfos.caster.fight.myWorker.summonFighter(Bomb);
                    CastInfos.caster.getActivableObjects().filter(Object -> Object instanceof FightBomb)
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
                    //CastInfos.caster.fight.affectSpellTo(CastInfos.caster, CastInfos.caster.fight.getCell(CastInfos.getCellId).GetObjectsAsFighter()[0] , CastInfos.Effect.diceSide, SpellDAOImpl.bombs.get(CastInfos.Effect.diceNum).instantSpellId);
                    CastInfos.caster.fight.launchSpell(CastInfos.caster, DAO.getSpells().findSpell(DAO.getSpells().findBomb(CastInfos.Effect.diceNum).instantSpellId).getSpellLevel(CastInfos.Effect.diceSide), (short) CastInfos.targetKnownCellId, true,true,true);
                }
            }
        }

        return -1;
    }

}
