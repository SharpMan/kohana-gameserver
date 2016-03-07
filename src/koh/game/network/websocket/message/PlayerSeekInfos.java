package koh.game.network.websocket.message;

import koh.game.network.websocket.IMessage;
import koh.protocol.client.enums.CommPacketEnum;
import lombok.Getter;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 2/27/16.
 */
public class PlayerSeekInfos implements IMessage {

    @Getter
    private int account;

    @Override
    public CommPacketEnum getMessageId() {
        return CommPacketEnum.GET_INFOS;
    }

    @Override
    public void serialize(JSONArray buf) {

    }

    @Override
    public void deserialize(JSONArray buf) {
        this.account = ((Long) buf.get(1)).intValue();
    }
}
