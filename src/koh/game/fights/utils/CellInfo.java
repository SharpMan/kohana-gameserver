package koh.game.fights.utils;

import lombok.Getter;

import java.util.ArrayList;

/**
 * Created by Melancholia on 1/13/16.
 */
public class CellInfo {

    @Getter
    private double heuristic;
    @Getter
    private ArrayList<CellInfo> parent;
    @Getter
    private boolean opened,closed;
    @Getter
    private int movementCost;

    public  CellInfo(double pHeuristic, ArrayList<CellInfo> pParent, boolean pOpened, boolean pClosed)
    {
        this.heuristic = pHeuristic;
        this.parent = pParent;
        this.opened = pOpened;
        this.closed = pClosed;
    }
}
