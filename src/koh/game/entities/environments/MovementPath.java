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

    public short[] SerializePath() {
        if (!this.mySerialized) {
            //byte lastDirection = -1;
            for (int i = 0; i < TransitCells.size(); i++) {
                /* if (lastDirection == Directions.get(i)) {
                 System.out.println(lastDirection +" "+Directions.get(i));
                 continue;
                 }*/
                this.keyMovements = ArrayUtils.add(keyMovements, (short) (((Directions.get(i) & 7) << 12) | (TransitCells.get(i) & 4095)));
                //lastDirection = Directions.get(i);
            }
            this.mySerialized = true;
        }

        return this.keyMovements;
    }

    public short BeginCell() {
        return TransitCells.stream().findFirst().get();
    }

    public int MovementLength;

    public int MovementTime() {
        return (int) Pathfinder.GetPathTime(this.MovementLength);
    }

    public short LastStep() {
        return TransitCells.get(TransitCells.size() - 2);
    }

    public short EndCell() {
        return TransitCells.get(TransitCells.size() - 1);
    }

    public void AddCell(short Cell, byte Direction) {
        this.TransitCells.add(Cell);
        this.Directions.add(Direction);
    }

    public byte GetDirection(short Cell) {
        return this.Directions.get(TransitCells.indexOf(Cell));
    }

    public void Clean() {
        List<Short> TransitCells = new ArrayList<>();
        List<Byte> Directions = new ArrayList<>();

        for (int i = 0; i < this.Directions.size(); i++) {
            if (i == this.Directions.size() - 1) {
                TransitCells.add(this.TransitCells.get(i));
                Directions.add(this.Directions.get(i));
            } else {
                if (!Objects.equals(this.Directions.get(i), this.Directions.get(i + 1))) {
                    TransitCells.add(this.TransitCells.get(i));
                    Directions.add(this.Directions.get(i));
                }
            }
        }

        this.TransitCells = TransitCells;
        this.Directions = Directions;
    }

    public List<Short> TransitCells = new ArrayList<>();
    public List<Byte> Directions = new ArrayList<>();

    public void CutPath(int index) {
        if (index > this.TransitCells.size() - 1) {
            return;
        }
        this.TransitCells = this.TransitCells.subList(0, index);
        this.Directions = this.Directions.subList(0, index);
        this.MovementLength = index - 1;
        //this.keyMovements = ArrayUtils.subarray(this.keyMovements ,0, index);
    }

}
