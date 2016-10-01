package koh.game.network.handlers.game.context;

import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.ChallengeFightRequest;
import koh.game.entities.actors.Player;
import koh.game.entities.fight.Challenge;
import koh.game.fights.Fight;
import koh.game.fights.Fight.FightLoopState;
import koh.game.fights.FightState;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.SlaveFighter;
import koh.game.fights.utils.SwapPositionRequest;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.types.AgressionFight;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import static koh.protocol.client.enums.AggressableStatusEnum.NON_AGGRESSABLE;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.FightOptionsEnum;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
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
import koh.protocol.messages.game.context.fight.challenge.ChallengeTargetsListRequestMessage;
import koh.protocol.messages.game.context.roleplay.fight.GameRolePlayPlayerFightFriendlyAnswerMessage;
import koh.protocol.messages.game.context.roleplay.fight.GameRolePlayPlayerFightRequestMessage;
import koh.protocol.messages.game.guild.ChallengeFightJoinRefusedMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightHandler {

    @HandlerAttribute(ID = ChallengeTargetsListRequestMessage.MESSAGE_ID)
    public static void handleChallengeTargetsListRequestMessage(WorldClient client, ChallengeTargetsListRequestMessage message){
        if (!client.isGameAction(GameActionTypeEnum.FIGHT) || client.getCharacter().getFighter() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final Challenge chal = client.getCharacter().getFight().getChallenges().get(message.getChallengedId(),client.getCharacter().getFighter().getTeam());
        if(chal == null || chal.getTarget() == null){
            client.send(new BasicNoOperationMessage());
            return;
        }
        chal.sendSingleTarget(client);
    }

    @HandlerAttribute(ID = GameFightOptionToggleMessage.M_ID)
    public static void HandleGameFightOptionToggleMessage(WorldClient client, GameFightOptionToggleMessage Message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT) || client.getCharacter().getFighter() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getFighter() != client.getCharacter().getFighter().getTeam().leader) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        client.getCharacter().getFight().toggleLock(client.getCharacter().getFighter(), FightOptionsEnum.valueOf(Message.option));
    }

    @HandlerAttribute(ID = 718)
    public static void HandleGameFightTurnFinishMessage(WorldClient client, GameFightTurnFinishMessage Message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT) || client.getCharacter().getFighter() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getFight().getFightState() != FightState.STATE_ACTIVE) {
            client.send(new BasicNoOperationMessage());
            return;
        }


        if (client.getCharacter().getFight().getCurrentFighter() == client.getCharacter().getFighter() || client.getCharacter().getFight().getCurrentFighter() instanceof SlaveFighter
                && client.getCharacter().getFight().getCurrentFighter().getSummoner() == client.getCharacter().getFighter()) {
            client.getCharacter().getFight().setFightLoopState(FightLoopState.STATE_END_TURN);
        }
    }

    @HandlerAttribute(ID = GameFightTurnReadyMessage.M_ID)
    public static void HandleGameFightTurnReadyMessage(WorldClient client, GameFightTurnReadyMessage message) {
        if (client.getCharacter().getFighter() == null || !client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getFight() == null || client.getCharacter().getFight().getFightState() != FightState.STATE_ACTIVE) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getFighter() == null) {
            return;
        }

        client.getCharacter().getFighter().setTurnReady(true);
    }

    @HandlerAttribute(ID = 255)
    public static void HandleGameContextQuitMessage(WorldClient client, GameContextQuitMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFight() != null) {
            if(client.getCharacter().getFighter() != null)
                client.getCharacter().getFight().leaveFight(client.getCharacter().getFighter());
            else
                client.getCharacter().getFight().leaveSpectator(client);
        } else {
            client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 708)
    public static void handleGameFightReadyMessage(WorldClient client, GameFightReadyMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT) || client.getCharacter().getFight() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if(!client.canParsePacket(message.getClass().getName(), 800)){
            return;
        }

        client.getCharacter().getFight().setFighterReady(client.getCharacter().getFighter());
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsRequestMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsRequestMessage(WorldClient client, GameFightPlacementSwapPositionsRequestMessage message) {
        if (client.getCharacter().getFighter() == null || client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || client.getCharacter().getFighter().getID() == message.requestedId) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        Fighter Fighter = client.getCharacter().getFight().getFighter(message.requestedId);
        if (Fighter == null || Fighter == client.getCharacter().getFighter()) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if(!client.canParsePacket(message.getClass().getName(), 1500)){
            return;
        }
        if (client.getCharacter().getFighter().getTeam().leader != client.getCharacter().getFighter()) {  //Demande
            synchronized (client.getCharacter().getFighter().getTeam().swapRequests) {
                SwapPositionRequest Request = new SwapPositionRequest(Fighter.getTeam().getNextRequestId(), client.getCharacter().getID(), client.getCharacter().getFighter().getCellId(), Fighter.getID(), Fighter.getCellId());
                client.getCharacter().getFighter().getTeam().swapRequests.add(Request);
                client.getCharacter().getFighter().getTeam().sendToField(new GameFightPlacementSwapPositionsOfferMessage(Request.requestId, Request.requesterId, Request.requesterCellId, Request.requestedId, Request.requestedCellId));
            }
        } else {
            client.getCharacter().getFight().swapPosition(client.getCharacter().getFighter(), Fighter);
        }
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsCancelMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsCancelMessage(WorldClient client, GameFightPlacementSwapPositionsCancelMessage message) {
        if (client.getCharacter().getFighter() == null || client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || client.getCharacter().getFighter().getTeam().getRequest(message.requestId) == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if(!client.canParsePacket(message.getClass().getName(), 1500)){
            return;
        }

        client.getCharacter().getFighter().getTeam().sendToField(new GameFightPlacementSwapPositionsCancelledMessage(client.getCharacter().getFighter().getTeam().getRequest(message.requestId).requesterId, client.getCharacter().getID()));
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsAcceptMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsAcceptMessage(WorldClient client, GameFightPlacementSwapPositionsAcceptMessage message) {
        if (client.getCharacter().getFighter() == null || client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || client.getCharacter().getFighter().getTeam().getRequest(message.requestId) == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if(!client.canParsePacket(message.getClass().getName(), 1500)){
            return;
        }
        //TODO: +=Event onLeftFight remove all joined requests + delete this part of code
        Fighter Target = null;
        if ((Target = client.getCharacter().getFight().getFighter(client.getCharacter().getFighter().getTeam().getRequest(message.requestId).requesterId)) != null) {
            client.getCharacter().getFight().swapPosition(client.getCharacter().getFighter(), Target);
        }
    }

    @HandlerAttribute(ID = 6081)
    public static void HandleGameContextKickMessage(WorldClient client, GameContextKickMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFighter() != null ) {
            if (client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
                client.send(new BasicNoOperationMessage());
            } else if (client.getCharacter().getFighter().getTeam().leader != client.getCharacter().getFighter()) {
                client.send(new BasicNoOperationMessage());
            } else {
                final Fighter Fighter = client.getCharacter().getFight().getFighter(Message.targetId);

                if (Fighter == null || Fighter == client.getCharacter().getFighter()) {
                    client.send(new BasicNoOperationMessage());
                } else if (Fighter.getTeam().id != client.getCharacter().getFighter().getTeam().id) {
                    client.send(new BasicNoOperationMessage());
                } else {
                    if(!client.canParsePacket(Message.getClass().getName(), 1000)){
                        return;
                    }
                    client.getCharacter().getFight().leaveFight(Fighter);
                }
            }
        }

    }

    @HandlerAttribute(ID = GameFightJoinRequestMessage.M_ID)
    public static void HandleGameFightJoinRequestMessage(WorldClient client, GameFightJoinRequestMessage message) {
        final Fight fight = client.getCharacter().getCurrentMap().getFight(message.fightId);
        if (fight == null) {
            client.send(new BasicNoOperationMessage());
        } else if (fight.getFightState() == FightState.STATE_ACTIVE) {
            if(fight.canJoinSpectator()){
                fight.joinFightSpectator(client);
            }else{
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR,57));
            }
        } else if(fight.getFightState() == FightState.STATE_PLACE) {
            final FightTeam team = fight.getTeam(message.fighterId);
            if (team == null) { //Ne doit pas arriver
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.JUST_RESPAWNED));
                return;
            }
            if(!client.canGameAction(GameActionTypeEnum.FIGHT)){
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.IM_OCCUPIED));
                return;
            }
            final FighterRefusedReasonEnum answer = fight.canJoin(team, client.getCharacter());
            if (answer == FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
                final Fighter Fighter = new CharacterFighter(fight, client);

                GameAction FightAction = new GameFight(Fighter, fight);

                client.addGameAction(FightAction);

                fight.joinFightTeam(Fighter, team, false, (short) -1, true);
            } else {
                client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), answer));
            }
        }
    }

    @HandlerAttribute(ID = GameFightPlacementPositionRequestMessage.M_ID)
    public static void HandleGameFightPlacementPositionRequestMessage(WorldClient client, GameFightPlacementPositionRequestMessage Message) {
        if (!client.isGameAction(GameActionTypeEnum.FIGHT) || client.getCharacter().getFight() == null || client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE || client.getCharacter().getFighter().getCellId() == Message.cellId) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        client.getCharacter().getFight().setFighterPlace(client.getCharacter().getFighter(), (short) Message.cellId);
    }

    @HandlerAttribute(ID = 5732)
    public static void HandleGameRolePlayPlayerFightFriendlyAnswerMessage(WorldClient Client, GameRolePlayPlayerFightFriendlyAnswerMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        final ChallengeFightRequest invitationRequest = (ChallengeFightRequest) Client.getBaseRequest();
        if (Message.accept) {
            invitationRequest.accept();
        } else if (Client == invitationRequest.requested) {
            invitationRequest.declin();
        } else {
            invitationRequest.Cancel();
        }

    }

    @HandlerAttribute(ID = GameRolePlayPlayerFightRequestMessage.M_ID)
    public static void HandleGameRolePlayPlayerFightRequestMessage(WorldClient client, GameRolePlayPlayerFightRequestMessage Message) {
        final Player target = client.getCharacter().getCurrentMap().getPlayer(Message.targetId);
        if (target == null || target.getClient() == null) {
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
                final Fight Fight = new AgressionFight(client.getCharacter().getCurrentMap(), client, target.getClient());
                target.getCurrentMap().addFight(Fight);
            }
        }
    }

}
