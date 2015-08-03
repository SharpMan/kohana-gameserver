package koh.game.fights.effects;

import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.IllusionFighter;
import koh.game.fights.layer.FightTrap;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_MAKE_INVISIBLE;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightInvisibleDetectedMessage;
import koh.protocol.messages.game.context.fight.character.GameFightRefreshFighterMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectPerception extends EffectBase {

    //TODO Cache AffectedCell in CastInfos et nettoyer ce code
    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (short Cell : (new Zone(CastInfos.Effect.ZoneShape(), CastInfos.Effect.ZoneSize(), MapPoint.fromCellId(CastInfos.Caster.CellId()).advancedOrientationTo(MapPoint.fromCellId(CastInfos.CellId), true),CastInfos.Caster.Fight.Map)).GetCells(CastInfos.CellId)) {
            FightCell fightCell = CastInfos.Caster.Fight.GetCell(Cell);
            if (fightCell != null) {
                fightCell.GetObjects().stream().filter((fightObject) -> (fightObject.CellId() == Cell)).forEach((fightObject) -> {
                    if (fightObject.ObjectType() == FightObjectType.OBJECT_TRAP && ((FightTrap) fightObject).VisibileState == GameActionFightInvisibilityStateEnum.INVISIBLE && ((FightTrap) fightObject).m_caster.IsEnnemyWith(CastInfos.Caster)) {
                        ((FightTrap) fightObject).VisibileState = GameActionFightInvisibilityStateEnum.DETECTED;
                        ((FightTrap) fightObject).AppearForAll();
                    } else if (fightObject instanceof IllusionFighter) {
                        ((IllusionFighter) fightObject).TryDie(CastInfos.Caster.ID);
                    } else if (fightObject.ObjectType() == FightObjectType.OBJECT_FIGHTER) {
                        Fighter fighter = (Fighter) fightObject;
                        if (fighter.IsEnnemyWith(CastInfos.Caster)) {
                            if (fighter instanceof CharacterFighter && fighter.Team.GetAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.Summoner == fighter)) {
                                ((CharacterFighter) fighter).CleanClone();
                            } else if (fighter.VisibleState == GameActionFightInvisibilityStateEnum.INVISIBLE) {
                                fighter.VisibleState = GameActionFightInvisibilityStateEnum.DETECTED;
                                CastInfos.Caster.Fight.sendToField(new GameActionFightInvisibleDetectedMessage(ACTION_CHARACTER_MAKE_INVISIBLE, CastInfos.Caster.ID, fighter.ID, fighter.CellId()));
                                //CastInfos.Caster.Fight.sendToField(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, CastInfos.Caster.ID, fighter.ID, fighter.VisibleState.value));
                                CastInfos.Caster.Fight.sendToField(new GameFightRefreshFighterMessage(fighter.GetGameContextActorInformations(null)));
                            }
                            /*if(fighter.StateManager.HasState(FighterStateEnum.STATE_STEALTH))
                             {
                             fighter.BuffManager.RemoveStealth();
                             }*/
                        }
                    }
                });
            }
        }
        return -1;
    }

}
