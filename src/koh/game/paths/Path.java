package koh.game.paths;


import java.util.ArrayList;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Path extends ArrayList<Node> {

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
        ArrayList <Node> copies = new ArrayList<>(this.size());
        copies.addAll(this);
        /*int elem = 0;
        try {
            if (size() > 0) {
                elem = (copies.size() - 1);
                while (elem > 0) {
                    if (copies.get(elem).getOrientation() == copies.get((elem - 1)).getOrientation()) {
                        this.deletePoint(elem, -1, copies);
                        elem--;
                    } else {
                        elem--;
                    }
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
        }*/
        return copies;
    }

    public short[] encode(){
        final ArrayList<Node> cells = this.getCompress();
        final short[] encodedPath = new short[cells.size()];
        byte lastOrientation;
        for(int i = 0 ; i < encodedPath.length; i++){
            lastOrientation = cells.get(i).getOrientation();
            encodedPath[i] = (short) (((lastOrientation & 7) << 12) | (cells.get(i).getCell() & 4095));
        }
        return encodedPath;
    }


}
