package koh.game.entities.environments;

import koh.protocol.client.BufUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class Layer {

    public DofusMap Map;
    public int LayerId;
    public short[] Cells;

    public Layer(DofusMap map, int l, short[] d) {
        this.Map = map;
        this.LayerId = l;
        this.Cells = d;
    }

    public Layer(DofusMap map, IoBuffer buf) {
        this.Map = map;
        this.LayerId = buf.getInt();
        this.Cells = BufUtils.readShortArray(buf);
    }

}
