package koh.game.entities.maps.pathfinding;

import java.awt.Point;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class LinkedCellsManager {

    public final static int PSEUDO_INFINITE = 63;
    private final static int SAME = 0;
    private final static int OPPOSITE = 1;
    private final static int TRIGONOMETRIC = 2;
    private final static int COUNTER_TRIGONOMETRIC = 3;

    public static int[] getLinks(MapPoint CurrentPortal, MapPoint[] OpenedPortals) {
        MapPoint ClosetPortal = null;
        int toRemovePortalIndex = 0;
        if (OpenedPortals == null || OpenedPortals.length <= 1 && CurrentPortal.get_cellId() == OpenedPortals[0].get_cellId()) {
            return new int[]{CurrentPortal.get_cellId()};
        }
        MapPoint[] _loc3_ = new MapPoint[0];
        int _loc4_ = 0;
        while (_loc4_ < OpenedPortals.length) {
            if (OpenedPortals[_loc4_].get_cellId() != CurrentPortal.get_cellId()) {
                _loc3_ = ArrayUtils.add(_loc3_, OpenedPortals[_loc4_]);
            }
            _loc4_++;
        }
        int[] _loc5_ = new int[0];
        MapPoint _loc6_ = CurrentPortal;
        int _loc9_ = _loc3_.length + 1;
        while ((_loc3_.length > 0) || _loc9_ > 0) {
            _loc9_--;
            _loc5_ = ArrayUtils.add(_loc5_, _loc6_.get_cellId());
            toRemovePortalIndex = ArrayUtils.indexOf(_loc3_, _loc6_);
            if (toRemovePortalIndex != -1) {
                _loc3_ = ArrayUtils.removeElement(_loc3_, _loc6_);
            }
            ClosetPortal = getClosestPortal(_loc6_, _loc3_);
            if (ClosetPortal == null) {
                break;
            }
            _loc6_ = ClosetPortal;
        }
        if (_loc5_.length < 2) {
            return new int[]{CurrentPortal.get_cellId()};
        }
        return _loc5_;
    }

    private static MapPoint getClosestPortal(MapPoint Portal, MapPoint[] OpenedPortals) {
        int GoalDistance = 0;
        MapPoint[] Portals = new MapPoint[0];
        int _loc4_ = PSEUDO_INFINITE;
        for (MapPoint _loc5_ : OpenedPortals) {
            GoalDistance = Portal.distanceToCell(_loc5_);
            if (GoalDistance < _loc4_) {
                Portals = new MapPoint[0];
                //_loc3_.length = 0;
                Portals = ArrayUtils.add(Portals, _loc5_);
                _loc4_ = GoalDistance;
            } else if (GoalDistance == _loc4_) {
                Portals = ArrayUtils.add(Portals, _loc5_);
            }

        }
        if (Portals.length == 0) {
            return null;
        }
        if (Portals.length == 1) {
            return Portals[0];
        }
        return getBestNextPortal(Portal, Portals);
    }

    private static MapPoint getBestNextPortal(MapPoint param1, MapPoint[] param2) {
        Point refCoord = null;
        Point nudge = null;
        MapPoint refCell = param1;
        MapPoint[] closests = param2;
        if (closests.length < 2) {
            throw new Error("closests should have a size of 2.");
        } else {
            refCoord = refCell.coordinates();
            nudge = new Point(refCoord.x, refCoord.y + 1);

            final Point CommparedrefCoord = refCoord;
            final Point ComparedNudge = nudge;

            Arrays.stream(closests).sorted((MapPoint param3, MapPoint param4) -> {
                final double _loc3_ = getPositiveOrientedAngle(CommparedrefCoord, ComparedNudge, new Point(param3.get_x(), param3.get_y())) - getPositiveOrientedAngle(CommparedrefCoord, ComparedNudge, new Point(param4.get_x(), param4.get_y()));
                return _loc3_ > 0 ? 1 : _loc3_ < 0 ? -1 : 0;
            }).toArray(MapPoint[]::new);
            MapPoint res = getBestPortalWhenRefIsNotInsideClosests(refCell, closests);
            if (res != null) {
                return res;
            }
            return closests[0];
        }
    }

    private static double getPositiveOrientedAngle(Point Portal1, Point Portal2, Point Portal3) {
        switch (compareAngles(Portal1, Portal2, Portal3)) {
            case SAME:
                return 0;
            case OPPOSITE:
                return Math.PI;
            case TRIGONOMETRIC:
                return getAngle(Portal1, Portal2, Portal3);
            case COUNTER_TRIGONOMETRIC:
                return 2 * Math.PI - getAngle(Portal1, Portal2, Portal3);
            default:
                return 0;
        }
    }

    private static int compareAngles(Point Portal1, Point Portal2, Point Portal3) {
        Point _loc4_ = vector(Portal1, Portal2);
        Point _loc5_ = vector(Portal1, Portal3);
        int _loc6_ = getDeterminant(_loc4_, _loc5_);
        if (_loc6_ != 0) {
            return _loc6_ > 0 ? TRIGONOMETRIC : COUNTER_TRIGONOMETRIC;
        }
        return _loc4_.x >= 0 == _loc5_.x >= 0 && _loc4_.y >= 0 == _loc5_.y >= 0 ? SAME : OPPOSITE;
    }

    private static Point vector(Point param1, Point param2) {
        return new Point(param2.x - param1.x, param2.y - param1.y);
    }

    private static double getAngle(Point Portal1, Point Portal2, Point Portal3) {
        double _loc4_ = getComplexDistance(Portal2, Portal3);
        double _loc5_ = getComplexDistance(Portal1, Portal2);
        double _loc6_ = getComplexDistance(Portal1, Portal3);
        return Math.acos((_loc5_ * _loc5_ + _loc6_ * _loc6_ - _loc4_ * _loc4_) / (2 * _loc5_ * _loc6_));
    }

    private static Double getComplexDistance(Point param1, Point param2) {
        return Math.sqrt(Math.pow(param1.x - param2.x, 2) + Math.pow(param1.y - param2.y, 2));
    }

    private static int getDeterminant(Point param1, Point param2) {
        return param1.x * param2.y - param1.y * param2.x;
    }

    private static MapPoint getBestPortalWhenRefIsNotInsideClosests(MapPoint Portal, MapPoint[] openedPortals) {
        if (openedPortals.length < 2) {
            return null;
        }
        MapPoint lastPortal = openedPortals[openedPortals.length - 1];
        for (MapPoint _loc4_ : openedPortals) {
            switch (compareAngles(Portal.coordinates(), lastPortal.coordinates(), _loc4_.coordinates())) {
                case OPPOSITE:
                    if (openedPortals.length <= 2) {
                        return null;
                    }
                case COUNTER_TRIGONOMETRIC:
                    return lastPortal;
                default:
                    lastPortal = _loc4_;
                    continue; //Mustn't be here
            }
        }
        return null;
    }

}
