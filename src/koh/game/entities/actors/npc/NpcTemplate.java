package koh.game.entities.actors.npc;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
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
    public boolean fastAnimsFun, OrderItemsByPrice, OrderItemsByLevel;
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

    public static <T> Comparator<T> Compose(
            final Comparator<? super T> primary,
            final Comparator<? super T> secondary
    ) {
        return (T a, T b) -> {
            int result = primary.compare(a, b);
            return result == 0 ? secondary.compare(a, b) : result;
        };
    }

    public ObjectItemToSellInNpcShop[] GetItems() {
        if (ItemList == null) {
            if (Items == null) {
                ItemList = new ObjectItemToSellInNpcShop[0];
            } else {
                Stream<NpcItem> Objects = this.Items.values().stream();
                if (this.Id == 816) {
                    Objects = Objects.filter(Item -> Item.Template().level > 80).sorted(Compose(((e1, e2) -> Float.compare(e1.Template().TypeId, e2.Template().TypeId)), ((e1, e2) -> Integer.compare(e1.Template().level, e2.Template().level))));
                }
                if (this.OrderItemsByPrice) {
                    Objects = Objects.sorted((e1, e2) -> Float.compare(e1.Price(), e2.Price()));
                }
                if (this.OrderItemsByLevel) {
                    Objects = Objects.sorted((e1, e2) -> Integer.compare(e1.Template().level, e2.Template().level));
                }

                ItemList = Objects.map(x -> x.toShop()).toArray(ObjectItemToSellInNpcShop[]::new);
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
