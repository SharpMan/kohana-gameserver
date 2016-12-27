package koh.game.entities.actors.pnj.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameExchange;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;
import koh.game.exchange.StorageExchange;

/**
 * Created by Melancholia on 8/19/16.
 */
public class DjReply extends NpcReply {



    @Override
    public boolean execute(Player player) {
        if (!super.execute(player) || !player.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP)) {
            return false;
        }

            if(player.getInventoryCache().hasItemId(Integer.parseInt(getParameters()[2]))){
                try {
                    if (player.getClient().getParty() != null) {
                        player.getClient().getParty().getPlayers().stream()
                                .filter(p -> p.getClient() != null && p.getClient().canGameAction(GameActionTypeEnum.FIGHT))
                                .forEach(p -> p.teleport(Integer.parseInt(getParameters()[0]), Integer.parseInt(getParameters()[0])));
                    } else
                        player.teleport(Integer.parseInt(getParameters()[0]), Integer.parseInt(getParameters()[0]));
                }catch (Exception e){
                    e.printStackTrace();
                    player.teleport(Integer.parseInt(getParameters()[0]), Integer.parseInt(getParameters()[0]));
                }

                player.getInventoryCache().safeDelete(player.getInventoryCache().findTemplate(Integer.parseInt(getParameters()[2])),1);
            }
            else{
                PlayerController.sendServerMessage(player.getClient(),"Vous ne possedez pas la clef necessaire.", "009900");
            }

        return true;
    }

}