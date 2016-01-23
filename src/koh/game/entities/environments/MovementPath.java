package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class MovementPath {

    private short[] keyMovements;
    private boolean mySerialized = false;

    private ArrayList <Short> getCompress(){
        ArrayList <Short> copies = new ArrayList<>(transitCells.size());
        copies.addAll(transitCells);
        return copies;
    }

    public short[] encode(){
        final ArrayList<Short> cells = this.getCompress();
        final short[] encodedPath = new short[cells.size()];
        byte lastOrientation;
        for(int i = 0 ; i < encodedPath.length; i++){
            lastOrientation = this.directions.get(i);
            encodedPath[i] = (short) (((lastOrientation & 7) << 12) | (cells.get(i) & 4095));
        }
        return encodedPath;
    }

    public short[] serializePath() {
        if (!this.mySerialized) {
            try {
                byte lastDirection = -1;
                if (transitCells.size() > 0) {
                    for (int i = transitCells.size() - 2; i > 0; i--) {
                        if (directions.get(i) == directions.get(i - 1)) {
                            transitCells.remove(i);
                            directions.remove(i);
                        }
                    }
                }
                this.keyMovements = new short[transitCells.size()];
                for (int i = 0; i < transitCells.size(); i++) {
                    this.keyMovements[i] = (short) (((directions.get(i) & 7) << 12) | (transitCells.get(i) & 4095));
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            this.mySerialized = true;
        }

        return this.keyMovements;
    }

    public short beginCell() {
        return transitCells.get(0);
    }

    @Setter @Getter
    private int movementLength = 0;

    public int getMovementTime() {
        return (int) Pathfunction.getPathTime(this.movementLength);
    }

    public short getLastStep() {
        return transitCells.get(transitCells.size() < 2 ? 0 : transitCells.size() - 2);
    }

    public short getEndCell() {
        return transitCells.get(transitCells.size() - 1);
    }

    public byte getEndDirection() {
        return directions.get(directions.size() - 1);
    }

    public void addCell(short Cell, byte Direction) {
        this.transitCells.add(Cell);
        this.directions.add(Direction);
    }

    public byte getDirection(int index) {
        return this.directions.get(index);
    }

    public byte getDirection(short Cell) {
        return this.directions.get(transitCells.indexOf(Cell));
    }

    public void clean() {
        List<Short> transitCells = new ArrayList<>();
        List<Byte> directions = new ArrayList<>();

        for (int i = 0; i < this.directions.size(); i++) {
            if (i == this.directions.size() - 1) {
                transitCells.add(this.transitCells.get(i));
                directions.add(this.directions.get(i));
            } else {
                if (!Objects.equals(this.directions.get(i), this.directions.get(i + 1))) {
                    transitCells.add(this.transitCells.get(i));
                    directions.add(this.directions.get(i));
                }
            }
        }

        this.transitCells = transitCells;
        this.directions = directions;
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
    }

}
