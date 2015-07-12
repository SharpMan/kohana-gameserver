package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public class FightExpetion extends Exception {

    public String Message;

    public FightExpetion(String Message) {
        super(Message);
        this.Message = Message;
    }

}
