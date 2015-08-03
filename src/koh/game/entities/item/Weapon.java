package koh.game.entities.item;

import java.util.Arrays;
import koh.game.dao.ItemDAO;
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

    public boolean Initialized = false;

    public void Initialize() {
        if(this.Initialized)
            return;
        Arrays.stream(this.possibleEffects).filter(e -> ArrayUtils.contains(unRandomablesEffects, e.effectId)).forEach(Effect -> Effect.rawZone = this.ItemType().rawZone);
        this.Initialized = true;
    }

    public ItemType ItemType() {
        return ItemDAO.SuperTypes.get(TypeId);
    }

}
