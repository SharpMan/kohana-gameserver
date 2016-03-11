package koh.game.entities.actors.pnj.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameExchange;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;
import koh.game.exchange.StorageExchange;

/**
 *
 * @author Neo-Craft
 */
public class BankReply extends NpcReply {

    @Override
    public boolean execute(Player player) {
        if (!super.execute(player)) {
            return false;
        }
        if (player.getClient().canGameAction(GameActionTypeEnum.EXCHANGE)) {
            player.getClient().setMyExchange(new StorageExchange(player.getClient()));
            player.getClient().addGameAction(new GameExchange(player, player.getClient().getMyExchange()));
        }
        return true;
    }

}
