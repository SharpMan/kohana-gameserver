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
import koh.protocol.messages.messages.game.tinsel.OrnamentGainedMessage;
import koh.protocol.messages.messages.game.tinsel.TitleGainedMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;

/**
 * Created by Melancholia on 2/23/16.
 */
@Log4j2
public class PlayerHandler {
    
    public static final ItemTemplate TOKEN = DAO.getItemTemplates().getTemplate(13470);

    @HandlerAttribute(ID = CommPacketEnum.SEND_ORNAMENT)
    public static void handleSendOrnamentTitle(WebSocket conn, PlayerOrnamentTitle message){
        final Player perso = DAO.getPlayers().getCharacter(message.getPlayer());
        if (perso == null) {
            conn.send("NO");
            log.error("Disconnected player {}", message.toString());
        }else{
            if(!ArrayUtils.contains(perso.getTitles(), message.getTitle())){
                perso.setTitles(ArrayUtils.add(perso.getTitles(), message.getTitle()));
                perso.send(new TitleGainedMessage(message.getTitle()));
            }
            if(!ArrayUtils.contains(perso.getOrnaments(), message.getOrnament())){
                perso.setTitles(ArrayUtils.add(perso.getOrnaments(), message.getOrnament()));
                perso.send(new OrnamentGainedMessage((short) message.getOrnament()));
            }
            conn.send("OK");

        }
    }

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
            if (perso.getInventoryCache().add(item, true)){
                item.setNeedInsert(true);
            }
            conn.send("OK");
        }
    }


}
