package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.fights.Fight;
import koh.game.fights.types.ChallengeFight;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayPlayerFightFriendlyAnsweredMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayPlayerFightFriendlyRequestedMessage;

/**
 *
 * @author Neo-Craft
 */
public class ChallengeFightRequest extends GameBaseRequest {

    public ChallengeFightRequest(WorldClient Client, WorldClient Target) {
        super(Client, Target);
        this.requester.send(new GameRolePlayPlayerFightFriendlyRequestedMessage(this.requested.character.getID(), this.requester.character.getID(), this.requested.character.getID()));
        this.requested.send(new GameRolePlayPlayerFightFriendlyRequestedMessage(this.requester.character.getID(), this.requester.character.getID(), this.requested.character.getID()));
    }

    @Override
    public boolean accept() {
        if (!super.declin()) {
            return false;
        }

        try {
            this.requester.send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.requested.character.getID(), this.requester.character.getID(), this.requested.character.getID(), true));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);

            Fight Fight = new ChallengeFight(this.requested.getCharacter().getCurrentMap(), requester, requested);
            this.requester.character.getCurrentMap().addFight(Fight);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.requester.setBaseRequest(null);
            this.requested.setBaseRequest(null);
        }
        return true;
    }

    public boolean Cancel() {
        if (!super.declin()) {
            return false;
        }

        try {//int fightId, int sourceId, int targetId, boolean accept
            this.requested.send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.requester.character.getID(), this.requester.character.getID(), this.requested.character.getID(), false));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.requester.setBaseRequest(null);
            this.requested.setBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean declin() {
        if (!super.declin()) {
            return false;
        }

        try {//int fightId, int sourceId, int targetId, boolean accept
            this.requester.send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.requested.character.getID(), this.requester.character.getID(), this.requested.character.getID(), false));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.requester.setBaseRequest(null);
            this.requested.setBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum action) {
        if (action == GameActionTypeEnum.CHALLENGE_DENY || action == GameActionTypeEnum.CHALLENGE_ACCEPT) {
            return true;
        }
        return false;
    }

}
