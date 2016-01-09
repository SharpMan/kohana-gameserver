package koh.game.fights.fighters;

import lombok.Getter;

/**
 * Created by Melancholia on 1/1/16.
 */
public final class DroppedItem {

    @Getter
    private final int item;
    @Getter
    private int quantity;

    public DroppedItem(int item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public void accumulateQuantity(){
        this.quantity++;
    }
}
