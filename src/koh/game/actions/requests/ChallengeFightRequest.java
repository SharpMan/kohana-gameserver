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
        this.Requester.Send(new GameRolePlayPlayerFightFriendlyRequestedMessage(this.Requested.Character.ID, this.Requester.Character.ID, this.Requested.Character.ID));
        this.Requested.Send(new GameRolePlayPlayerFightFriendlyRequestedMessage(this.Requester.Character.ID, this.Requester.Character.ID, this.Requested.Character.ID));
    }

    @Override
    public boolean Accept() {
        if (!super.Declin()) {
            return false;
        }

        try {
            this.Requester.Send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.Requested.Character.ID, this.Requester.Character.ID, this.Requested.Character.ID, true));

            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);

            Fight Fight = new ChallengeFight(this.Requested.Character.CurrentMap, Requester, Requested);
            this.Requester.Character.CurrentMap.AddFight(Fight);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.Requester.SetBaseRequest(null);
            this.Requested.SetBaseRequest(null);
        }
        return true;
    }

    public boolean Cancel() {
        if (!super.Declin()) {
            return false;
        }

        try {//int fightId, int sourceId, int targetId, boolean accept
            this.Requested.Send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.Requester.Character.ID, this.Requester.Character.ID, this.Requested.Character.ID, false));

            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.Requester.SetBaseRequest(null);
            this.Requested.SetBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean Declin() {
        if (!super.Declin()) {
            return false;
        }

        try {//int fightId, int sourceId, int targetId, boolean accept
            this.Requester.Send(new GameRolePlayPlayerFightFriendlyAnsweredMessage(this.Requested.Character.ID, this.Requester.Character.ID, this.Requested.Character.ID, false));

            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.Requester.SetBaseRequest(null);
            this.Requested.SetBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum Action) {
        if (Action == GameActionTypeEnum.CHALLENGE_DENY || Action == GameActionTypeEnum.CHALLENGE_ACCEPT) {
            return true;
        }
        return false;
    }

}
