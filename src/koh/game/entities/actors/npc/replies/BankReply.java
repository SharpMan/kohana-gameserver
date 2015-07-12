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
    public boolean Execute(Player player) {
        if (!super.Execute(player)) {
            return false;
        }
        if (player.Client.CanGameAction(GameActionTypeEnum.EXCHANGE)) {
            player.Client.myExchange = new StorageExchange(player.Client);
            player.Client.AddGameAction(new GameExchange(player, player.Client.myExchange));
        }
        return true;
    }

}
