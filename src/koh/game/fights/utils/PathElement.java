package koh.game.fights.utils;

import koh.game.entities.environments.DofusCell;

/**
 * Created by Melancholia on 1/26/16.
 */
public class PathElement {

    public PathElement(DofusCell cell, byte direction)
    {
        this.cell = cell;
        this.direction = direction;
    }

    public final DofusCell cell;
    public final byte direction;
}
