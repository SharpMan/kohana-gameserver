package koh.game.network.websocket.message;

import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 3/4/16.
 */
public class PlayersTotalMessage  implements IMessage {

    public PlayersTotalMessage(int number){
        this.number = number;
    }

    @Getter
    private int number;

    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.SEND_INFOS_TOTAL_PLAYERS;
    }

    @Override
    public void serialize(JSONArray buf) {
        buf.add(number);
    }

    @Override
    public void deserialize(JSONArray buf) {

    }
}
