package koh.game.dao.api;

import koh.game.entities.actors.Npc;
import koh.game.entities.actors.npc.NpcMessage;
import koh.game.entities.actors.npc.NpcReply;
import koh.game.entities.actors.npc.NpcTemplate;
import koh.patterns.services.api.Service;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class NpcDAO implements Service {
    public abstract ArrayList<Npc> forMap(int mapid);

    public abstract NpcTemplate findTemplate(int id);

    public abstract NpcMessage findMessage(int id);

    public abstract Stream<NpcReply> repliesAsStream();
}
