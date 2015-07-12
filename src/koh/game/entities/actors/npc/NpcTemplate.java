package koh.game.entities.actors.npc;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Map;
import koh.look.EntityLookParser;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkNpcShopMessage;
import koh.protocol.types.game.data.items.ObjectItemToSellInNpcShop;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Couple;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class NpcTemplate {

    public int Id;
    public String Name;
    public int[][] dialogMessages, dialogReplies;
    public int[] actions;
    public int gender;
    public String look;
    public boolean fastAnimsFun;
    public Map<Integer, NpcItem> Items;

    public int[] GetReply(int id) {
        try {
            return this.dialogReplies[id];
        } catch (Exception e) {
            return new int[0];
        }
    }

    public int GetMessageOffset(int Message) {
        for (int i = 0; i < this.dialogMessages.length; i++) {
            if (Ints.contains(this.dialogMessages[i], Message)) {
                //return Ints.indexOf(i, Message);
                return i;
            }
        }
        return -1;
    }

    private ObjectItemToSellInNpcShop[] ItemList = null;

    public ObjectItemToSellInNpcShop[] GetItems() {
        if (ItemList == null) {
            if (Items == null) {
                ItemList = new ObjectItemToSellInNpcShop[0];
            } else {
                ItemList = this.Items.values().stream().map(x -> x.toShop()).toArray(ObjectItemToSellInNpcShop[]::new);
            }
        }
        return ItemList;
    }

    public int getDialogMessage(int id, int pos) {
        try {
            return this.dialogMessages[id][pos];
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int CommonTokenId() {
        if (Items == null) {
            return 0;
        }
        return this.Items.values().stream().findFirst().get().Token;

    }

    public EntityLook GetEntityLook() {
        return EntityLookParser.fromString(this.look);
    }

}
