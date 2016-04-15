package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.layers.FightBomb;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class EffectSummonBomb extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        // Possibilité de spawn une creature sur la case ?

        final MonsterTemplate monster = DAO.getMonsters().find(castInfos.effect.diceNum);
        // getTemplate de monstre existante
        if (monster != null) {
            MonsterGrade MonsterLevel = monster.getLevelOrNear(castInfos.effect.diceSide);
            if (MonsterLevel != null) {
                if (castInfos.caster.getFight().isCellWalkable(castInfos.cellId)) {
                    final BombFighter bomb = new BombFighter(castInfos.caster.getFight(), castInfos.caster, MonsterLevel);
                    bomb.joinFight();
                    bomb.getFight().joinFightTeam(bomb, castInfos.caster.getTeam(), false, castInfos.cellId, true);
                    castInfos.caster.getFight().sendToField(new GameActionFightSummonMessage(1008, castInfos.caster.getID(), (GameFightFighterInformations) bomb.getGameContextActorInformations(null)));
                    castInfos.caster.getFight().getFightWorker().summonFighter(bomb);
                    castInfos.caster.getActivableObjects().filter(Object -> Object instanceof FightBomb)
                            .filter(Bombe -> ArrayUtils.contains(((FightBomb)Bombe).owner,bomb))
                            .forEach(Bombe -> ((FightBomb)Bombe).getFightCells()
                                              .filter(Cell -> Cell.hasFighter())
                                              .forEach(C -> {
                                                  for(Fighter F : C.getObjectsAsFighter()){
                                                      ((FightBomb)Bombe).loadTargets(F);
                                                      ((FightBomb)Bombe).activate(F, BuffActiveType.ACTIVE_ENDMOVE);
                                                  }
                                              }));
                } else {
                    //castInfos.caster.fight.affectSpellTo(castInfos.caster, castInfos.caster.fight.getCell(castInfos.getCellId).getObjectsAsFighter()[0] , castInfos.effect.diceSide, SpellDAOImpl.bombs.get(castInfos.effect.diceNum).instantSpellId);
                    castInfos.caster.getFight().launchSpell(castInfos.caster, DAO.getSpells().findSpell(DAO.getSpells().findBomb(castInfos.effect.diceNum).instantSpellId).getSpellLevel(castInfos.effect.diceSide), (short) castInfos.targetKnownCellId, true,true,true,-1);
                }
            }
        }

        return -1;
    }

}
