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

        if (Client.character.getFighter() != Client.character.getFighter().team.Leader) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.character.getFight().toggleLock(Client.character.getFighter(), FightOptionsEnum.valueOf(Message.option));
    }

    @HandlerAttribute(ID = 718)
    public static void HandleGameFightTurnFinishMessage(WorldClient Client, GameFightTurnFinishMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.character.getFight().fightState != FightState.STATE_ACTIVE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.character.getFight().currentFighter == Client.character.getFighter()) {
            Client.character.getFight().fightLoopState = FightLoopState.STATE_END_TURN;
        }
    }

    @HandlerAttribute(ID = GameFightTurnReadyMessage.M_ID)
    public static void HandleGameFightTurnReadyMessage(WorldClient Client, GameFightTurnReadyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.character.getFight().fightState != FightState.STATE_ACTIVE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.character.getFighter() == null) {
            return;
        }

        Client.character.getFighter().turnReady = true;
    }

    @HandlerAttribute(ID = 255)
    public static void HandleGameContextQuitMessage(WorldClient Client, GameContextQuitMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.character.getFight().leaveFight(Client.character.getFighter());
        } else {

        }
    }

    @HandlerAttribute(ID = 708)
    public static void HandleGameFightReadyMessage(WorldClient Client, GameFightReadyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.character.getFight().fightState != FightState.STATE_PLACE) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.character.getFight().SetFighterReady(Client.character.getFighter());
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsRequestMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsRequestMessage(WorldClient Client, GameFightPlacementSwapPositionsRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.character.getFight().fightState != FightState.STATE_PLACE || Client.character.getFighter().ID == Message.requestedId) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Fighter Fighter = Client.character.getFight().getFighter(Message.requestedId);
        if (Fighter == null || Fighter == Client.character.getFighter()) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (Client.character.getFighter().team.Leader != Client.character.getFighter()) {  //Demande
            synchronized (Client.character.getFighter().team.swapRequests) {
                SwapPositionRequest Request = new SwapPositionRequest(Fighter.team.getNextRequestId(), Client.character.ID, Client.character.getFighter().getCellId(), Fighter.ID, Fighter.getCellId());
                Client.character.getFighter().team.swapRequests.add(Request);
                Client.character.getFighter().team.sendToField(new GameFightPlacementSwapPositionsOfferMessage(Request.requestId, Request.requesterId, Request.requesterCellId, Request.requestedId, Request.requestedCellId));
            }
        } else {
            Client.character.getFight().swapPosition(Client.character.getFighter(), Fighter);
        }
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsCancelMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsCancelMessage(WorldClient Client, GameFightPlacementSwapPositionsCancelMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.character.getFight().fightState != FightState.STATE_PLACE || Client.character.getFighter().team.getRequest(Message.requestId) == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        Client.character.getFighter().team.sendToField(new GameFightPlacementSwapPositionsCancelledMessage(Client.character.getFighter().team.getRequest(Message.requestId).requesterId, Client.character.ID));
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsAcceptMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsAcceptMessage(WorldClient Client, GameFightPlacementSwapPositionsAcceptMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.character.getFight().fightState != FightState.STATE_PLACE || Client.character.getFighter().team.getRequest(Message.requestId) == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        //TODO: +=Event onLeftFight remove all joined requests + delete this part of code
        Fighter Target = null;
        if ((Target = Client.character.getFight().getFighter(Client.character.getFighter().team.getRequest(Message.requestId).requesterId)) != null) {
            Client.character.getFight().swapPosition(Client.character.getFighter(), Target);
        }
    }

    @HandlerAttribute(ID = 6081)
    public static void HandleGameContextKickMessage(WorldClient Client, GameContextKickMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.FIGHT)) {
            if (Client.character.getFight().fightState != FightState.STATE_PLACE) {
                Client.send(new BasicNoOperationMessage());
            } else if (Client.character.getFighter().team.Leader != Client.character.getFighter()) {
                Client.send(new BasicNoOperationMessage());
            } else {
                Fighter Fighter = Client.character.getFight().getFighter(Message.targetId);

                if (Fighter == null || Fighter == Client.character.getFighter()) {
                    Client.send(new BasicNoOperationMessage());
                } else if (Fighter.team != Client.character.getFighter().team) {
                    Client.send(new BasicNoOperationMessage());
                } else {
                    Client.character.getFight().leaveFight(Fighter);
                }
            }
        }

    }

    @HandlerAttribute(ID = GameFightJoinRequestMessage.M_ID)
    public static void HandleGameFightJoinRequestMessage(WorldClient Client, GameFightJoinRequestMessage Message) {
        Fight Fight = Client.character.getCurrentMap().getFight(Message.fightId);
        if (Fight == null) {
            Client.send(new BasicNoOperationMessage());
        } else if (Fight.fightState != FightState.STATE_PLACE) {
            Client.send(new BasicNoOperationMessage());
        } else {
            FightTeam Team = Fight.getTeam(Message.fighterId);
            if (Team == null) { //Ne doit pas arriver
                Client.send(new ChallengeFightJoinRefusedMessage(Client.character.ID, FighterRefusedReasonEnum.JUST_RESPAWNED));
                return;
            }
            FighterRefusedReasonEnum Answer = Fight.canJoin(Team, Client.character);
            if (Answer == FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
                Fighter Fighter = new CharacterFighter(Fight, Client);

                GameAction FightAction = new GameFight(Fighter, Fight);

                Client.addGameAction(FightAction);

                Fight.joinFightTeam(Fighter, Team, false, (short) -1, true);
            } else {
                Client.send(new ChallengeFightJoinRefusedMessage(Client.character.ID, Answer));
            }
        }
    }

    @HandlerAttribute(ID = GameFightPlacementPositionRequestMessage.M_ID)
    public static void HandleGameFightPlacementPositionRequestMessage(WorldClient Client, GameFightPlacementPositionRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.FIGHT) || Client.character.getFight().fightState != FightState.STATE_PLACE || Client.character.getFighter().getCellId() == Message.cellId) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.character.getFight().setFighterPlace(Client.character.getFighter(), (short) Message.cellId);
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
        Player target = client.character.getCurrentMap().getPlayer(Message.targetId);
        if (target == null) {
            client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
        } else if (target == client.character) {
            client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.FIGHT_MYSELF));
        } else if (target.getCurrentMap().getId() != client.character.getCurrentMap().getId()) {
            client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.WRONG_MAP));
        } else if (Message.friendly) {
            if (!target.getClient().canGameAction(GameActionTypeEnum.BASIC_REQUEST) || !target.isInWorld) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
            } else if (!client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.IM_OCCUPIED));
            } else {
                ChallengeFightRequest Request = new ChallengeFightRequest(client, target.getClient());
                GameRequest RequestAction = new GameRequest(client.character, Request);

                client.addGameAction(RequestAction);
                target.getClient().addGameAction(RequestAction);

                client.setBaseRequest(Request);
                target.getClient().setBaseRequest(Request);

            }
        } else {
            if (target.PvPEnabled == NON_AGGRESSABLE || client.character.PvPEnabled == NON_AGGRESSABLE || client.character.alignmentSide == target.alignmentSide) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else if (!target.isInWorld || target.getClient().isGameAction(GameActionTypeEnum.FIGHT)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.OPPONENT_OCCUPIED));
            } else if (!client.character.isInWorld || client.isGameAction(GameActionTypeEnum.FIGHT)) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.IM_OCCUPIED));
            } else if (client.character.alignmentSide == AlignmentSideEnum.ALIGNMENT_NEUTRAL || target.alignmentSide == AlignmentSideEnum.ALIGNMENT_NEUTRAL) {
                client.send(new ChallengeFightJoinRefusedMessage(client.character.ID, FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else {
                target.getClient().abortGameActions();
                Fight Fight = new AgressionFight(client.character.getCurrentMap(), client, target.getClient());
                target.getCurrentMap().addFight(Fight);
            }
        }
    }

}
