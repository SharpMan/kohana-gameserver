package koh.game.network.handlers.game.context;

import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.ChallengeFightRequest;
import koh.game.entities.actors.Player;
import koh.game.fights.Fight;
import koh.game.fights.Fight.FightLoopState;
import koh.game.fights.FightState;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.SwapPositionRequest;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.types.AgressionFight;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import static koh.protocol.client.enums.AggressableStatusEnum.NON_AGGRESSABLE;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.FightOptionsEnum;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.GameContextKickMessage;
import koh.protocol.messages.game.context.GameContextQuitMessage;
import koh.protocol.messages.game.context.fight.GameFightJoinRequestMessage;
import koh.protocol.messages.game.context.fight.GameFightOptionToggleMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementPositionRequestMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsAcceptMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsCancelMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsCancelledMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsOfferMessage;
import koh.protocol.messages.game.context.fight.GameFightPlacementSwapPositionsRequestMessage;
import koh.protocol.messages.game.context.fight.GameFightReadyMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnFinishMessage;
import koh.protocol.messages.game.context.fight.GameFightTurnReadyMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayPlayerFightFriendlyAnswerMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayPlayerFightRequestMessage;
import koh.protocol.messages.game.guild.ChallengeFightJoinRefusedMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightHandler {

    @HandlerAttribute(ID = GameFightOptionToggleMessage.M_ID)
    public static void HandleGameFightOptionToggleMessage(WorldClient Client, GameFightOptionToggleMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFighter() != Client.getCharacter().getFighter().getTeam().leader) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.getCharacter().getFight().toggleLock(Client.getCharacter().getFighter(), FightOptionsEnum.valueOf(Message.option));
    }

    @HandlerAttribute(ID = 718)
    public static void HandleGameFightTurnFinishMessage(WorldClient Client, GameFightTurnFinishMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFight().getFightState() != FightState.STATE_ACTIVE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFight().getCurrentFighter() == Client.getCharacter().getFighter()) {
            Client.getCharacter().getFight().setFightLoopState(FightLoopState.STATE_END_TURN);
        }
    }

    @HandlerAttribute(ID = GameFightTurnReadyMessage.M_ID)
    public static void HandleGameFightTurnReadyMessage(WorldClient Client, GameFightTurnReadyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFight().getFightState() != FightState.STATE_ACTIVE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFighter() == null) {
            return;
        }

        Client.getCharacter().getFighter().turnReady = true;
    }

    @HandlerAttribute(ID = 255)
    public static void HandleGameContextQuitMessage(WorldClient Client, GameContextQuitMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.getCharacter().getFight().leaveFight(Client.getCharacter().getFighter());
        } else {

        }
    }

    @HandlerAttribute(ID = 708)
    public static void HandleGameFightReadyMessage(WorldClient Client, GameFightReadyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.getCharacter().getFight().SetFighterReady(Client.getCharacter().getFighter());
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsRequestMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsRequestMessage(WorldClient Client, GameFightPlacementSwapPositionsRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || Client.getCharacter().getFighter().getID() == Message.requestedId) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Fighter Fighter = Client.getCharacter().getFight().getFighter(Message.requestedId);
        if (Fighter == null || Fighter == Client.getCharacter().getFighter()) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (Client.getCharacter().getFighter().getTeam().leader != Client.getCharacter().getFighter()) {  //Demande
            synchronized (Client.getCharacter().getFighter().getTeam().swapRequests) {
                SwapPositionRequest Request = new SwapPositionRequest(Fighter.getTeam().getNextRequestId(), Client.getCharacter().getID(), Client.getCharacter().getFighter().getCellId(), Fighter.getID(), Fighter.getCellId());
                Client.getCharacter().getFighter().getTeam().swapRequests.add(Request);
                Client.getCharacter().getFighter().getTeam().sendToField(new GameFightPlacementSwapPositionsOfferMessage(Request.requestId, Request.requesterId, Request.requesterCellId, Request.requestedId, Request.requestedCellId));
            }
        } else {
            Client.getCharacter().getFight().swapPosition(Client.getCharacter().getFighter(), Fighter);
        }
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsCancelMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsCancelMessage(WorldClient Client, GameFightPlacementSwapPositionsCancelMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || Client.getCharacter().getFighter().getTeam().getRequest(Message.requestId) == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.getCharacter().getFighter().getTeam().sendToField(new GameFightPlacementSwapPositionsCancelledMessage(Client.getCharacter().getFighter().getTeam().getRequest(Message.requestId).requesterId, Client.getCharacter().getID()));
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsAcceptMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsAcceptMessage(WorldClient Client, GameFightPlacementSwapPositionsAcceptMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || Client.getCharacter().getFighter().getTeam().getRequest(Message.requestId) == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        //TODO: +=Event onLeftFight remove all joined requests + delete this part of code
        Fighter Target = null;
        if ((Target = Client.getCharacter().getFight().getFighter(Client.getCharacter().getFighter().getTeam().getRequest(Message.requestId).requesterId)) != null) {
            Client.getCharacter().getFight().swapPosition(Client.getCharacter().getFighter(), Target);
        }
    }

    @HandlerAttribute(ID = 6081)
    public static void HandleGameContextKickMessage(WorldClient Client, GameContextKickMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            if (Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
                Client.send(new BasicNoOperationMessage());
            } else if (Client.getCharacter().getFighter().getTeam().leader != Client.getCharacter().getFighter()) {
                Client.send(new BasicNoOperationMessage());
            } else {
                Fighter Fighter = Client.getCharacter().getFight().getFighter(Message.targetId);

                if (Fighter == null || Fighter == Client.getCharacter().getFighter()) {
                    Client.send(new BasicNoOperationMessage());
                } else if (Fighter.getTeam() != Client.getCharacter().getFighter().getTeam()) {
                    Client.send(new BasicNoOperationMessage());
                } else {
                    Client.getCharacter().getFight().leaveFight(Fighter);
                }
            }
        }

    }

    @HandlerAttribute(ID = GameFightJoinRequestMessage.M_ID)
    public static void HandleGameFightJoinRequestMessage(WorldClient Client, GameFightJoinRequestMessage Message) {
        Fight Fight = Client.getCharacter().getCurrentMap().getFight(Message.fightId);
        if (Fight == null) {
            Client.send(new BasicNoOperationMessage());
        } else if (Fight.getFightState() != FightState.STATE_PLACE) {
            Client.send(new BasicNoOperationMessage());
        } else {
            FightTeam Team = Fight.getTeam(Message.fighterId);
            if (Team == null) { //Ne doit pas arriver
                Client.send(new ChallengeFightJoinRefusedMessage(Client.getCharacter().getID(), FighterRefusedReasonEnum.JUST_RESPAWNED));
                return;
            }
            FighterRefusedReasonEnum Answer = Fight.canJoin(Team, Client.getCharacter());
            if (Answer == FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
                Fighter Fighter = new CharacterFighter(Fight, Client);

                GameAction FightAction = new GameFight(Fighter, Fight);

                Client.addGameAction(FightAction);

                Fight.joinFightTeam(Fighter, Team, false, (short) -1, true);
            } else {
                Client.send(new ChallengeFightJoinRefusedMessage(Client.getCharacter().getID(), Answer));
            }
        }
    }

    @HandlerAttribute(ID = GameFightPlacementPositionRequestMessage.M_ID)
    public static void HandleGameFightPlacementPositionRequestMessage(WorldClient Client, GameFightPlacementPositionRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || Client.getCharacter().getFighter().getCellId() == Message.cellId) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.getCharacter().getFight().setFighterPlace(Client.getCharacter().getFighter(), (short) Message.cellId);
    }

    @HandlerAttribute(ID = 5732)
    public static void HandleGameRolePlayPlayerFightFriendlyAnswerMessage(WorldClient Client, GameRolePlayPlayerFightFriendlyAnswerMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        ChallengeFightRequest InvitationRequest = (ChallengeFightRequest) Client.getBaseRequest();
        if (Message.accept) {
            InvitationRequest.accept();
        } else if (Client == InvitationRequest.requested) {
            InvitationRequest.declin();
        } else {
            InvitationRequest.Cancel();
        }

    }

    @HandlerAttribute(ID = GameRolePlayPlayerFightRequestMessage.M_ID)
    public static void HandleGameRolePlayPlayerFightRequestMessage(WorldClient client, GameRolePlayPlayerFightRequestMessage Message) {
        Player target = client.getCharacter().getCurrentMap().getPlayer(Message.targetId);
        if (target == null) {
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
        } else if (target == client.getCharacter()) {
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.FIGHT_MYSELF));
        } else if (target.getCurrentMap().getId() != client.getCharacter().getCurrentMap().getId()) {
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.WRONG_MAP));
        } else if (Message.friendly) {
            if (!target.getClient().canGameAction(GameActionTypeEnum.BASIC_REQUEST) || !target.isInWorld()) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
            } else if (!client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.IM_OCCUPIED));
            } else {
                ChallengeFightRequest Request = new ChallengeFightRequest(client, target.getClient());
                GameRequest RequestAction = new GameRequest(client.getCharacter(), Request);

                client.addGameAction(RequestAction);
                target.getClient().addGameAction(RequestAction);

                client.setBaseRequest(Request);
                target.getClient().setBaseRequest(Request);

            }
        } else {
            if (target.getPvPEnabled() == NON_AGGRESSABLE || client.getCharacter().getPvPEnabled() == NON_AGGRESSABLE || client.getCharacter().getAlignmentSide() == target.getAlignmentSide()) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else if (!target.isInWorld() || target.getClient().isGameAction(GameActionTypeEnum.FIGHT)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_OCCUPIED));
            } else if (!client.getCharacter().isInWorld() || client.isGameAction(GameActionTypeEnum.FIGHT)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.IM_OCCUPIED));
            } else if (client.getCharacter().getAlignmentSide() == AlignmentSideEnum.ALIGNMENT_NEUTRAL || target.getAlignmentSide() == AlignmentSideEnum.ALIGNMENT_NEUTRAL) {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else {
                target.getClient().abortGameActions();
                Fight Fight = new AgressionFight(client.getCharacter().getCurrentMap(), client, target.getClient());
                target.getCurrentMap().addFight(Fight);
            }
        }
    }

}
