package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public class FightException extends Exception {

    public String Message;

    public FightException(String Message) {
        super(Message);
        this.Message = Message;
    }

}
