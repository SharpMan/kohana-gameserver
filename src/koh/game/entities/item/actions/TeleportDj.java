package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class TeleportDj  extends ItemAction {

    private short newMapID;
    private int newCellID,objetNeed, mapNeeded;

    public TeleportDj(String[] args, String criteria) {
        super(args, criteria);
        short newMapID = Short.parseShort(args[0]);
        int newCellID = Integer.parseInt(args[1]);
        int objetNeed = Integer.parseInt(args[2]);
        int mapNeed = Integer.parseInt(args[3]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        if(true){
            return false; //need to chance all mapid
        }
        if(objetNeed == 0)
        {
            p.teleport(newMapID,newCellID);
        }else if(mapNeeded > 0){
            if(p.getInventoryCache().hasItemId(objetNeed) && p.mapid == mapNeeded){
                p.teleport(newMapID,newCellID);
                p.getInventoryCache().safeDelete(p.getInventoryCache().findTemplate(objetNeed),1);
            }
            else if(p.getCurrentMap().getId() != mapNeeded){
                PlayerController.sendServerMessage(p.getClient(),"Vous n'etes pas sur la bonne map du donjon pour etre teleporter.", "009900");
            }else{
                PlayerController.sendServerMessage(p.getClient(),"Vous ne possedez pas la clef necessaire.", "009900");
            }
        }
        return true;
    }
}
