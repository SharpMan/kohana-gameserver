package koh.game.dao.api;

import koh.game.entities.environments.Area;
import koh.game.entities.environments.SubArea;
import koh.game.entities.environments.SuperArea;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class AreaDAO implements Service {

    public abstract SuperArea getSuperArea(int id);
    public abstract Area getArea(int id);
    public abstract SubArea getSubArea(int id);

}
