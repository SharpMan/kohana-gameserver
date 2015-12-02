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
    public EntityLook entityLook;
    public ArrayList<Couple<Integer, Double>> stats = new ArrayList<>();
    public int scroolId;

}
