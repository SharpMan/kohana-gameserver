package koh.game.entities.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import koh.game.dao.DAO;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import static koh.game.entities.item.EffectHelper.unRandomablesEffects;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class Weapon extends ItemTemplate {

    @Getter
    private int range, criticalHitBonus, minRange, maxCastPerTurn, criticalFailureProbability, criticalHitProbability;
    @Getter
    private boolean castInDiagonal,castInLine, castTestLos;
    @Getter
    private int apCost;

    private boolean initialized = false;

    public Weapon(ResultSet result) throws SQLException {
        super(result);
        this.range = result.getInt("range");
        this.criticalHitBonus = result.getInt("range");
        this.minRange = result.getInt("range");
        this.maxCastPerTurn = result.getInt("range");
        this.criticalFailureProbability = result.getInt("range");
        this.criticalHitProbability = result.getInt("range");
        this.castInDiagonal = result.getBoolean("cast_in_diagonal");
        this.apCost = result.getInt("ap_cost");
        this.castInLine = result.getBoolean("cast_in_line");
        this.castTestLos = result.getBoolean("cast_test_los");
    }

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
