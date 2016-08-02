package koh.game.entities.actors.pnj;

import com.google.common.primitives.Ints;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import koh.look.EntityLookParser;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayNpcInformations;
import koh.protocol.types.game.data.items.ObjectItemToSellInNpcShop;
import koh.protocol.types.game.look.EntityLook;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
@Builder
public class NpcTemplate {

    @Getter
    private int id;
    private String name;
    private int[][] dialogMessages, dialogReplies;
    @Getter
    private int[] actions;
    private int gender;
    private String look;
    private boolean fastAnimsFun, orderItemsByPrice, orderItemsByLevel;
    @Getter @Setter
    private Map<Integer, NpcItem> items;

    public int[] getReply(int id) {
        try {
            return this.dialogReplies[id];
        } catch (Exception e) {
            return new int[0];
        }
    }

    public int getMessageOffset(int Message) {
        for (int i = 0; i < this.dialogMessages.length; i++) {
            if (Ints.contains(this.dialogMessages[i], Message)) {
                //return Ints.indexOf(i, Message);
                return i;
            }
        }
        return -1;
    }

    private ObjectItemToSellInNpcShop[] itemList = null;

    public static <T> Comparator<T> Compose(
            final Comparator<? super T> primary,
            final Comparator<? super T> secondary
    ) {
        return (T a, T b) -> {
            int result = primary.compare(a, b);
            return result == 0 ? secondary.compare(a, b) : result;
        };
    }

    public ObjectItemToSellInNpcShop[] getItems$Array() {
        if (itemList == null) {
            if (items == null) {
                itemList = new ObjectItemToSellInNpcShop[0];
            } else {
                Stream<NpcItem> Objects = this.items.values().stream();
                if (this.id == 816) {
                    Objects = Objects.filter(Item -> Item.getTemplate().getLevel() > 80).sorted(Compose(((e1, e2) -> Float.compare(e1.getTemplate().getTypeId(), e2.getTemplate().getTypeId())), ((e1, e2) -> Integer.compare(e1.getTemplate().getLevel(), e2.getTemplate().getLevel()))));
                }
                if (this.orderItemsByPrice) {
                    Objects = Objects.sorted((e1, e2) -> Float.compare(e1.getPrice(), e2.getPrice()));
                }
                if (this.orderItemsByLevel) {
                    Objects = Objects.sorted((e1, e2) -> Integer.compare(e1.getTemplate().getLevel(), e2.getTemplate().getLevel()));
                }

                itemList = Objects.map(x -> x.toShop()).filter(x -> x != null).toArray(ObjectItemToSellInNpcShop[]::new);
            }
        }
        return itemList;
    }

    public int getDialogMessage(int id, int pos) {
        try {
            return this.dialogMessages[id][pos];
        } catch (Exception e) {
            return -1;
        }
    }

    public GameRolePlayActorInformations getProjection(short cell, byte dir){
        return new GameRolePlayNpcInformations(-100, this.getEntityLook(), new EntityDispositionInformations(cell, dir), (short) this.getId(), false, 0);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    int i = -1;

    public int getCommonTokenId() {
        if (items == null) {
            return 0;
        }
        /*if(i == -1){
            i = this.items.values().stream()
                    .map(e -> e.getToken())
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .orElse(new Map.Entry<Integer, Long>() {
                        @Override
                        public Integer getKey() {
                            return -1;
                        }

                        @Override
                        public Long getValue() {
                            return null;
                        }

                        @Override
                        public Long setValue(Long value) {
                            return null;
                        }
                    }).getKey();
        }
        return i;*/
        return this.items.values().stream().findFirst().get().getToken();

    }

    public EntityLook getEntityLook() {
        return EntityLookParser.fromString(this.look);
    }

}
