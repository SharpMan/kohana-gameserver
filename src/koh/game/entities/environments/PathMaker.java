package koh.game.entities.environments;

import com.google.common.collect.Lists;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Melancholia on 1/18/16.
 */
public class Pathmaker {
    private static final Logger logger = LogManager.getLogger(Pathmaker.class);

    private List<Short> openlist = new ArrayList<>();
    private List<Short> closelist = new ArrayList<>();
    private Map<Short, Node> myNodes = new HashMap<>();

    private boolean fourDir;
    private boolean isFight;

    private int nombreDePM;

    private class Node
    {
        @Getter @Setter
        private short parent;
        @Getter @Setter
        private double F,G,H;
    }

    private void loadSprites(Fight fight)
    {
        closelist.addAll(fight.getFightCells().values()
                .stream()
                .filter(cell -> !cell.canWalk())
                .map((Function<FightCell, Short>) FightCell::getId)
                .collect(Collectors.toList()));
    }

    public MovementPath pathing(DofusMap Map, short nCellBegin, short nCellEnd, boolean fourDir, int numberPM, boolean isInFight, Fight myFight)
    {

        try
        {
            loadSprites(myFight);
            if (closelist.contains(nCellBegin))
                closelist.remove(closelist.indexOf(nCellBegin)); //IndexOF useless
            if (closelist.contains(nCellEnd))
                closelist.remove(closelist.indexOf(nCellEnd));

            this.fourDir = fourDir;
            this.isFight = isInFight;

            nombreDePM = numberPM;

            return findpath(Map, nCellBegin, nCellEnd);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private MovementPath findpath(DofusMap map, short cell1, short cell2)
    {
        int heuristicEstimate = 20;
        short current = 0;
        int i = 0;

        openlist.add(cell1);

        Node NodeBegin = new Node();
        NodeBegin.setG(0);
        NodeBegin.setH(Pathfunction.goalDistanceNoSqrt(map, current, cell2) * heuristicEstimate);
        NodeBegin.setF(NodeBegin.G + NodeBegin.H);
        NodeBegin.setParent(cell1);
        this.myNodes.put(cell1, NodeBegin);


        double test1 = Pathfunction.goalDistanceScore(map, cell1, cell2);
        double test2 = Pathfunction.goalDistanceEstimate(map, cell1, cell2);
        double test3 = Pathfunction.goalDistanceNoSqrt(map, cell1, cell2);

        current = cell1;

        while (!(openlist.contains(cell2)))
        {
            if (i++ > 1000)
                return null;

            if (current == cell2)
                break; // TODO: might not be correct. Was : Exit Do
            closelist.add(current);
            if(openlist.contains(current))
                openlist.remove(openlist.indexOf(current));

            short cell = map.getBestCellBetween(current, cell2, closelist);
            if (cell != -1)
            {
                if (!closelist.contains(cell))
                {
                    if (openlist.contains(cell))
                    {
                        if (this.myNodes.get(cell).G >this.myNodes.get(current).G)
                        {
                            Node Node = this.myNodes.get(cell);
                            Node.parent = current;
                            Node.G =this.myNodes.get(current).G + 1;
                            Node.H = Pathfunction.goalDistanceScore(map, cell, cell2) * heuristicEstimate;
                            Node.F = Node.G + Node.H;
                        }
                    }
                    else
                    {
                        openlist.add(cell);
                        openlist.add(openlist.size() - 1, cell);
                        Node Node = new Node();
                        Node.G = this.myNodes.get(current).G + 1;
                        Node.H = Pathfunction.goalDistanceScore(map, cell, cell2) * heuristicEstimate;
                        Node.F = Node.G + Node.H;
                        Node.parent = current;
                        this.myNodes.put(cell, Node);
                    }
                }

                current = cell;
            }
        }

        return (getParent(map, cell1, cell2));
    }

    private MovementPath getParent(DofusMap Map, short cell1, short cell2)
    {
        short current = cell2;
        List<Short> pathCell = new ArrayList<>();
        pathCell.add(current);

        while (!(current == cell1))
        {
            pathCell.add(this.myNodes.get(current).parent);
            current = this.myNodes.get(current).parent;
        }

        return getPath(Map, pathCell);
    }

    private MovementPath getPath(DofusMap Map, List<Short> pathCell)
    {
        pathCell = Lists.reverse(pathCell);
        MovementPath Path = new MovementPath();
        short current;
        short child;
        int PMUsed = 0;
        if (pathCell.size() > 1)
            Path.addCell(pathCell.get(0), Pathfunction.getDirection(Map, pathCell.get(0), pathCell.get(1)));
        for (int i = 0; i <= pathCell.size() - 2; i++)
        {
            PMUsed++;
            if ((PMUsed > nombreDePM))
            {
                Path.setMovementLength(nombreDePM);
                return Path;
            }
            current = pathCell.get(i);
            child = pathCell.get(i +1);
            Path.addCell(child, Pathfunction.getDirection(Map, current, child));
        }

        Path.setMovementLength(PMUsed);

        return Path;
    }

    private int getFPoint()
    {
        double x = 9999;
        int cell = 0;

        for (short item : openlist)
        {
            if (!closelist.contains(item))
            {
                if (this.myNodes.get(item).F < x)
                {
                    x = this.myNodes.get(item).F;
                    cell = item;
                }
            }
        }

        return cell;
    }




}
