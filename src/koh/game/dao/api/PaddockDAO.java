package koh.game.dao.api;

import koh.game.entities.environments.Paddock;
import koh.patterns.services.api.Service;
import koh.protocol.client.BufUtils;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.paddock.MountInformationsForPaddock;
import koh.protocol.types.game.paddock.PaddockItem;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class PaddockDAO implements Service {

    public abstract boolean update(Paddock Item, String[] Columns);

    public static IoBuffer SerializeGuildInformations(GuildInformations m) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);
        buff.putInt(m.guildId);
        BufUtils.writeUTF(buff, m.guildName);
        buff.putInt(m.guildEmblem.symbolShape);
        buff.putInt(m.guildEmblem.symbolColor);
        buff.put(m.guildEmblem.backgroundShape);
        buff.putInt(m.guildEmblem.backgroundColor);
        buff.flip();
        return buff;
    }

    public static IoBuffer SerializeMountsInformations(MountInformationsForPaddock[] M) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(M.length);
        for (MountInformationsForPaddock e : M) {
            buff.put(e.modelId);
            BufUtils.writeUTF(buff, e.name);
            BufUtils.writeUTF(buff, e.ownerName);
        }

        buff.flip();
        return buff;
    }

    public static IoBuffer SerializeItemsInformations(PaddockItem[] Items) {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(Items.length);
        for (PaddockItem e : Items) {
            buff.putInt(e.cellId);
            buff.putInt(e.objectGID);
            buff.putShort(e.durability.durability);
            buff.putShort(e.durability.durabilityMax);
        }

        buff.flip();
        return buff;
    }
}

