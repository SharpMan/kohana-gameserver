package koh.game.paths;

import koh.collections.SortedList;
import koh.game.fights.Fighter;
import koh.protocol.client.enums.DirectionsEnum;
import lombok.Getter;

import static koh.game.paths.Cells.distanceBetween;
import static koh.game.paths.Cells.getCellIdByOrientation;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Pathfinder {

    private byte[] DIRECTIONS = new byte[] {
            DirectionsEnum.DOWN,
            DirectionsEnum.DOWN_LEFT,
            DirectionsEnum.DOWN_RIGHT,
            DirectionsEnum.LEFT,
            DirectionsEnum.RIGHT,
            DirectionsEnum.UP,
            DirectionsEnum.UP_LEFT,
            DirectionsEnum.UP_RIGHT
    };

    private byte[] ADJACENTS = new byte[] {
            DirectionsEnum.DOWN_LEFT,
            DirectionsEnum.DOWN_RIGHT,
            DirectionsEnum.UP_LEFT,
            DirectionsEnum.UP_RIGHT
    };

    protected final ScoredNode start;
    protected final Fighter fighter;
    protected final short target;
    protected final boolean allDirections;

    protected final SortedList<ScoredNode> open = SortedList.create();
    @Getter
    protected final Path path = new Path();


    protected boolean found;
    @Getter
    private int points;

    public Pathfinder(ScoredNode start, short target, boolean allDirections , Fighter fighter) {
        this.start = start;
        this.target = target;
        this.fighter = fighter;
        this.allDirections = allDirections;
    }

    public Pathfinder(byte startDirection, short startCell, short target, boolean allDirections, Fighter fighter) {
        this(new ScoredNode(startDirection, startCell), target, allDirections, fighter);
    }


    public Path find() throws PathNotFoundException {
        if (found) return path;
        found = true;

        path.add(0, start);

        addAdjacents(start, allDirections);

        while (!open.isEmpty()) {
            ScoredNode best = open.remove(0);
            path.add(best);
            onAdded(best);
            if (mayStop(best)) break;

            addAdjacents(best, allDirections);
            if (open.isEmpty()) throw new PathNotFoundException();
        }

        return path;
    }

    protected void onAdded(Node node) {
        ++points;
    }

    protected boolean mayStop(Node node) {
        return points >= fighter.getMP() || node.getCell() == target;
    }

    protected boolean canAdd(short cellId) {
        return  (!path.contains(cellId) && cellId <= 560 &&  fighter.getFight().getCell(cellId).canWalk());
    }

    protected void addAdjacents(Node node, boolean allDirections) {
        for (byte orientation : (allDirections ? DIRECTIONS : ADJACENTS)) {
            short cellId = getCellIdByOrientation(node, orientation);

            if (cellId != -1 && canAdd(cellId)) {
                ScoredNode newNode = new ScoredNode(orientation, cellId);
                newNode.setDistanceToStart(distanceBetween(start, newNode));
                newNode.setDistanceToEnd(distanceBetween(newNode, target));

                open.add(newNode);
            }
        }
    }
}
