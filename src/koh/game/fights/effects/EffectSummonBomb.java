package koh.game.fights.effects;

import koh.game.dao.MonsterDAO;
import koh.game.dao.SpellDAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.fighters.BombFighter;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

/**
 *
 * @author Neo-Craft
 */
public class EffectSummonBomb extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Possibilité de spawn une creature sur la case ?

        MonsterTemplate Monster = MonsterDAO.Cache.get(CastInfos.Effect.diceNum);
        // Template de monstre existante
        if (Monster != null) {
            MonsterGrade MonsterLevel = Monster.GetLevelOrNear(CastInfos.Effect.diceSide);
            if (MonsterLevel != null) {
                if (CastInfos.Caster.Fight.IsCellWalkable(CastInfos.CellId)) {
                    BombFighter Bomb = new BombFighter(CastInfos.Caster.Fight, CastInfos.Caster, MonsterLevel);
                    Bomb.JoinFight();
                    Bomb.Fight.JoinFightTeam(Bomb, CastInfos.Caster.Team, false, CastInfos.CellId, true);
                    CastInfos.Caster.Fight.sendToField(new GameActionFightSummonMessage(1008, CastInfos.Caster.ID, (GameFightFighterInformations) Bomb.GetGameContextActorInformations(null)));
                    CastInfos.Caster.Fight.myWorker.SummonFighter(Bomb);
                } else {
                    //CastInfos.Caster.Fight.AffectSpellTo(CastInfos.Caster, CastInfos.Caster.Fight.GetCell(CastInfos.CellId).GetObjectsAsFighter()[0] , CastInfos.Effect.diceSide, SpellDAO.Bombs.get(CastInfos.Effect.diceNum).instantSpellId);
                    CastInfos.Caster.Fight.LaunchSpell(CastInfos.Caster, SpellDAO.Spells.get(SpellDAO.Bombs.get(CastInfos.Effect.diceNum).instantSpellId).SpellLevel(CastInfos.Effect.diceSide), (short) CastInfos.targetKnownCellId, true,true,true);
                }
            }
        }

        return -1;
    }

}
