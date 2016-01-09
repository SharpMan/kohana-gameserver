package koh.game.entities.environments;

import koh.protocol.client.BufUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class Layer {

    private DofusMap map;
    private int layerId;
    private short[] cells;

    public Layer(DofusMap map, int l, short[] d) {
        this.map = map;
        this.layerId = l;
        this.cells = d;
    }

    public Layer(DofusMap map, IoBuffer buf) {
        this.map = map;
        this.layerId = buf.getInt();
        this.cells = BufUtils.readShortArray(buf);
    }

}
