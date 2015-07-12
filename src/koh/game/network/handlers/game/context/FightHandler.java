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
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFighter() != Client.Character.GetFighter().Team.Leader) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        Client.Character.GetFight().ToggleLock(Client.Character.GetFighter(), FightOptionsEnum.valueOf(Message.option));
    }

    @HandlerAttribute(ID = 718)
    public static void HandleGameFightTurnFinishMessage(WorldClient Client, GameFightTurnFinishMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFight().FightState != FightState.STATE_ACTIVE) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFight().CurrentFighter == Client.Character.GetFighter()) {
            Client.Character.GetFight().FightLoopState = FightLoopState.STATE_END_TURN;
        }
    }

    @HandlerAttribute(ID = GameFightTurnReadyMessage.M_ID)
    public static void HandleGameFightTurnReadyMessage(WorldClient Client, GameFightTurnReadyMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFight().FightState != FightState.STATE_ACTIVE) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFighter() == null) {
            return;
        }

        Client.Character.GetFighter().TurnReady = true;
    }

    @HandlerAttribute(ID = 255)
    public static void HandleGameContextQuitMessage(WorldClient Client, GameContextQuitMessage Message) {
        if (Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Character.GetFight().LeaveFight(Client.Character.GetFighter());
        } else {

        }
    }

    @HandlerAttribute(ID = 708)
    public static void HandleGameFightReadyMessage(WorldClient Client, GameFightReadyMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.Character.GetFight().FightState != FightState.STATE_PLACE) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        Client.Character.GetFight().SetFighterReady(Client.Character.GetFighter());
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsRequestMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsRequestMessage(WorldClient Client, GameFightPlacementSwapPositionsRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT) || Client.Character.GetFight().FightState != FightState.STATE_PLACE || Client.Character.GetFighter().ID == Message.requestedId) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        Fighter Fighter = Client.Character.GetFight().GetFighter(Message.requestedId);
        if (Fighter == null || Fighter == Client.Character.GetFighter()) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (Client.Character.GetFighter().Team.Leader != Client.Character.GetFighter()) {  //Demande
            synchronized (Client.Character.GetFighter().Team.SwapRequests) {
                SwapPositionRequest Request = new SwapPositionRequest(Fighter.Team.GetNextRequestId(), Client.Character.ID, Client.Character.GetFighter().CellId(), Fighter.ID, Fighter.CellId());
                Client.Character.GetFighter().Team.SwapRequests.add(Request);
                Client.Character.GetFighter().Team.sendToField(new GameFightPlacementSwapPositionsOfferMessage(Request.requestId, Request.requesterId, Request.requesterCellId, Request.requestedId, Request.requestedCellId));
            }
        } else {
            Client.Character.GetFight().SwapPosition(Client.Character.GetFighter(), Fighter);
        }
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsCancelMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsCancelMessage(WorldClient Client, GameFightPlacementSwapPositionsCancelMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT) || Client.Character.GetFight().FightState != FightState.STATE_PLACE || Client.Character.GetFighter().Team.GetRequest(Message.requestId) == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        Client.Character.GetFighter().Team.sendToField(new GameFightPlacementSwapPositionsCancelledMessage(Client.Character.GetFighter().Team.GetRequest(Message.requestId).requesterId, Client.Character.ID));
    }

    @HandlerAttribute(ID = GameFightPlacementSwapPositionsAcceptMessage.M_ID)
    public static void HandleGameFightPlacementSwapPositionsAcceptMessage(WorldClient Client, GameFightPlacementSwapPositionsAcceptMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT) || Client.Character.GetFight().FightState != FightState.STATE_PLACE || Client.Character.GetFighter().Team.GetRequest(Message.requestId) == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        //TODO: +=Event onLeftFight remove all joined requests + delete this part of code
        Fighter Target = null;
        if ((Target = Client.Character.GetFight().GetFighter(Client.Character.GetFighter().Team.GetRequest(Message.requestId).requesterId)) != null) {
            Client.Character.GetFight().SwapPosition(Client.Character.GetFighter(), Target);
        }
    }

    @HandlerAttribute(ID = 6081)
    public static void HandleGameContextKickMessage(WorldClient Client, GameContextKickMessage Message) {
        if (Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            if (Client.Character.GetFight().FightState != FightState.STATE_PLACE) {
                Client.Send(new BasicNoOperationMessage());
            } else if (Client.Character.GetFighter().Team.Leader != Client.Character.GetFighter()) {
                Client.Send(new BasicNoOperationMessage());
            } else {
                Fighter Fighter = Client.Character.GetFight().GetFighter(Message.targetId);

                if (Fighter == null || Fighter == Client.Character.GetFighter()) {
                    Client.Send(new BasicNoOperationMessage());
                } else if (Fighter.Team != Client.Character.GetFighter().Team) {
                    Client.Send(new BasicNoOperationMessage());
                } else {
                    Client.Character.GetFight().LeaveFight(Fighter);
                }
            }
        }

    }

    @HandlerAttribute(ID = GameFightJoinRequestMessage.M_ID)
    public static void HandleGameFightJoinRequestMessage(WorldClient Client, GameFightJoinRequestMessage Message) {
        Fight Fight = Client.Character.CurrentMap.GetFight(Message.fightId);
        if (Fight == null) {
            Client.Send(new BasicNoOperationMessage());
        } else if (Fight.FightState != FightState.STATE_PLACE) {
            Client.Send(new BasicNoOperationMessage());
        } else {
            FightTeam Team = Fight.GetTeam(Message.fighterId);
            if (Team == null) { //Ne doit pas arriver
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.JUST_RESPAWNED));
                return;
            }
            FighterRefusedReasonEnum Answer = Fight.CanJoin(Team, Client.Character);
            if (Answer == FighterRefusedReasonEnum.FIGHTER_ACCEPTED) {
                Fighter Fighter = new CharacterFighter(Fight, Client);

                GameAction FightAction = new GameFight(Fighter, Fight);

                Client.AddGameAction(FightAction);

                Fight.JoinFightTeam(Fighter, Team, false, (short) -1, true);
            } else {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, Answer));
            }
        }
    }

    @HandlerAttribute(ID = GameFightPlacementPositionRequestMessage.M_ID)
    public static void HandleGameFightPlacementPositionRequestMessage(WorldClient Client, GameFightPlacementPositionRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.FIGHT) || Client.Character.GetFight().FightState != FightState.STATE_PLACE || Client.Character.GetFighter().CellId() == Message.cellId) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        Client.Character.GetFight().SetFighterPlace(Client.Character.GetFighter(), (short) Message.cellId);
    }

    @HandlerAttribute(ID = 5732)
    public static void HandleGameRolePlayPlayerFightFriendlyAnswerMessage(WorldClient Client, GameRolePlayPlayerFightFriendlyAnswerMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        ChallengeFightRequest InvitationRequest = (ChallengeFightRequest) Client.GetBaseRequest();
        if (Message.accept) {
            InvitationRequest.Accept();
        } else if (Client == InvitationRequest.Requested) {
            InvitationRequest.Declin();
        } else {
            InvitationRequest.Cancel();
        }

    }

    @HandlerAttribute(ID = GameRolePlayPlayerFightRequestMessage.M_ID)
    public static void HandleGameRolePlayPlayerFightRequestMessage(WorldClient Client, GameRolePlayPlayerFightRequestMessage Message) {
        Player Target = Client.Character.CurrentMap.GetPlayer(Message.targetId);
        if (Target == null) {
            Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
        } else if (Target == Client.Character) {
            Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.FIGHT_MYSELF));
        } else if (Target.CurrentMap.Id != Client.Character.CurrentMap.Id) {
            Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.WRONG_MAP));
        } else if (Message.friendly) {
            if (!Target.Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST) || !Target.IsInWorld) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
            } else if (!Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.IM_OCCUPIED));
            } else {
                ChallengeFightRequest Request = new ChallengeFightRequest(Client, Target.Client);
                GameRequest RequestAction = new GameRequest(Client.Character, Request);

                Client.AddGameAction(RequestAction);
                Target.Client.AddGameAction(RequestAction);

                Client.SetBaseRequest(Request);
                Target.Client.SetBaseRequest(Request);

            }
        } else {
            if (Target.PvPEnabled == NON_AGGRESSABLE || Client.Character.PvPEnabled == NON_AGGRESSABLE || Client.Character.AlignmentSide == Target.AlignmentSide) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else if (!Target.IsInWorld || Target.Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.OPPONENT_OCCUPIED));
            } else if (!Client.Character.IsInWorld || Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.IM_OCCUPIED));
            } else if (Client.Character.AlignmentSide == AlignmentSideEnum.ALIGNMENT_NEUTRAL || Target.AlignmentSide == AlignmentSideEnum.ALIGNMENT_NEUTRAL) {
                Client.Send(new ChallengeFightJoinRefusedMessage(Client.Character.ID, FighterRefusedReasonEnum.WRONG_ALIGNMENT));
            } else {
                Target.Client.AbortGameActions();
                Fight Fight = new AgressionFight(Client.Character.CurrentMap, Client, Target.Client);
                Target.CurrentMap.AddFight(Fight);
            }
        }
    }

}
