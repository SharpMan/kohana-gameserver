package koh.game.fights.utils;

import koh.game.entities.environments.DofusCell;

import java.util.PriorityQueue;

/**
 * Created by Melancholia on 1/26/16.
 */
public class LogOpenList extends PriorityQueue implements IOpenList{


    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void push(DofusCell cell) {
        this.add(cell);
    }

    @Override
    public DofusCell pop() {
        return null;
    }
}
