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
    public int applyEffect(EffectCast CastInfos) {
        int DistanceCharacterFromHidedPlace = Pathfinder.getGoalDistance(CastInfos.caster.getFight().getMap(), CastInfos.caster.getCellId(), CastInfos.cellId);
        byte IgnoredDirection = Pathfinder.getDirection(CastInfos.caster.getFight().getMap(), CastInfos.caster.getCellId(), CastInfos.cellId);
        short StartCell = CastInfos.caster.getCellId();

        BuffState Buff = new BuffState(new EffectCast(StatsEnum.INVISIBILITY, CastInfos.spellId, CastInfos.cellId, CastInfos.chance, null, CastInfos.caster, null), CastInfos.caster);
        Buff.duration = 1;
        Buff.DecrementType = BuffDecrementType.TYPE_BEGINTURN;
        CastInfos.caster.getBuff().addBuff(Buff);
        if (Buff.applyEffect(null, null) == -3) {
            return -3;
        }
        Buff.duration = -1;

        FightCell cell = CastInfos.caster.getFight().getCell(CastInfos.cellId);
        if (cell != null) {
            int Result = CastInfos.caster.setCell(cell);
            ((CharacterFighter) CastInfos.caster).fakeContextualId = CastInfos.caster.getFight().getNextContextualId();

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
            FightCell Cell = CastInfos.caster.getFight().getCell(Pathfinder.nextCell(StartCell, Direction, DistanceCharacterFromHidedPlace));
            if (Cell != null && Cell.canWalk()) {
                IllusionFighter Clone = new IllusionFighter(CastInfos.caster.getFight(), CastInfos.caster);
                Clone.getFight().joinFightTeam(Clone, CastInfos.caster.getTeam(), false, Cell.Id, true);
                CastInfos.caster.getFight().sendToField(new GameActionFightSummonMessage(1097, CastInfos.caster.getID(), (GameFightFighterInformations) Clone.getGameContextActorInformations(null)));
                CastInfos.caster.getFight().getFightWorker().summonFighter(Clone);
            }
        }
        CastInfos.caster.getFight().observers.stream().forEach((o) -> {
            if (CastInfos.caster.isMyFriend((Player) o)) {
                ((Player) o).send(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.caster.getID(), CastInfos.caster.getID(), CastInfos.cellId));
            } else {
                ((Player) o).send(new GameFightShowFighterMessage(CastInfos.caster.getGameContextActorInformations((Player) o)));
                ((Player) o).send(new GameActionFightSummonMessage(1097, CastInfos.caster.getID(), (GameFightFighterInformations) CastInfos.caster.getGameContextActorInformations((Player) o)));
            }
        });

        return -1;
    }

}
