package koh.game.network.websocket.message;

import koh.game.Main;
import koh.game.network.websocket.HandlerAttribute;
import koh.protocol.client.enums.CommPacketEnum;
import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 3/4/16.
 */
public class GameHandler {

    @HandlerAttribute(ID = CommPacketEnum.SEEK_TOTAL)
    public static void handleSeekTotalMessage(WebSocket conn, UptimeSeekInfos message){
        final PlayersTotalMessage packet = new PlayersTotalMessage(Main.getWorldServer().size());
        final JSONArray buffer = new JSONArray();
        packet.serialize(buffer);
        conn.send(buffer.toJSONString());
    }

}
