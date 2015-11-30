package koh.game.entities.actors.npc;

import koh.game.dao.DAO;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.ItemTemplate;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.types.game.data.items.ObjectItemToSellInNpcShop;

/**
 *
 * @author Neo-Craft
 */
public class NpcItem {

    public boolean maximiseStats;
    public int item, token;
    public String buyCriterion;
    public float customPrice;

    public float getPrice() {
        return customPrice == -1 ? getTemplate().price : this.customPrice;
    }

    public EffectGenerationType genType(){
        return this.maximiseStats ? EffectGenerationType.MaxEffects : EffectGenerationType.Normal;
    }
    
    public ItemTemplate getItemToken() {
        return DAO.getItemTemplates().getTemplate(token);
    }

    public ItemTemplate getTemplate() {
        return DAO.getItemTemplates().getTemplate(item);
    }

    /*/int objectGID, ObjectEffect[] effects, int objectPrice, String buyCriterion*/
    public ObjectItemToSellInNpcShop toShop() {
        return new ObjectItemToSellInNpcShop(this.item, EffectHelper.toObjectEffects(this.getTemplate().possibleEffects), (int) getPrice(), this.buyCriterion);
    }

    public NpcItem() {

    }

}
