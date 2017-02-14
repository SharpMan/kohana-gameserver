package koh.game.entities.command;

import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.roleplay.emote.EmoteListMessage;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;

/**
 * Created by Melancholia on 1/30/17.
 */
public class AddEmoteCommand implements PlayerCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        final byte em = Byte.parseByte(args[0]);
        client.getCharacter().setEmotes(ArrayUtils.add(client.getCharacter().getEmotes(), em));
        client.send(new EmoteListMessage(client.getCharacter().getEmotes()));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 2;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
