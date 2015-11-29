package koh.game.actions.interactive;

import koh.game.actions.GameGuildCreation;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveUsage implements InteractiveAction {

    public enum ActionType {

        TELEPORT(0),
        CREATE_GUILDE(2);

        int Value;

        private ActionType(int Type) {
            this.Value = Type;
        }

        public int value() {
            return Value;
        }

        public static ActionType valueOf(int value) {
            for (ActionType failure : values()) {
                if (failure.Value == value) {
                    return failure;
                }
            }
            return null;
        }
    }

    @Override
    public boolean isEnabled(Player Actor) {
        return true;
    }

    @Override
    public int GetDuration() {
        return 0;
    }

    @Override
    public void Execute(Player Actor, int Element) {
        if (!this.isEnabled(Actor)) {
            Actor.Send(new BasicNoOperationMessage());
            return;
        }
        if (Actor.CurrentMap.getDoor(Element) == null) {
            PlayerController.SendServerMessage(Actor.Client, "Door not implanted yet ..." + Element);
            return;
        }
        switch (ActionType.valueOf(Actor.CurrentMap.getDoor(Element).Type)) {
            case TELEPORT:
                Actor.teleport(Integer.parseInt(Actor.CurrentMap.getDoor(Element).Parameters.split(",")[0]), Integer.parseInt(Actor.CurrentMap.getDoor(Element).Parameters.split(",")[1]));
                break;
            case CREATE_GUILDE:
                //if (Actor.Client.CanGameAction(GameActionTypeEnum.CREATE_GUILD)) {
                Actor.Client.AddGameAction(new GameGuildCreation(Actor));
                //}
                break;
            default:
                PlayerController.SendServerMessage(Actor.Client, "Door not parametered ...");
                return;
        }

    }

    @Override
    public void Leave(Player Actor, int Element) {

    }

    @Override
    public void Abort(Player player, int Element) {

    }

}
