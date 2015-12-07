package koh.game.entities.item;

import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import static koh.game.entities.item.EffectHelper.unRandomablesEffects;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class Weapon extends ItemTemplate {

    public int range, criticalHitBonus, minRange, maxCastPerTurn, criticalFailureProbability, criticalHitProbability;
    public boolean castInDiagonal;
    public int apCost;
    public boolean castInLine, castTestLos;

    public boolean initialized = false;

    public void initialize() {
        if(this.initialized)
            return;
        Arrays.stream(this.possibleEffects).filter(e -> ArrayUtils.contains(unRandomablesEffects, e.effectId)).forEach(Effect -> Effect.rawZone = this.getItemType().getRawZone());
        this.initialized = true;
    }

    public ItemType getItemType() {
        return DAO.getItemTemplates().getType(typeId);
    }

}
