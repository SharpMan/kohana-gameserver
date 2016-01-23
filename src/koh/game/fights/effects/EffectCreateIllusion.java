package koh.game.fights.effects;

import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfunction;
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
    public int applyEffect(EffectCast castInfos) {
        int DistanceCharacterFromHidedPlace = Pathfunction.goalDistance(castInfos.caster.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);
        byte IgnoredDirection = Pathfunction.getDirection(castInfos.caster.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);
        short StartCell = castInfos.caster.getCellId();

        BuffState Buff = new BuffState(new EffectCast(StatsEnum.INVISIBILITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, null), castInfos.caster);
        Buff.duration = 1;
        Buff.decrementType = BuffDecrementType.TYPE_BEGINTURN;
        castInfos.caster.getBuff().addBuff(Buff);
        if (Buff.applyEffect(null, null) == -3) {
            return -3;
        }
        Buff.duration = -1;

        FightCell cell = castInfos.caster.getFight().getCell(castInfos.cellId);
        if (cell != null) {
            int Result = castInfos.caster.setCell(cell);
            ((CharacterFighter) castInfos.caster).fakeContextualId = castInfos.caster.getFight().getNextContextualId();

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
            FightCell Cell = castInfos.caster.getFight().getCell(Pathfunction.nextCell(StartCell, Direction, DistanceCharacterFromHidedPlace));
            if (Cell != null && Cell.canWalk()) {
                IllusionFighter Clone = new IllusionFighter(castInfos.caster.getFight(), castInfos.caster);
                Clone.getFight().joinFightTeam(Clone, castInfos.caster.getTeam(), false, Cell.Id, true);
                castInfos.caster.getFight().sendToField(new GameActionFightSummonMessage(1097, castInfos.caster.getID(), (GameFightFighterInformations) Clone.getGameContextActorInformations(null)));
                castInfos.caster.getFight().getFightWorker().summonFighter(Clone);
            }
        }
        castInfos.caster.getFight().observers.stream().forEach((o) -> {
            if (castInfos.caster.isMyFriend((Player) o)) {
                ((Player) o).send(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), castInfos.caster.getID(), castInfos.cellId));
            } else {
                ((Player) o).send(new GameFightShowFighterMessage(castInfos.caster.getGameContextActorInformations((Player) o)));
                ((Player) o).send(new GameActionFightSummonMessage(1097, castInfos.caster.getID(), (GameFightFighterInformations) castInfos.caster.getGameContextActorInformations((Player) o)));
            }
        });

        return -1;
    }

}
