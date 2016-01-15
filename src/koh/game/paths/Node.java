package koh.game.paths;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Node {

    @Getter @Setter
    private byte orientation;
    @Getter @Setter
    private short cell;

    public Node() {
    }

    public Node(byte orientation, short cell) {
        this.orientation = orientation;
        this.cell = cell;
    }


    //encode


}
