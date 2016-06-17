package koh.game.entities.actors.pnj;

import koh.game.dao.DAO;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.ItemTemplate;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.types.game.data.items.ObjectItemToSellInNpcShop;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Neo-Craft
 */
@Builder
@ToString
public class NpcItem {
    private static final Logger logger = LogManager.getLogger(NpcItem.class);

    private boolean maximiseStats;
    @Getter
    private int item, token;
    private String buyCriterion;
    private float customPrice;

    public float getPrice() {
        return customPrice == -1 ? getTemplate().getPrice() : this.customPrice;
    }

    public EffectGenerationType genType() {
        return this.maximiseStats ? EffectGenerationType.MAX_EFFECTS : EffectGenerationType.NORMAL;
    }

    public ItemTemplate getItemToken() {
        return DAO.getItemTemplates().getTemplate(token);
    }

    public ItemTemplate getTemplate() {
        return DAO.getItemTemplates().getTemplate(item);
    }

    /*/int objectGID, ObjectEffect[] effects, int objectPrice, String buyCriterion*/
    public ObjectItemToSellInNpcShop toShop() {
        try {
            return new ObjectItemToSellInNpcShop(this.item, EffectHelper.toObjectEffects(this.getTemplate().getPossibleEffects()), (int) getPrice(), this.buyCriterion);
        } catch (NullPointerException e) {
            logger.error("Wrong param with {}", this.toString());
            return null;
        }
    }


}
