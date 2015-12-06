package koh.game.dao.api;

import koh.d2o.entities.EmblemSymbols;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 11/29/15.
 */
public abstract class GuildEmblemDAO implements Service {

    public abstract EmblemSymbols get(int id);

}
