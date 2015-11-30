package koh.game.actions;

import koh.game.Main;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.environments.IWorldField;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class GameMapMovement extends GameAction {

    // syncro
    private final Object sync = new Object();

    // mouvement stoppé ?
    private boolean myAborted = false;
    public short[] keyMovements;

    // map du deplacement
    private final IWorldField myField;

    public GameMapMovement(IWorldField field, IGameActor actor, short[] keyMovements) {
        super(GameActionTypeEnum.MAP_MOVEMENT, actor);
        this.keyMovements = keyMovements;
        this.myField = field;
    }

    @Override
    public void execute() {
        try {
            synchronized (this.sync) {
                if (!this.isFinish) {
                    // mouvement stoppé ?
                    if (!this.myAborted) {
                        this.myField.ActorMoved(null, this.actor, (short) (keyMovements[keyMovements.length - 1] & 4095), (byte) (keyMovements[keyMovements.length - 1] >> 12 & 7));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.Logs().writeError(this.toString());
        }
        super.execute();
    }

    @Override
    public void abort(Object[] Args) {
        synchronized (this.sync) {
            // deja fini ?
            if (!this.isFinish) {
                // deja aborté ?
                if (!this.myAborted) {
                    // cell de transit ?
                    if (Args.length > 0) {
                        short StoppedCell = (short) Args[0];

                        try {
                            // on apell
                            super.endExecute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        this.myField.ActorMoved(null, this.actor, StoppedCell, (byte) -1);
                    }

                    this.myAborted = true;
                }
            }
        }
    }

    @Override
    public void endExecute() throws Exception {
        super.endExecute();
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
