package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.IWorldField;

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

    public GameMapMovement(IWorldField Field, IGameActor Actor, short[] keyMovements) {
        super(GameActionTypeEnum.MAP_MOVEMENT, Actor);
        this.keyMovements = keyMovements;
        this.myField = Field;
    }

    @Override
    public void Execute() {
        synchronized (this.sync) {
            if (!this.IsFinish) {
                // mouvement stoppé ?
                if (!this.myAborted) {
                    this.myField.ActorMoved(null, this.Actor, (short) (keyMovements[keyMovements.length - 1] & 4095), (byte) (keyMovements[keyMovements.length - 1] >> 12 & 7));
                }
            }
        }
        super.Execute();
    }

    @Override
    public void Abort(Object[] Args) {
        synchronized (this.sync) {
            // deja fini ?
            if (!this.IsFinish) {
                // deja aborté ?
                if (!this.myAborted) {
                    // cell de transit ?
                    if (Args.length > 0) {
                        short StoppedCell = (short) Args[0];

                        try {
                            // on apell
                            super.EndExecute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        this.myField.ActorMoved(null, this.Actor, StoppedCell, (byte) -1);
                    }

                    this.myAborted = true;
                }
            }
        }
    }

    @Override
    public void EndExecute() throws Exception {
        super.EndExecute();
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
