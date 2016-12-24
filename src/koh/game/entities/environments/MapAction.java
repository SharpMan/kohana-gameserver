package koh.game.entities.environments;

import koh.game.actions.GameGuildCreation;
import koh.game.actions.interactive.InteractiveUsage;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import lombok.Getter;

/**
 * Created by Melancholia on 8/11/16.
 */
public class MapAction {

    @Getter
    private final byte action;
    @Getter
    private final String[] param;

    public MapAction(byte action, String param) {
        this.action = action;
        this.param = param.split(",");
    }


    public void execute(Player actor){
        switch (InteractiveUsage.ActionType.valueOf(action)) {
            case TELEPORT:
                actor.fightTeleportation(Integer.parseInt(param[0]), Integer.parseInt(param[1].trim()));
                break;
            case CREATE_GUILDE:
                //if (actor.client.canGameAction(GameActionTypeEnum.CREATE_GUILD)) {
                actor.getClient().addGameAction(new GameGuildCreation(actor));
                //}
                break;
            default:
                PlayerController.sendServerMessage(actor.getClient(), "Action not implanted  ... " + action);
                return;
        }
    }

}
