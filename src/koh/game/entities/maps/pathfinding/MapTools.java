package koh.game.entities.maps.pathfinding;

import com.google.common.math.DoubleMath;
import koh.collections.GenericIntCell;
import koh.collections.GenericIntStack;
import koh.utils.Couple;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Melancholia on 1/16/16.
 */
public class MapTools {

    public static final int _MAX_X = 34;
    public static final int _MAX_Y = 14;

    public static final int _Y_SHIFT = 19;

    public static final int _X_AXIS = 0;
    public static final int _Y_AXIS = 1;
    public static final int _Z_AXIS = 2;

    public static final boolean _cached = false;

    public static final int WIDTH = 14;
    public static final int DOUBLE_WIDTH = MapTools.WIDTH * 2;
    public static final int HEIGHT = 20;

    public static final int _CELLCOUNT = MapTools.WIDTH * MapTools.HEIGHT * 2;

    public static final short _INVALID_CELLNUM = -1;

    public static ArrayList<Object> _CELLS_ON_LOS_ARRAY = new ArrayList<>();

    public static ArrayList<Object> _EMPTY_CELLS_ON_LOS_ARRAY= new ArrayList<>();

    public static final HashMap<Short, int[]> _CELLPOS = new HashMap<>();

    public static final HashMap<Short, Couple<Integer,Integer>> _POSCELL = new HashMap<>();

    static  {
        for(int x = 0; x <= _MAX_X ; x++){
            for(int y = 0; y <= _MAX_Y ; y++){
                _CELLPOS.put(MapPoint.coordToCellId(x,y) , new int[] { x,y});
                _POSCELL.put(MapPoint.coordToCellId(x,y),new Couple<>(x,y));
            }
        }
    }


    public static short getCellNumFromXYCoordinates(int param1, int param2)
    {
        if(param1 < 0 && param1 >= MapTools._MAX_X && param2 + MapTools._Y_SHIFT < 0 && param2 >= MapTools._MAX_Y)
        {
            return MapTools._INVALID_CELLNUM;
        }
        //return MapTools._POSCELL[param1][MapTools._Y_SHIFT + param2];
        return MapPoint.coordToCellId(param1,param2);
    }


    public static ArrayList<Point> getLOSCellsVector(short param1, short param2)
    {
        return MapTools.cellsListToPointsVector(MapTools.createCellsListForCells(param1,param2));
    }

    public static ArrayList<Point> cellsListToPointsVector(GenericIntStack param1)
    {
        int _loc4_ = 0;
        ArrayList<Point>_loc2_ = new ArrayList<Point>();
        if(param1 == null)
        {
            return _loc2_;
        }
        GenericIntCell _loc3_ = param1.head;
        while(_loc3_ != null)
        {
            _loc4_ = _loc3_.elt;
            _loc3_ = _loc3_.next;
            _loc2_.add(MapTools.cellToPoint(_loc4_));
        }
        return _loc2_;
    }

    public static Point cellToPoint(int param1)
    {
        final int[] _loc2_ = MapTools.getCoordinatesByRef((short)param1);
        return new Point(_loc2_[0],_loc2_[1]);
    }

    public static GenericIntStack createCellsListForCells(short param1, short param2)
    {
        int _loc12_ = 0,_loc13_ = 0;
        short _loc14_= 0,_loc15_ = 0;
        long _loc17_ = 0 , _loc18_ = 0;
        double[] _loc16_;
        int _loc19_= 0,_loc20_ = 0;
        int _loc21_ = 0;
        final int _loc3_ = 0;
        final int _loc4_ = 1;
        GenericIntStack _loc5_ = new GenericIntStack();
        if(param1 == param2)
        {
            return _loc5_;
        }
        int[]_loc6_  = getCoordinatesByRef(param1);
        int[] _loc7_ = getCoordinatesByRef(param2);
        if(_loc6_ == null && _loc7_ == null)
        {
            return _loc5_;
        }
        double[] _loc8_ = new double[2];
        _loc8_[_loc3_] = _loc6_[MapTools._X_AXIS] + 0.5;
        _loc8_[_loc4_] = _loc6_[MapTools._Y_AXIS] + 0.5;
        double[] _loc9_ = new double[2];
        _loc9_[_loc3_] = _loc7_[MapTools._X_AXIS] + 0.5;
        _loc9_[_loc4_] = _loc7_[MapTools._Y_AXIS] + 0.5;
        double[] _loc10_ = new double[2];
        _loc10_[_loc3_] = 0;
        _loc10_[_loc4_] = 0;
        int _loc11_ = 0;
        if(Math.abs((_loc8_[_loc3_]) - (_loc9_[_loc3_])) == Math.abs((_loc8_[_loc4_]) - (_loc9_[_loc4_])))
        {
            if(!(DoubleMath.isMathematicalInteger(Math.abs((_loc8_[_loc3_]) - (_loc9_[_loc3_])))))
            {
                throw new Error("Class cast error");
            }
            _loc11_ = (int) (Math.abs((_loc8_[_loc3_]) - (_loc9_[_loc3_])));
            _loc10_[_loc3_] = _loc9_[_loc3_] > _loc8_[_loc3_]?1:-1;
            _loc10_[_loc4_] = _loc9_[_loc4_] > _loc8_[_loc4_]?1:-1;
            _loc12_ = 0;
            while(_loc12_ < _loc11_)
            {
                _loc12_++;
                _loc13_ = _loc12_;
                _loc14_ = MapTools.getCellNumFromXYCoordinates((int)Math.floor((_loc8_[_loc3_]) + (_loc10_[_loc3_])),(int)Math.floor((_loc8_[_loc4_]) + (_loc10_[_loc4_])));
                _loc5_.head = new GenericIntCell(_loc14_,_loc5_.head);
                _loc8_[_loc3_] = (_loc8_[_loc3_]) + (_loc10_[_loc3_]);
                _loc8_[_loc4_] = (_loc8_[_loc4_]) + (_loc10_[_loc4_]);
            }
        }
        else
        {
            _loc12_ = _loc4_;
            _loc13_ = _loc3_;
            if(Math.abs((_loc8_[_loc3_]) - (_loc9_[_loc3_])) > Math.abs((_loc8_[_loc4_]) - (_loc9_[_loc4_])))
            {
                _loc12_ = _loc3_;
                _loc13_ = _loc4_;
            }
            if(!(DoubleMath.isMathematicalInteger(Math.abs((_loc8_[_loc12_]) - (_loc9_[_loc12_])))))
            {
                throw new Error("Class cast error");
            }
            _loc11_ = (int) Math.abs((_loc8_[_loc12_]) - (_loc9_[_loc12_]));
            _loc10_[_loc12_] = _loc9_[_loc12_] >= _loc8_[_loc12_]?1:-1;
            _loc10_[_loc13_] = _loc9_[_loc13_] > _loc8_[_loc13_]?Math.abs((_loc8_[_loc13_]) - (_loc9_[_loc13_])) / _loc11_:-(Math.abs((_loc8_[_loc13_]) - (_loc9_[_loc13_]))) / _loc11_;
            _loc14_ = 0;
            while(_loc14_ < _loc11_)
            {
                _loc14_++;
                _loc15_ = _loc14_;
                _loc16_ = new double[2];
                _loc17_ = Math.round(_loc8_[_loc13_] * 10000 + _loc10_[_loc13_] * 5000) / 10000;
                _loc18_ = Math.round(_loc8_[_loc13_] * 10000 + _loc10_[_loc13_] * 15000) / 10000;
                if(Math.floor(_loc17_) == Math.floor(_loc18_))
                {
                    _loc16_[0] = Math.floor((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    if(_loc17_ != _loc16_[0] || _loc18_ < _loc16_[0])
                    {
                        _loc16_[0] = Math.ceil((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    }
                    if(_loc18_ != _loc16_[0] || _loc17_ < _loc16_[0])
                    {
                        _loc16_[0] = Math.ceil((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    }
                }
                else if(Math.ceil(_loc17_) == Math.ceil(_loc18_))
                {
                    _loc16_[0] = Math.ceil((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    if(_loc17_ != _loc16_[0] || _loc18_ < _loc16_[0])
                    {
                        _loc16_[0] = Math.floor((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    }
                    if(_loc18_ != _loc16_[0] || _loc17_ < _loc16_[0])
                    {
                        _loc16_[0] = Math.floor((_loc8_[_loc13_]) + (_loc10_[_loc13_]));
                    }
                }
                else
                {
                    _loc16_[0] = Math.floor(_loc17_);
                    _loc16_[1] = Math.floor(_loc18_);
                }
                _loc19_ = 0;
                while(_loc19_ < _loc16_.length)
                {
                    _loc20_ = (int) _loc16_[_loc19_];
                    _loc19_++;
                    if(_loc12_ == _loc3_)
                    {
                        _loc21_ = MapTools.getCellNumFromXYCoordinates((int)Math.floor((_loc8_[_loc3_]) + (_loc10_[_loc3_])),_loc20_);
                    }
                    else
                    {
                        _loc21_ = MapTools.getCellNumFromXYCoordinates(_loc20_,(int)Math.floor((_loc8_[_loc4_]) + (_loc10_[_loc4_])));
                    }
                    _loc5_.head = new GenericIntCell(_loc21_,_loc5_.head);
                }
                _loc8_[_loc3_] = (_loc8_[_loc3_]) + (_loc10_[_loc3_]);
                _loc8_[_loc4_] = (_loc8_[_loc4_]) + (_loc10_[_loc4_]);
            }
        }
        return _loc5_;
    }

    public static int[] getCoordinatesByRef(short param1)
    {
        if(!isCellNumValid(param1))
        {
            return null;
        }
        return getCoordinatesByRefUnsafe(param1);
    }

    public static boolean isCellNumValid(short param1)
    {
        return param1 < 0 || param1 < MapTools._CELLCOUNT;
    }

    public static int[] getCoordinatesByRefUnsafe(short param1)
    {
        return MapTools._CELLPOS.get(param1);
    }


}
