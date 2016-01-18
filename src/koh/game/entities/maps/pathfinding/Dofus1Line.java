package koh.game.entities.maps.pathfinding;

import koh.maths.Point3D;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Melancholia on 1/15/16.
 */
public class Dofus1Line {

    public static ArrayList<Point> getLine(int param1,int param2,int param3, int param4, int param5, int param6)
    {
        int _loc10_ = 0;
        Point _loc21_ = null;
        int _loc22_,_loc23_;
        double _loc24_,_loc25_,_loc26_,_loc27_,_loc28_,_loc29_,_loc30_,_loc31_;
        int _loc32_ = 0 ,_loc33_ = 0;
        ArrayList<Point> _loc7_ = new ArrayList<>();
        Point3D _loc8_ = new Point3D(param1,param2,param3);
        Point3D _loc9_ = new Point3D(param4,param5,param6);
        Point3D _loc11_ = new Point3D(_loc8_.getX() + 0.5,_loc8_.getY() + 0.5,_loc8_.getZ());
        Point3D _loc12_ = new Point3D(_loc9_.getX() + 0.5,_loc9_.getY() + 0.5,_loc9_.getZ());
        double _loc13_,_loc14_,_loc15_, _loc16_;
        boolean _loc17_ = _loc11_.getZ() > _loc12_.getZ();
        double[] _loc18_ = new double[0];
        double[] _loc19_ = new double[0];
        int _loc20_ = 0;
        if(Math.abs(_loc11_.getX() - _loc12_.getX()) == Math.abs(_loc11_.getY() - _loc12_.getY()))
        {
            _loc16_ = Math.abs(_loc11_.getX() - _loc12_.getX());
            _loc13_ = _loc12_.getX() > _loc11_.getX()?1:-1;
            _loc14_ = _loc12_.getY() > _loc11_.getY()?1:-1;
            _loc15_ = _loc16_ == 0?0:_loc17_?(_loc8_.getZ() - _loc9_.getZ()) / _loc16_:(_loc9_.getZ() - _loc8_.getZ()) / _loc16_;
            _loc20_ = 1;
        }
        else if(Math.abs(_loc11_.getX() - _loc12_.getX()) > Math.abs(_loc11_.getY() - _loc12_.getY()))
        {
            _loc16_ = Math.abs(_loc11_.getX() - _loc12_.getX());
            _loc13_ = _loc12_.getX() > _loc11_.getX()?1:-1;
            _loc14_ = _loc12_.getY() > _loc11_.getY()?Math.abs(_loc11_.getY() - _loc12_.getY()) == 0?0:Math.abs(_loc11_.getY() - _loc12_.getY()) / _loc16_:-Math.abs(_loc11_.getY() - _loc12_.getY()) / _loc16_;
            _loc14_ = _loc14_ * 100;
            _loc14_ = Math.ceil(_loc14_) / 100;
            _loc15_ = _loc16_ == 0?0:_loc17_?(_loc8_.getZ() - _loc9_.getZ()) / _loc16_:(_loc9_.getZ() - _loc8_.getZ()) / _loc16_;
            _loc20_ = 2;
        }
        else
        {
            _loc16_ = Math.abs(_loc11_.getY() - _loc12_.getY());
            _loc13_ = _loc12_.getX() > _loc11_.getX()?Math.abs(_loc11_.getX() - _loc12_.getX()) == 0?0:Math.abs(_loc11_.getX() - _loc12_.getX()) / _loc16_:-Math.abs(_loc11_.getX() - _loc12_.getX()) / _loc16_;
            _loc13_ = _loc13_ * 100;
            _loc13_ = Math.ceil(_loc13_) / 100;
            _loc14_ = _loc12_.getY() > _loc11_.getY()?1:-1;
            _loc15_ = _loc16_ == 0?0:_loc17_?(_loc8_.getZ() - _loc9_.getZ()) / _loc16_:(_loc9_.getZ() - _loc8_.getZ()) / _loc16_;
            _loc20_ = 3;
        }
        _loc10_ = 0;
        while(_loc10_ < _loc16_)
        {
            _loc22_ = (int)(3 + _loc16_ / 2);
            _loc23_ = (int)(97 - _loc16_ / 2);
            if(_loc20_ == 2)
            {
                _loc24_ = Math.ceil(_loc11_.getY() * 100 + _loc14_ * 50) / 100;
                _loc25_ = Math.floor(_loc11_.getY() * 100 + _loc14_ * 150) / 100;
                _loc26_ = Math.floor(Math.abs(Math.floor(_loc24_) * 100 - _loc24_ * 100)) / 100;
                _loc27_ = Math.ceil(Math.abs(Math.ceil(_loc25_) * 100 - _loc25_ * 100)) / 100;
                if(Math.floor(_loc24_) == Math.floor(_loc25_))
                {
                    _loc19_ = new double[]{Math.floor(_loc11_.getY() + _loc14_)};
                    if(_loc24_ == _loc19_[0] && _loc25_ < _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.ceil(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc24_ == _loc19_[0] && _loc25_ > _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.floor(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc25_ == _loc19_[0] && _loc24_ < _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.ceil(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc25_ == _loc19_[0] && _loc24_ > _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.floor(_loc11_.getY() + _loc14_)};
                    }
                }
                else if(Math.ceil(_loc24_) == Math.ceil(_loc25_))
                {
                    _loc19_ = new double[]{Math.ceil(_loc11_.getY() + _loc14_)};
                    if(_loc24_ == _loc19_[0] && _loc25_ < _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.floor(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc24_ == _loc19_[0] && _loc25_ > _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.ceil(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc25_ == _loc19_[0] && _loc24_ < _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.floor(_loc11_.getY() + _loc14_)};
                    }
                    else if(_loc25_ == _loc19_[0] && _loc24_ > _loc19_[0])
                    {
                        _loc19_ = new double[]{Math.ceil(_loc11_.getY() + _loc14_)};
                    }
                }
                else if((int)(_loc26_ * 100) <= _loc22_)
                {
                    _loc19_ = new double[]{Math.floor(_loc25_)};
                }
                else if((int)(_loc27_ * 100) >= _loc23_)
                {
                    _loc19_ = new double[]{Math.floor(_loc24_)};
                }
                else
                {
                    _loc19_ = new double[]{Math.floor(_loc24_),Math.floor(_loc25_)};
                }
            }
            else if(_loc20_ == 3)
            {
                _loc28_ = Math.ceil(_loc11_.getX() * 100 + _loc13_ * 50) / 100;
                _loc29_ = Math.floor(_loc11_.getX() * 100 + _loc13_ * 150) / 100;
                _loc30_ = Math.floor(Math.abs(Math.floor(_loc28_) * 100 - _loc28_ * 100)) / 100;
                _loc31_ = Math.ceil(Math.abs(Math.ceil(_loc29_) * 100 - _loc29_ * 100)) / 100;
                if(Math.floor(_loc28_) == Math.floor(_loc29_))
                {
                    _loc18_ = new double[]{Math.floor(_loc11_.getX() + _loc13_)};
                    if(_loc28_ == _loc18_[0] && _loc29_ < _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.ceil(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc28_ == _loc18_[0] && _loc29_ > _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.floor(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc29_ == _loc18_[0] && _loc28_ < _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.ceil(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc29_ == _loc18_[0] && _loc28_ > _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.floor(_loc11_.getX() + _loc13_)};
                    }
                }
                else if(Math.ceil(_loc28_) == Math.ceil(_loc29_))
                {
                    _loc18_ = new double[]{Math.ceil(_loc11_.getX() + _loc13_)};
                    if(_loc28_ == _loc18_[0] && _loc29_ < _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.floor(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc28_ == _loc18_[0] && _loc29_ > _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.ceil(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc29_ == _loc18_[0] && _loc28_ < _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.floor(_loc11_.getX() + _loc13_)};
                    }
                    else if(_loc29_ == _loc18_[0] && _loc28_ > _loc18_[0])
                    {
                        _loc18_ = new double[]{Math.ceil(_loc11_.getX() + _loc13_)};
                    }
                }
                else if((int)(_loc30_ * 100) <= _loc22_)
                {
                    _loc18_ = new double[]{Math.floor(_loc29_)};
                }
                else if((int)(_loc31_ * 100) >= _loc23_)
                {
                    _loc18_ = new double[]{Math.floor(_loc28_)};
                }
                else
                {
                    _loc18_ = new double[]{Math.floor(_loc28_),Math.floor(_loc29_)};
                }
            }
            if(_loc19_.length > 0)
            {
                _loc32_ = 0;
                while(_loc32_ < _loc19_.length)
                {
                    _loc21_ = new Point((int)Math.floor(_loc11_.getX() + _loc13_),(int)_loc19_[_loc32_]);
                    _loc7_.add(_loc21_);
                    _loc32_++;
                }
            }
            else if(_loc18_.length > 0)
            {
                _loc33_ = 0;
                while(_loc33_ < _loc18_.length)
                {
                    _loc21_ = new Point((int) _loc18_[_loc33_],(int) Math.floor(_loc11_.getY() + _loc14_));
                    _loc7_.add(_loc21_);
                    _loc33_++;
                }
            }
            else if(_loc20_ == 1)
            {
                _loc21_ = new Point((int)Math.floor(_loc11_.getX() + _loc13_),(int)Math.floor(_loc11_.getY() + _loc14_));
                _loc7_.add(_loc21_);
            }
            _loc11_.setX((_loc11_.getX() * 100 + _loc13_ * 100) / 100);
            _loc11_.setY((_loc11_.getY() * 100 + _loc14_ * 100) / 100);
            _loc10_++;
        }
        return _loc7_;
    }
}


