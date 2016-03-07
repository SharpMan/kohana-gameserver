package koh.game.network.websocket.message;

import koh.game.entities.actors.Player;
import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import org.json.simple.JSONArray;

import java.util.stream.Stream;

/**
 * Created by Melancholia on 2/27/16.
 */
public class PlayerListMessage implements IMessage {

   private Stream<Player> players;

    public PlayerListMessage(Stream<Player> players){
        this.players = players;
    }

    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.SEND_INFOS;
    }

    @Override
    public void serialize(JSONArray buf) {
       players.forEach(pl -> {
           buf.add(pl.getID());
           buf.add(pl.getNickName());
       });
    }

    @Override
    public void deserialize(JSONArray buf) {

    }
}
