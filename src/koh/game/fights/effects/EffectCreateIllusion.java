package koh.game.fights.effects;

import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfinder;
import koh.game.fights.FightCell;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.IllusionFighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.client.enums.DirectionsEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSummonMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.messages.game.context.fight.character.GameFightShowFighterMessage;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;

/**
 *
 * @author Neo-Craft
 */
public class EffectCreateIllusion extends EffectBase {

    public static final byte[] TrueDirection = new byte[]{DirectionsEnum.DOWN_RIGHT, DirectionsEnum.DOWN_LEFT, DirectionsEnum.UP_LEFT, DirectionsEnum.UP_RIGHT};

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        int DistanceCharacterFromHidedPlace = Pathfinder.getGoalDistance(CastInfos.Caster.Fight.Map, CastInfos.Caster.CellId(), CastInfos.CellId);
        byte IgnoredDirection = Pathfinder.getDirection(CastInfos.Caster.Fight.Map, CastInfos.Caster.CellId(), CastInfos.CellId);
        short StartCell = CastInfos.Caster.CellId();

        BuffState Buff = new BuffState(new EffectCast(StatsEnum.Invisibility, CastInfos.SpellId, CastInfos.CellId, CastInfos.Chance, null, CastInfos.Caster, null), CastInfos.Caster);
        Buff.Duration = 1;
        Buff.DecrementType = BuffDecrementType.TYPE_BEGINTURN;
        CastInfos.Caster.Buffs.AddBuff(Buff);
        if (Buff.ApplyEffect(null, null) == -3) {
            return -3;
        }
        Buff.Duration = -1;

        FightCell cell = CastInfos.Caster.Fight.GetCell(CastInfos.CellId);
        if (cell != null) {
            int Result = CastInfos.Caster.SetCell(cell);
            ((CharacterFighter) CastInfos.Caster).fakeContextualId = CastInfos.Caster.Fight.GetNextContextualId();

            if (Result != -1) {
                return Result;
            }
        } else {
            return -1;
        }

        for (byte Direction : TrueDirection) {
            if (IgnoredDirection == Direction) {
                continue;
            }
            FightCell Cell = CastInfos.Caster.Fight.GetCell(Pathfinder.nextCell(StartCell, Direction, DistanceCharacterFromHidedPlace));
            if (Cell != null && Cell.CanWalk()) {
                IllusionFighter Clone = new IllusionFighter(CastInfos.Caster.Fight, CastInfos.Caster);
                Clone.Fight.JoinFightTeam(Clone, CastInfos.Caster.Team, false, Cell.Id, true);
                CastInfos.Caster.Fight.sendToField(new GameActionFightSummonMessage(1097, CastInfos.Caster.ID, (GameFightFighterInformations) Clone.getGameContextActorInformations(null)));
                CastInfos.Caster.Fight.myWorker.SummonFighter(Clone);
            }
        }
        CastInfos.Caster.Fight.observers.stream().forEach((o) -> {
            if (CastInfos.Caster.IsMyFriend((Player) o)) {
                ((Player) o).send(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.Caster.ID, CastInfos.Caster.ID, CastInfos.CellId));
            } else {
                ((Player) o).send(new GameFightShowFighterMessage(CastInfos.Caster.getGameContextActorInformations((Player) o)));
                ((Player) o).send(new GameActionFightSummonMessage(1097, CastInfos.Caster.ID, (GameFightFighterInformations) CastInfos.Caster.getGameContextActorInformations((Player) o)));
            }
        });

        return -1;
    }

}
