package koh.game.fights.effects;

import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfunction;
import koh.game.fights.FightCell;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.buffState;
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

    public static final byte[] TRUE_DIRECTION = new byte[]{DirectionsEnum.DOWN_RIGHT, DirectionsEnum.DOWN_LEFT, DirectionsEnum.UP_LEFT, DirectionsEnum.UP_RIGHT};

    @Override
    public int applyEffect(EffectCast castInfos) {
        final int distanceCharacterFromHidedPlace = Pathfunction.goalDistance(castInfos.caster.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);
        final byte ignoredDirection = Pathfunction.getDirection(castInfos.caster.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);
        final short startCell = castInfos.caster.getCellId();

        final buffState buff = new buffState(new EffectCast(StatsEnum.INVISIBILITY, castInfos.spellId, castInfos.cellId, castInfos.chance, null, castInfos.caster, null), castInfos.caster);
        buff.duration = 1;
        buff.decrementType = BuffDecrementType.TYPE_BEGINTURN;
        castInfos.caster.getBuff().addBuff(buff);
        if (buff.applyEffect(null, null) == -3) {
            return -3;
        }
        buff.duration = -1;

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

        for (byte Direction : TRUE_DIRECTION) {
            if (ignoredDirection == Direction) {
                continue;
            }
            final FightCell Cell = castInfos.caster.getFight().getCell(Pathfunction.nextCell(startCell, Direction, distanceCharacterFromHidedPlace));
            if (Cell != null && Cell.canWalk()) {
                final IllusionFighter clone = new IllusionFighter(castInfos.caster.getFight(), castInfos.caster);
                clone.getFight().joinFightTeam(clone, castInfos.caster.getTeam(), false, Cell.Id, true);
                castInfos.caster.getFight().sendToField(new GameActionFightSummonMessage(1097, castInfos.caster.getID(), (GameFightFighterInformations) clone.getGameContextActorInformations(null)));
                castInfos.caster.getFight().getFightWorker().summonFighter(clone);
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
