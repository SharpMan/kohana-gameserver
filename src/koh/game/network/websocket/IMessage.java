package koh.game.network.websocket;

import koh.protocol.client.enums.CommPacketEnum;
import org.apache.mina.core.buffer.IoBuffer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by Melancholia on 2/23/16.
 */
public interface IMessage {

    CommPacketEnum getMessageId();

    void serialize(JSONArray buf);

    void deserialize(JSONArray buf);
}
