package koh.game.entities.item.animal;

import java.util.ArrayList;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public class MountTemplate {
    
    public MountTemplate(){
        
    }

    public int Id;
    public EntityLook Look;
    public ArrayList<Couple<Integer, Double>> Stats = new ArrayList<>();
    public int ScroolId;

}
