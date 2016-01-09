package koh.game.network;

import java.util.HashMap;
import java.util.Map;
import koh.game.entities.environments.IWorldEventObserver;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.*;

/**
 *
 * @author Neo-Craft
 */
public class ChatChannel extends IWorldEventObserver {
    
    public static final Map<Byte, ChatChannel> CHANNELS = new HashMap<Byte, ChatChannel>() {
        {
            put(CHANNEL_ADMIN, new ChatChannel());
            put(CHANNEL_SALES, new ChatChannel());
            put(CHANNEL_SEEK, new ChatChannel());
            put(CHANNEL_NOOB, new ChatChannel());
            put(CHANNEL_ADS, new ChatChannel());
        }
    };

    public static void register(WorldClient client) {
        CHANNELS.forEach((k, v) -> {
            {
                if (k == CHANNEL_ADMIN) {
                    if (client.getAccount().right > 0) {
                        v.registerPlayer(client.getCharacter());
                    }
                } else {
                    v.registerPlayer(client.getCharacter());
                }
            }
        });
    }

    public static void unRegister(WorldClient client) {
        CHANNELS.forEach((k, v) -> {
            {
                if (k == CHANNEL_ADMIN) {
                    if (client.getAccount().right > 0) {
                        v.unregisterPlayer(client.getCharacter());
                    }
                } else {
                    v.unregisterPlayer(client.getCharacter());
                }
            }
        });
    }

}
