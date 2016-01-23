package koh.game.paths;


import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Path extends ArrayList<Node> {

    public Path(){
        super();
    }

    public Path(Collection<Node> c){
        super(c);
    }

    public Node first() {
        return get(0);
    }

    public Node last() {
        return get(size() - 1);
    }

    public boolean contains(short cellId) {
        for (Node node : this) {
            if (node.getCell() == cellId) return true;
        }
        return false;
    }

    public long estimateTimeOn() {
        return Cells.estimateTime(this);
    }


    public void deletePoint(int index, int deleteCount, ArrayList <Node> nodes) /*-1*/ {
        if (deleteCount <= 0) {
            nodes.remove(index);
            //this._aPath.splice(index);
        } else {
            int i = index + deleteCount;
            while (i >= index) {
                nodes.remove(i);
                i--;
            }
            //this._aPath.splice(index, deleteCount);
        }
    }


    private ArrayList <Node> getCompress(){
        /*int elem = 0;
        try {
            if (size() > 0) {
                elem = (copies.size() - 1);
                while (elem > 0) {
                    if (copies.get(elem).getOrientation() == copies.get((elem - 1)).getOrientation()) {
                        this.deletePoint(elem, 1, copies);
                        elem--;
                    } else {
                        elem--;
                    }
                }
            }
        }*/

        return this;
    }

    public Node getLastStep() {
        return this.get(size() < 2 ? 0 : size() - 2);
    }

    public short[] encode(){
        //final ArrayList<Node> cells = this.getCompress();

        final short[] encodedPath = new short[this.size()];
        byte lastOrientation;
            for(int i = 0 ; i < encodedPath.length; i++){
            lastOrientation = this.get(i).getOrientation();
            encodedPath[i] = (short) (((lastOrientation & 7) << 12) | (this.get(i).getCell() & 4095));
        }
        return encodedPath;
    }


}
