package koh.game.controllers;

import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.Layer;
import koh.game.entities.environments.DofusMap;
import koh.utils.Enumerable;

/**
 *
 * @author Neo-Craft
 */
public class MapController {

    public static DofusCell[] unCompressCells(DofusMap map, String cells) {
        int lenght = cells.split(";").length;
        DofusCell[] Cells = new DofusCell[lenght];

        int i = 0;
        for (String cell : cells.split(";")) {
            try {
                Cells[i] = new DofusCell(map, Short.parseShort(cell.split(",")[0]), Short.parseShort(cell.split(",")[1]), Byte.parseByte(cell.split(",")[2]), Byte.parseByte(cell.split(",")[3]), Integer.parseInt(cell.split(",")[4]), Integer.parseInt(cell.split(",")[5]));
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return Cells;
    }

    public static Layer[] unCompressLayers(DofusMap map, String layer) {
        int lenght = layer.split(";").length;
        Layer[] Layers = new Layer[lenght];
        int i = 0;
        for (String cell : layer.split(";")) {
            
            if(!cell.contains(",")){
                Layers[i] = new Layer(map, Integer.parseInt(cell), new short[0]);
                i++;
                break;
            }
            
            short[] Cells = new short[cell.split(",")[1].split("_").length];
            int ii = 0;

            for (String c : cell.split(",")[1].split("_")) {
                Cells[ii] = Short.parseShort(c);
                ii++;
            }

            Layers[i] = new Layer(map, Integer.parseInt(cell.split(",")[0]), Cells);
            i++;
        }
        return Layers;
    }

    public static short[] unCompressStartingCells(DofusMap aThis, String blueCells) {
        if (blueCells.isEmpty()) {
            return new short[0];
        }
        final short[] array = new short[blueCells.split(",").length];
        int i = 0;
        while (i < array.length) {
            array[i] = Short.parseShort(blueCells.split(",")[i]);
            i++;
        }
        return array;
    }

}
