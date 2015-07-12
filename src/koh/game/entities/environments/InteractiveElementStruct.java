package koh.game.entities.environments;

import java.util.ArrayList;
import koh.protocol.types.game.interactive.InteractiveElementNamedSkill;
import koh.protocol.types.game.interactive.InteractiveElementSkill;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveElementStruct {

    public int elementId = 0;
    public int elementTypeId = 0;
    public ArrayList<InteractiveElementSkill> Skills;
    public short AgeBonus = -1;
    
     public InteractiveElementSkill GetSkill(int id) {
        return Skills.stream().filter(x -> x.skillInstanceUid == id).findFirst().orElse(null);
    }
    
     public InteractiveElementSkill GetSkillID(int id) {
         return Skills.stream().filter(x -> x.skillId == id).findFirst().orElse(null);
    }

    public InteractiveElementStruct(int elementId, int elementTypeId, String enabledSkills, String disabledSkills, short AgeBonus) {
        this.elementId = elementId;
        this.elementTypeId = elementTypeId;
        this.Skills  = new ArrayList<>();
        if (enabledSkills.contains(",")) {
            for (String c : enabledSkills.split(";")) {
                this.Skills.add(c.split(",").length > 2 ? new InteractiveElementNamedSkill(Integer.parseInt(c.split(",")[0]), Integer.parseInt(c.split(",")[1]), Integer.parseInt(c.split(",")[2])) : new InteractiveElementSkill(Integer.parseInt(c.split(",")[0]), Integer.parseInt(c.split(",")[1])));
            }
        }
        if (disabledSkills.contains(",")) {
            for (String c : disabledSkills.split(";")) {
                this.Skills.add(c.split(",").length > 2 ? new InteractiveElementNamedSkill(Integer.parseInt(c.split(",")[0]), Integer.parseInt(c.split(",")[1]), Integer.parseInt(c.split(",")[2])) : new InteractiveElementSkill(Integer.parseInt(c.split(",")[0]), Integer.parseInt(c.split(",")[1])));
            }
        }
        //this.AgeBonus = -1;
    }

}
