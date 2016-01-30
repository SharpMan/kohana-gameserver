package koh.game.fights.utils;

import koh.game.entities.environments.DofusCell;

/**
 * Created by Melancholia on 1/26/16.
 */
public interface IOpenList {

    int getCount();

    void push(DofusCell cell);
    DofusCell pop();



}
