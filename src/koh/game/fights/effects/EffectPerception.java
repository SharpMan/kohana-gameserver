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

    //TODO dofusMaps AffectedCell in CastInfos et nettoyer ce code
    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (short Cell : (new Zone(CastInfos.Effect.ZoneShape(), CastInfos.Effect.ZoneSize(), MapPoint.fromCellId(CastInfos.Caster.getCellId()).advancedOrientationTo(MapPoint.fromCellId(CastInfos.CellId), true),CastInfos.Caster.fight.map)).getCells(CastInfos.CellId)) {
            FightCell fightCell = CastInfos.Caster.fight.getCell(Cell);
            if (fightCell != null) {
                fightCell.GetObjects().stream().filter((fightObject) -> (fightObject.getCellId() == Cell)).forEach((fightObject) -> {
                    if (fightObject.getObjectType() == FightObjectType.OBJECT_TRAP && ((FightTrap) fightObject).visibileState == GameActionFightInvisibilityStateEnum.INVISIBLE && ((FightTrap) fightObject).m_caster.isEnnemyWith(CastInfos.Caster)) {
                        ((FightTrap) fightObject).visibileState = GameActionFightInvisibilityStateEnum.DETECTED;
                        ((FightTrap) fightObject).AppearForAll();
                    } else if (fightObject instanceof IllusionFighter) {
                        ((IllusionFighter) fightObject).tryDie(CastInfos.Caster.ID);
                    } else if (fightObject.getObjectType() == FightObjectType.OBJECT_FIGHTER) {
                        Fighter fighter = (Fighter) fightObject;
                        if (fighter.isEnnemyWith(CastInfos.Caster)) {
                            if (fighter instanceof CharacterFighter && fighter.team.getAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.summoner == fighter)) {
                                ((CharacterFighter) fighter).CleanClone();
                            } else if (fighter.visibleState == GameActionFightInvisibilityStateEnum.INVISIBLE) {
                                fighter.visibleState = GameActionFightInvisibilityStateEnum.DETECTED;
                                CastInfos.Caster.fight.sendToField(new GameActionFightInvisibleDetectedMessage(ACTION_CHARACTER_MAKE_INVISIBLE, CastInfos.Caster.ID, fighter.ID, fighter.getCellId()));
                                //CastInfos.Caster.fight.sendToField(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, CastInfos.Caster.id, fighter.id, fighter.visibleState.value));
                                CastInfos.Caster.fight.sendToField(new GameFightRefreshFighterMessage(fighter.getGameContextActorInformations(null)));
                            }
                            /*if(fighter.StateManager.hasState(FighterStateEnum.STATE_STEALTH))
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
