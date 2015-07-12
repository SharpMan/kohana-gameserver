package koh.game.utils;

/**
 *
 * @author Alleos13
 */
public interface Observer {

    public void Observer$update(Observable o);

    public void Observer$update(Observable o, Object arg);

    public void Observer$update(Observable o, Object... args);

    public void Observer$reset(Observable o);
}
