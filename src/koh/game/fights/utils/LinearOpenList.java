package koh.game.fights.utils;

import koh.game.entities.environments.DofusCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Melancholia on 1/26/16.
 */
public class LinearOpenList implements IOpenList {

    private final List<DofusCell> m_cells = new ArrayList<>();
    private final Comparator<DofusCell> m_comparer;


    public LinearOpenList(Comparator<DofusCell> comparer)
    {
        m_comparer = comparer;
    }

    @Override
    public int getCount() {
        return m_cells.size();
    }

    @Override
    public void push(DofusCell cell) {
        m_cells.add(cell);
    }

    @Override
    public DofusCell pop() {
        if (m_cells.size() == 0)
            throw new Error("LinearOpenList is empty");

        DofusCell bestCell = m_cells.get(0);
        for (int i = 1; i < m_cells.size(); i++)
        {
            // bestCell has a greater cost than the other cell
            if (m_comparer.compare(bestCell, m_cells.get(i)) >= 0)
                bestCell = m_cells.get(i);
        }

        m_cells.remove(bestCell);

        return bestCell;
    }
}
