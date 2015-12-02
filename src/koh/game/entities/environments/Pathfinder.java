package koh.game.entities.environments;

import java.util.ArrayList;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 *
 * @author Neo-Craft
 */
public class Pathfinder {

    public static final double RUN_SPEED = 0.20;
    public static final double WALK_SPEED = 0.50;
    private static final byte[] FIGHT_DIRECTIONS = {1, 3, 5, 7};

    public static int getDistance(int param1, int param2) {
        return MapPoint.fromCellId(param1).distanceToCell(MapPoint.fromCellId(param2));
    }

    public static int getSquareDistance(int param1, int param2) {
        MapPoint _loc3_ = MapPoint.fromCellId(param1);
        MapPoint _loc4_ = MapPoint.fromCellId(param2);
        return Math.max(Math.abs(_loc3_.get_x() - _loc4_.get_x()), Math.abs(_loc3_.get_y() - _loc4_.get_y()));
    }

    public static double getPathTime(int Len) {
        return ((Len >= 6 ? Pathfinder.RUN_SPEED : Pathfinder.WALK_SPEED) * 1000 * Len);
    }

    public static MovementPath DecodePath(DofusMap Map, short CurrentCell, byte CurrentDirection, short[] Path) {
        MovementPath MovementPath = new MovementPath();

        //MovementPath.addCell(CurrentCell, CurrentDirection);
        for (short i : Path) {
            MovementPath.addCell((short) (i & 4095), (byte) (i >> 12 & 7));
        }

        return MovementPath;
    }

    public static MovementPath isValidPath(Fight fight, Fighter fighter, short currentCell, byte currentDirection, short[] encodedPath) {
        MovementPath DecodedPath = Pathfinder.DecodePath(fight.Map, currentCell, currentDirection, encodedPath);
        MovementPath FinalPath = new MovementPath();

        int Index = 0;
        short TransitCell = 0;
        do {
            TransitCell = DecodedPath.transitCells.get(Index);

            int Length = Pathfinder.isValidLine(fight, fighter, FinalPath, TransitCell, DecodedPath.getDirection(TransitCell), DecodedPath.transitCells.get(DecodedPath.transitCells.size() == 1 ? Index : Index + 1));
            if (Length == -1) {
                return null;
            } else if (Length == -2) {
                break;
            }

            Index++;

        } while (TransitCell != DecodedPath.getLastStep());

        return FinalPath;
    }

    public static int isValidLine(Fight fight, Fighter fighter, MovementPath path, short beginCell, byte direction, int endCell) {
        int length = -1;
        Short ActualCell = beginCell;

        if (!Pathfinder.inLine(fight.Map, beginCell, endCell)) {
            return length;
        }

        length = (int) getGoalDistanceEstimate(fight.Map, beginCell, endCell);

        path.addCell(ActualCell, direction);

        for (int i = 0; i < length; i++) {

            ActualCell = (short) Pathfinder.nextCell(ActualCell, direction);

            if (!fight.Map.getCell(ActualCell).walakable()) {
                return -2;
            }

            if (fight.GetFighterOnCell(ActualCell) != null) {
                if (i == 0) {
                    fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 276));
                }
                return -2;
            }

            path.addCell(ActualCell, direction);

            path.movementLength++;

            if (Pathfinder.isStopCell(fighter.Fight, fighter.Team, ActualCell, fighter)) {
                return -2;
            }
        }

        return length;
    }

    public static boolean isStopCell(Fight fight, FightTeam team, short cellId, Fighter fighter) {
        // Un piege etc ?
        if (fight.GetCell(cellId).HasGameObject(FightObjectType.OBJECT_TRAP) || fight.GetCell(cellId).HasGameObject(FightObjectType.OBJECT_BOMB) || fight.GetCell(cellId).HasGameObject(FightObjectType.OBJECT_PORTAL)) {
            //fight.GetCell(CellId).GetObjects<FightTrap>().ForEach(x => x.onTraped(Fighter));
            return true;
        }
        if (team != null) {
            return GetEnnemyNear(fight, team, cellId, true).size() > 0;
        } else {
            return false;
        }
    }

    public static ArrayList<Fighter> GetEnnemyNear(Fight fight, FightTeam team, short cellId) {
        return GetEnnemyNear(fight, team, cellId, false);
    }

    public static ArrayList<Fighter> GetEnnemyNear(Fight fight, FightTeam team, short cellId, boolean notVisible) {
        ArrayList<Fighter> ennemies = new ArrayList<>();

        for (byte Direction : Pathfinder.FIGHT_DIRECTIONS) {
            Fighter Ennemy = fight.HasEnnemyInCell((short) Pathfinder.nextCell(cellId, Direction), team);
            if (Ennemy != null) {
                if (!Ennemy.Dead && !(notVisible && Ennemy.VisibleState != GameActionFightInvisibilityStateEnum.INVISIBLE)) {
                    ennemies.add(Ennemy);
                }
            }
        }

        return ennemies;
    }

    public static ArrayList<Fighter> getEnnemyNearToTakle(Fight Fight, FightTeam Team, short CellId) {
        ArrayList<Fighter> Ennemies = new ArrayList<>();

        for (byte Direction : Pathfinder.FIGHT_DIRECTIONS) {
            Fighter Ennemy = Fight.HasEnnemyInCell((short) Pathfinder.nextCell(CellId, Direction), Team);
            if (Ennemy != null) {
                if (!Ennemy.Dead() && !Ennemy.States.HasState(FightStateEnum.Enraciné) && Ennemy.VisibleState != GameActionFightInvisibilityStateEnum.INVISIBLE) {
                    Ennemies.add(Ennemy);
                }
            }
        }

        return Ennemies;
    }

    public static byte oppositeDirection(byte Direction) {
        return (byte) (Direction >= 4 ? Direction - 4 : Direction + 4);
    }

    public static short nextCell(short Cell, byte Direction) //TOODO : Refaire tout la merde d'Ankama en static , pour ne pas faire tout ces instances de merde
    {
        try {
            return MapPoint.fromCellId(Cell).getNearestCellInDirection(Direction).get_cellId();
        } catch (Exception e) {
            return -1;
        }
    }

    public static short nextCell(short cell, byte direction, int time) {
        try {
            short Cell2 = MapPoint.fromCellId(cell).getNearestCellInDirection(direction).get_cellId();

            for (int i = 1; i < time; i++) {
                Cell2 = MapPoint.fromCellId(Cell2).getNearestCellInDirection(direction).get_cellId();
            }
            return Cell2;
        } catch (Exception e) {
            return -1;
        }
    }

    public static byte getDirection(DofusMap map, int beginCell, int endCell) {
        return MapPoint.fromCellId(beginCell).orientationTo(MapPoint.fromCellId(endCell));
    }

    public static boolean inLine(DofusMap map, int beginCell, int endCell) {
        try {
            return MapPoint.GetX(beginCell) == MapPoint.GetX(endCell) || MapPoint.GetY(beginCell) == MapPoint.GetY(endCell);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static double getGoalDistanceEstimate(DofusMap map, int BeginCell, int EndCell) {
        //return MapPoint.fromCellId(beginCell).distanceToCell(MapPoint.fromCellId(getEndCell));
        int loc7 = MapPoint.GetX(BeginCell) - MapPoint.GetX(EndCell);
        int loc8 = MapPoint.GetY(BeginCell) - MapPoint.GetY(EndCell);

        return Math.sqrt(Math.pow(loc7, 2) + Math.pow(loc8, 2));
    }

    public static int getGoalDistance(DofusMap map, int beginCell, int endCell) { //To not use
        return (int) (Math.abs(MapPoint.GetX(endCell) - MapPoint.GetX(beginCell)) + Math.abs(MapPoint.GetY(endCell) - MapPoint.GetY(beginCell)));
    }

    public static Short[] getLineCellsBetween(Fight fight, short beginCell, byte direction, int endCell) {
        return getLineCellsBetween(fight, beginCell, direction, endCell, true);
    }

    public static Short[] getLineCellsBetweenBomb(Fight fight, short beginCell, byte direction, int endCell, boolean withoutFighter) {
        int length = -1;
        Short actualCell = beginCell;

        if (!Pathfinder.inLine(fight.Map, beginCell, endCell)) {
            return null;
        }

        length = (int) getGoalDistanceEstimate(fight.Map, beginCell, endCell) - 1;
        Short[] cells = new Short[length];

        for (int i = 0; i < length; i++) {

            actualCell = (short) Pathfinder.nextCell(actualCell, direction);


            if (withoutFighter && fight.GetFighterOnCell(actualCell) != null) {
                return null;
            }

            cells[i] = actualCell;

            if (Pathfinder.isStopCell(fight, null, actualCell, null)) {
                return null;
            }
        }

        return cells;
    }
    
    public static Short[] getLineCellsBetween(Fight fight, short beginCell, byte direction, int endCell, boolean withoutFighter) {
        int length = -1;
        Short actualCell = beginCell;

        if (!Pathfinder.inLine(fight.Map, beginCell, endCell)) {
            return null;
        }

        length = (int) getGoalDistanceEstimate(fight.Map, beginCell, endCell) - 1;
        Short[] Cells = new Short[length];

        for (int i = 0; i < length; i++) {

            actualCell = (short) Pathfinder.nextCell(actualCell, direction);

            if (!fight.Map.getCell(actualCell).walakable()) {
                return null;
            }

            if (withoutFighter && fight.GetFighterOnCell(actualCell) != null) {
                return null;
            }

            Cells[i] = actualCell;

            if (Pathfinder.isStopCell(fight, null, actualCell, null)) {
                return null;
            }
        }

        return Cells;
    }

}
