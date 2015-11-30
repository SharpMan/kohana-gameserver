package koh.game.entities.environments.cells;

import koh.game.entities.environments.DofusMap;

/**
 *
 * @author Neo-Craft
 */
public class Square extends ZRectangle {

    public Square(byte minRadius, byte radius, DofusMap Map) {
        super(minRadius, radius, radius, Map);
    }

    @Override
    public int getSurface() {
        return (int) (Math.pow(((this.getRadius() * 2) + 1), 2));
    }

}
