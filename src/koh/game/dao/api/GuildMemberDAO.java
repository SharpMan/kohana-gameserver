package koh.game.dao.api;

import koh.game.entities.guilds.Guild;
import koh.game.entities.guilds.GuildMember;
import koh.patterns.services.api.Service;

public abstract class GuildMemberDAO implements Service {

    public abstract void update(GuildMember member);

    public abstract Guild getForPlayer(int playerId);

    public abstract GuildMember get(int playerId);

    public abstract void insert(GuildMember member);

    public abstract void delete(GuildMember member);

}
