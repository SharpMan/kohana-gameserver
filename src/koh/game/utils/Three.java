package koh.game.utils;

/**
 *
 * @author Neo-Craft
 */
public class Three <L, R,S> {

    public L first;
    public R second;
    public S tree;

    public Three(L s, R i,S j) {
        first = s;
        second = i;
        tree = j;
    }

    public void clear() {
        try {
            this.first = null;
            this.second = null;
            this.tree = null;
            this.finalize();
        } catch (Throwable tr) {

        }
    }

}
