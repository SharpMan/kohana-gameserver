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
    
    public static Map<Byte, ChatChannel> Channels = new HashMap<Byte, ChatChannel>() {
        {
            put(CHANNEL_ADMIN, new ChatChannel());
            put(CHANNEL_SALES, new ChatChannel());
            put(CHANNEL_SEEK, new ChatChannel());
            put(CHANNEL_NOOB, new ChatChannel());
            put(CHANNEL_ADS, new ChatChannel());
        }
    };

    public static void Register(WorldClient Client) {
        Channels.forEach((k, v) -> {
            {
                if (k == CHANNEL_ADMIN) {
                    if (Client.getAccount().Right > 0) {
                        v.registerPlayer(Client.Character);
                    }
                } else {
                    v.registerPlayer(Client.Character);
                }
            }
        });
    }

    public static void UnRegister(WorldClient Client) {
        Channels.forEach((k, v) -> {
            {
                if (k == CHANNEL_ADMIN) {
                    if (Client.getAccount().Right > 0) {
                        v.unregisterPlayer(Client.Character);
                    }
                } else {
                    v.unregisterPlayer(Client.Character);
                }
            }
        });
    }

}
