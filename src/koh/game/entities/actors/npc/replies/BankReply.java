package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameExchange;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcReply;
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
        if (player.client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
            player.client.myExchange = new StorageExchange(player.client);
            player.client.addGameAction(new GameExchange(player, player.client.myExchange));
        }
        return true;
    }

}
