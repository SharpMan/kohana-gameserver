package koh.game.network.websocket.message;

import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.mina.core.buffer.IoBuffer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by Melancholia on 2/23/16.
 */
public class TokenMessage implements IMessage {

    @Getter @Setter
    private int tokens, playerId;

    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.SEND_TOKEN;
    }

    @Override
    public void serialize(JSONArray buf) {
        buf.add(playerId);
        buf.add(tokens);

    }

    @Override
    public void deserialize(JSONArray buf) {
        this.playerId = ((Long) buf.get(1)).intValue();
        this.tokens = ((Long)  buf.get(2)).intValue();
    }
}
