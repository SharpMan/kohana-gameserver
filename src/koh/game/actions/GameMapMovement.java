package koh.game.actions;

import koh.game.Main;
import koh.game.dao.api.AccountDataDAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.environments.IWorldField;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class GameMapMovement extends GameAction {

    private static final Logger logger = LogManager.getLogger(GameMapMovement.class);

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
                        this.myField.actorMoved(null, this.actor, (short) (keyMovements[keyMovements.length - 1] & 4095), (byte) (keyMovements[keyMovements.length - 1] >> 12 & 7));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(this.toString());
        }
        super.execute();
    }

    @Override
    public void abort(Object[] args) {
        synchronized (this.sync) {
            // deja fini ?
            if (!this.isFinish) {
                // deja aborté ?
                if (!this.myAborted) {
                    // cell de transit ?
                    if (args.length > 0) {
                        final short stoppedCell = (short) args[0];

                        try {
                            // on apelle
                            super.endExecute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        this.myField.actorMoved(null, this.actor, stoppedCell, (byte) -1);
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
