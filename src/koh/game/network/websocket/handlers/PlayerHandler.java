package koh.game.network.websocket.handlers;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemTemplate;
import koh.game.network.websocket.HandlerAttribute;
import koh.game.network.websocket.message.*;
import koh.protocol.client.enums.CommPacketEnum;
import koh.protocol.client.enums.EffectGenerationType;
import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;

import java.util.stream.Stream;

/**
 * Created by Melancholia on 2/23/16.
 */
public class PlayerHandler {
    
    static final ItemTemplate TOKEN = DAO.getItemTemplates().getTemplate(13470);

    @HandlerAttribute(ID = CommPacketEnum.GET_INFOS)
    public static void handleGetInfosMessage(WebSocket conn, PlayerSeekInfos message){
        final PlayerListMessage packet = new PlayerListMessage(DAO.getPlayers().getByAccount(message.getAccount()));
        final JSONArray buffer = new JSONArray();
        packet.serialize(buffer);
        conn.send(buffer.toJSONString());
    }

    @HandlerAttribute(ID = CommPacketEnum.SEND_TOKEN)
    public static void handleTokenMessage(WebSocket conn, TokenMessage message) {
        final Player perso = DAO.getPlayers().getCharacter(message.getPlayerId());
        if (perso == null || !perso.isInWorld()) {
            conn.send("NO");
        } else {
            final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), TOKEN.getId(), 63, perso.getID(), message.getTokens(), EffectHelper.generateIntegerEffect(TOKEN.getPossibleEffects(), EffectGenerationType.NORMAL, TOKEN.isWeapon()));
            item.setNeedInsert(true);
            if (perso.getInventoryCache().add(item, true))
                conn.send("OK");
            else
                conn.send("NO");
        }
    }


}
