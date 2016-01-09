package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class MovementPath {

    private short[] keyMovements = new short[0];
    private boolean mySerialized = false;

    public short[] serializePath() {
        if (!this.mySerialized) {
            //byte lastDirection = -1;
            for (int i = 0; i < transitCells.size(); i++) {
                /* if (lastDirection == directions.get(i)) {
                 System.out.println(lastDirection +" "+directions.get(i));
                 continue;
                 }*/
                this.keyMovements = ArrayUtils.add(keyMovements, (short) (((directions.get(i) & 7) << 12) | (transitCells.get(i) & 4095)));
                //lastDirection = directions.get(i);
            }
            this.mySerialized = true;
        }

        return this.keyMovements;
    }

    public short beginCell() {
        return transitCells.stream().findFirst().get();
    }

    public int movementLength;

    public int getMovementTime() {
        return (int) Pathfinder.getPathTime(this.movementLength);
    }

    public short getLastStep() {
        return transitCells.get(transitCells.size() < 2 ? 0 : transitCells.size() - 2);
    }

    public short getEndCell() {
        return transitCells.get(transitCells.size() - 1);
    }

    public void addCell(short Cell, byte Direction) {
        this.transitCells.add(Cell);
        this.directions.add(Direction);
    }

    public byte getDirection(short Cell) {
        return this.directions.get(transitCells.indexOf(Cell));
    }

    public void clean() {
        List<Short> TransitCells = new ArrayList<>();
        List<Byte> Directions = new ArrayList<>();

        for (int i = 0; i < this.directions.size(); i++) {
            if (i == this.directions.size() - 1) {
                TransitCells.add(this.transitCells.get(i));
                Directions.add(this.directions.get(i));
            } else {
                if (!Objects.equals(this.directions.get(i), this.directions.get(i + 1))) {
                    TransitCells.add(this.transitCells.get(i));
                    Directions.add(this.directions.get(i));
                }
            }
        }

        this.transitCells = TransitCells;
        this.directions = Directions;
    }

    public List<Short> transitCells = new ArrayList<>();
    public List<Byte> directions = new ArrayList<>();

    public void cutPath(int index) {
        if (index > this.transitCells.size() - 1) {
            return;
        }
        this.transitCells = this.transitCells.subList(0, index);
        this.directions = this.directions.subList(0, index);
        this.movementLength = index - 1;
        //this.keyMovements = ArrayUtils.subarray(this.keyMovements ,0, index);
    }

}
