package koh.game.dao.api;

import koh.game.entities.guilds.Guild;
import koh.game.entities.guilds.GuildEntity;
import koh.patterns.services.api.Service;
import koh.protocol.types.game.guild.GuildEmblem;

import java.util.stream.Stream;

public abstract class GuildDAO implements Service {

    public abstract void update(GuildEntity entity);

    public abstract void remove(GuildEntity Item);

    public abstract void insert(Guild guild);

    public abstract int nextId();

    public abstract boolean alreadyTakenEmblem(GuildEmblem emblem);

    public boolean alreadyTakenName(String name) {
        return get(name) != null;
    }

    public abstract Stream<Guild> asStream();

    public abstract Guild get(int id);

    public abstract Guild get(String name);

    public abstract Guild getForPlayer(int playerId);

}
