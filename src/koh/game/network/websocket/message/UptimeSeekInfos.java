package koh.game.network.websocket.message;

import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 3/4/16.
 */
public class UptimeSeekInfos implements IMessage {


    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.SEEK_TOTAL;
    }

    @Override
    public void serialize(JSONArray buf) {

    }

    @Override
    public void deserialize(JSONArray buf) {

    }
}
