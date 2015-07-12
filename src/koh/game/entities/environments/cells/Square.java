package koh.game.entities.environments.cells;


/**
 *
 * @author Neo-Craft
 */
public class Square extends ZRectangle {

    public Square(byte minRadius, byte radius) {
        super(minRadius, radius, radius);
    }

    @Override
    public int Surface() {
        return (int) (Math.pow(((this.Radius() * 2) + 1), 2));
    }

}
