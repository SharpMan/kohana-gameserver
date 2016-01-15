package koh.game.paths;

/**
 * Created by Melancholia on 1/13/16.
 */
public class ScoredNode extends Node implements Comparable<ScoredNode> {
    private int distanceToStart, distanceToEnd;

    public ScoredNode(byte orientation, short cell) {
        super(orientation, cell);
    }

    public int distanceToStart() {
        return distanceToStart;
    }

    public void setDistanceToStart(int distanceToStart) {
        this.distanceToStart = distanceToStart;
    }

    public int distanceToEnd() {
        return distanceToEnd;
    }

    public void setDistanceToEnd(int distanceToEnd) {
        this.distanceToEnd = distanceToEnd;
    }

    public int sum() {
        return distanceToStart + distanceToEnd;
    }

    @Override
    public int compareTo(ScoredNode o) {
        return this.distanceToEnd - o.distanceToEnd;
    }
}
