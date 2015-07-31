package koh.game.entities.actors.npc;

import koh.game.dao.ItemDAO;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.ItemTemplate;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.types.game.data.items.ObjectItemToSellInNpcShop;

/**
 *
 * @author Neo-Craft
 */
public class NpcItem {

    public boolean MaximiseStats;
    public int Item, Token;
    public String BuyCriterion;
    public float CustomPrice;

    public float Price() {
        return CustomPrice == -1 ? Template().price : this.CustomPrice;
    }

    public EffectGenerationType GenType(){
        return this.MaximiseStats ? EffectGenerationType.MaxEffects : EffectGenerationType.Normal;
    }
    
    public ItemTemplate ItemToken() {
        return ItemDAO.Cache.get(Token);
    }

    public ItemTemplate Template() {
        return ItemDAO.Cache.get(Item);
    }

    /*/int objectGID, ObjectEffect[] effects, int objectPrice, String buyCriterion*/
    public ObjectItemToSellInNpcShop toShop() {
        return new ObjectItemToSellInNpcShop(this.Item, EffectHelper.toObjectEffects(this.Template().possibleEffects), (int) Price(), this.BuyCriterion);
    }

    public NpcItem() {

    }

}
