package koh.game.network.websocket.message;

import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import lombok.ToString;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 7/27/16.
 */
@ToString
public class PlayerOrnamentTitle implements IMessage {

    @Getter
    private int player, ornament, title;

    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.GET_INFOS;
    }

    @Override
    public void serialize(JSONArray buf) {

    }

    @Override
    public void deserialize(JSONArray buf) {
        this.player = ((Long) buf.get(1)).intValue();
        this.ornament = ((Long) buf.get(2)).intValue();
        this.title = ((Long) buf.get(3)).intValue();
    }
}