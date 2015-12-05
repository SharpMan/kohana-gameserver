package koh.game.dao.api;

import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.DofusZaap;
import koh.game.entities.environments.MapPosition;
import koh.patterns.services.api.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class MapDAO implements Service {

    public abstract DofusMap findMapByPos(int X, int Y);

    public abstract MapPosition[] getSubAreaOfPos(int X, int Y);

    public abstract DofusMap findMapByPos(int X, int Y, int subArea);

    public abstract DofusMap findTemplate(int id);

    public abstract DofusZaap getZaap(int id);

    public abstract int getZaapsLength();

    public abstract Stream<Map.Entry<Integer,DofusZaap>> getZaapsNot(int id);

    public abstract ArrayList<DofusZaap> getSubway(int id);

    public abstract DofusZaap findSubWay(int sub, int mapid);
}
