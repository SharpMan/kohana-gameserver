package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.List;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.MapTools;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 *
 * @author Neo-Craft
 */
public class Pathfunction {

    public static final double RUN_SPEED = 0.20;
    public static final double WALK_SPEED = 0.50;
    private static final byte[] FIGHT_DIRECTIONS = {1, 3, 5, 7};

    public static int getDistance(int param1, int param2) {
        return MapPoint.fromCellId(param1).distanceToCell(MapPoint.fromCellId(param2));
    }

    public static int getSquareDistance(int param1, int param2) {
        final MapPoint _loc3_ = MapPoint.fromCellId(param1);
        final MapPoint _loc4_ = MapPoint.fromCellId(param2);
        return Math.max(Math.abs(_loc3_.get_x() - _loc4_.get_x()), Math.abs(_loc3_.get_y() - _loc4_.get_y()));
    }

    public static double getPathTime(int Len) {
        return ((Len >= 6 ? Pathfunction.RUN_SPEED : Pathfunction.WALK_SPEED) * 1000 * Len);
    }

    public static MovementPath decodePath(DofusMap Map, short CurrentCell, byte CurrentDirection, short[] Path) {
        MovementPath MovementPath = new MovementPath();

        //MovementPath.addCell(CurrentCell, CurrentDirection);
        for (short i : Path) {
            MovementPath.addCell((short) (i & 4095), (byte) (i >> 12 & 7));
        }

        return MovementPath;
    }

    public static MovementPath isValidPath(Fight fight, Fighter fighter, short currentCell, byte currentDirection, short[] encodedPath) {
        final MovementPath decodePath = Pathfunction.decodePath(fight.getMap(), currentCell, currentDirection, encodedPath);
        final MovementPath finalPath = new MovementPath();

        int index = 0;
        short transitCell = 0;
        do {
            transitCell = decodePath.transitCells.get(index);
            //System.out.println(Enumerable.join(encodedPath));
            int length = Pathfunction.isValidLine(fight, fighter, finalPath, transitCell, decodePath.getDirection(index), decodePath.transitCells.get(decodePath.transitCells.size() == 1 ? index : index + 1));
            if (length == -1) {
                return null;
            } else if (length == -2) {
                break;
            }
            index++;

        } while (transitCell != decodePath.getLastStep());

        return finalPath;
    }

    public static int isValidLine(Fight fight, Fighter fighter, MovementPath path, short beginCell, byte direction, int endCell) {
        int length = -1;
        Short actualCell = beginCell;

        if (!Pathfunction.inLine(fight.getMap(), beginCell, endCell)) {
            return length;
        }

        length = (int) goalDistanceEstimate(fight.getMap(), beginCell, endCell);

        path.addCell(actualCell, direction);

        for (int i = 0; i < length; i++) {

            actualCell = Pathfunction.nextCell(actualCell, direction);

            if (!fight.getCell(actualCell).canWalk()) {
                fighter.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 276));
                return -2;
            }

            path.addCell(actualCell, direction);

            path.setMovementLength(path.getMovementLength() +1);

            if (Pathfunction.isStopCell(fighter.getFight(), fighter.getTeam(), actualCell, fighter)) {
                return -2;
            }
        }

        return length;
    }

    public static boolean isStopCell(Fight fight, FightTeam team, short cellId, Fighter fighter) {
        // Un piege etc ?
        if (fight.getCell(cellId).hasGameObject(FightObjectType.OBJECT_TRAP) || fight.getCell(cellId).hasGameObject(FightObjectType.OBJECT_BOMB) || fight.getCell(cellId).hasGameObject(FightObjectType.OBJECT_PORTAL)) {
            //fight.getCell(getCellId).getObjects<FightTrap>().ForEach(x => x.onTraped(Fighter));
            return true;
        }
        if (team != null) {
            return getEnnemyNear(fight, team, cellId, true).size() > 0;
        } else {
            return false;
        }
    }

    public static ArrayList<Fighter> getEnnemyNear(Fight fight, FightTeam team, short cellId) {
        return getEnnemyNear(fight, team, cellId, false);
    }

    public static ArrayList<Fighter> getEnnemyNear(Fight fight, FightTeam team, short cellId, boolean notVisible) {
        ArrayList<Fighter> ennemies = new ArrayList<>();

        for (byte Direction : Pathfunction.FIGHT_DIRECTIONS) {
            Fighter Ennemy = fight.hasEnnemyInCell(Pathfunction.nextCell(cellId, Direction), team);
            if (Ennemy != null) {
                if (!Ennemy.isDead() && !(notVisible && Ennemy.getVisibleState() != GameActionFightInvisibilityStateEnum.INVISIBLE) && !(Ennemy instanceof BombFighter)) {
                    ennemies.add(Ennemy);
                }
            }
        }

        return ennemies;
    }

    public static ArrayList<Fighter> getEnnemyNearToTakle(Fight Fight, FightTeam Team, short CellId) {
        final ArrayList<Fighter> ennemies = new ArrayList<>();

        for (byte Direction : Pathfunction.FIGHT_DIRECTIONS) {
            Fighter ennemy = Fight.hasEnnemyInCell(Pathfunction.nextCell(CellId, Direction), Team);
            if (ennemy != null) {
                if (!ennemy.isDead()
                        && !(ennemy instanceof BombFighter)
                        && !ennemy.getStates().hasState(FightStateEnum.ENRACINÉ)
                        && !(ennemy instanceof MonsterFighter && ennemy.asMonster().getGrade().getMonster().isCanTackle())
                        && ennemy.getVisibleState() != GameActionFightInvisibilityStateEnum.INVISIBLE) {
                    ennemies.add(ennemy);
                }
            }
        }

        return ennemies;
    }

    public static byte oppositeDirection(byte Direction) {
        return (byte) (Direction >= 4 ? Direction - 4 : Direction + 4);
    }

    public static short computeNextCell(short cell, int direction)
    {
        switch (direction)
        {
            case 0:
                return (short) (cell + 1);
            case 1:
                return (short) (cell + MapTools.WIDTH);
            case 2:
                return (short) (cell + (MapTools.WIDTH * 2) - 1);
            case 3:
                return (short) (cell + MapTools.WIDTH - 1);
            case 4:
                return (short) (cell - 1);
            case 5:
                return (short) (cell - MapTools.WIDTH);
            case 6:
                return (short) (cell - (MapTools.WIDTH * 2) - 1);
            case 7:
                return (short) (cell - MapTools.WIDTH + 1);
            default:
                return -1;
        }
    }


    public static short nextCell(short cell, byte direction) //TOODO : Refaire tout la merde d'Ankama en static , pour ne pas faire tout ces instances de merde
    {
        try {
            return MapPoint.fromCellId(cell).getNearestCellInDirection(direction).get_cellId();
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
        try
        {
            if (beginCell == endCell) return true;
            if ( MapPoint.getX(beginCell) == MapPoint.getX(endCell) && MapPoint.getY(beginCell) == MapPoint.getY(endCell))
            {
                return false;
            }
            return MapPoint.getX(beginCell) == MapPoint.getX( endCell) || MapPoint.getY(beginCell) == MapPoint.getY(endCell);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public static double goalDistanceNoSqrt(DofusMap map, int beginCell, int EendCelldCell)
    {
        int loc7 = MapPoint.getX(beginCell) - MapPoint.getX(EendCelldCell);
        int loc8 = MapPoint.getY(beginCell) - MapPoint.getY(EendCelldCell);

        return Math.pow(loc7, 2) + Math.pow(loc8, 2);
    }

    public static double goalDistanceEstimate(DofusMap map, int BeginCell, int EndCell) {
        //return MapPoint.fromCellId(beginCell).distanceToCell(MapPoint.fromCellId(getEndCell));
        int loc7 = MapPoint.getX(BeginCell) - MapPoint.getX(EndCell);
        int loc8 = MapPoint.getY(BeginCell) - MapPoint.getY(EndCell);

        return Math.sqrt(Math.pow(loc7, 2) + Math.pow(loc8, 2));
    }

    public static int goalDistanceScore(DofusMap Map, int beginCell, int endCell)
    {
        int  Xbegin = MapPoint.getX(beginCell);
        int Xend = MapPoint.getX(endCell);
        int Ybegin = MapPoint.getY(beginCell);
        int Yend = MapPoint.getY(endCell);
        int XDiff = Math.abs(Xbegin - Xend);
        int YDiff = Math.abs(Ybegin - Yend);

        return XDiff + YDiff;
    }

    public static int goalDistance(DofusMap map, int beginCell, int endCell) { //To not use
        return (Math.abs(MapPoint.getX(endCell) - MapPoint.getX(beginCell)) + Math.abs(MapPoint.getY(endCell) - MapPoint.getY(beginCell)));
    }

    public static List<Short> getCircleZone(short baseCell, int radius)
    {
        List<Short> openedList = new ArrayList<>();
        openedList.add(baseCell);
        for (int i = 0; i <= radius - 1; i++)
        {
                synchronized (openedList) {
                for(short value : openedList.stream().toArray(Short[]::new)){
                    for (short jCell : getJoinCell(value)) {
                        if (!openedList.contains(jCell)) {
                            openedList.add(jCell);
                        }
                    }
                }
            }
        }
        return openedList;
    }


    public static short[] getJoinCell(short cell)
    {
        return new short[] { nextCell(cell,(byte)1), nextCell(cell,(byte)3),nextCell(cell,(byte)5),nextCell(cell,(byte)7)};
    }

    public static Short[] getLineCellsBetween(Fight fight, short beginCell, byte direction, int endCell) {
        return getLineCellsBetween(fight, beginCell, direction, endCell, true);
    }

    public static Short[] getLineCellsBetweenBomb(Fight fight, short beginCell, byte direction, int endCell, boolean withoutFighter) {
        int length = -1;
        Short actualCell = beginCell;

        if (!Pathfunction.inLine(fight.getMap(), beginCell, endCell)) {
            return null;
        }

        length = (int) goalDistanceEstimate(fight.getMap(), beginCell, endCell) - 1;
        Short[] cells = new Short[length];

        for (int i = 0; i < length; i++) {

            actualCell =  Pathfunction.nextCell(actualCell, direction);


            if (withoutFighter && fight.getCell(actualCell).hasFighter()) {
                return null;
            }

            cells[i] = actualCell;

            if (Pathfunction.isStopCell(fight, null, actualCell, null)) {
                return null;
            }
        }

        return cells;
    }
    
    public static Short[] getLineCellsBetween(Fight fight, short beginCell, byte direction, int endCell, boolean withoutFighter) {
        int length = -1;
        Short actualCell = beginCell;

        if (!Pathfunction.inLine(fight.getMap(), beginCell, endCell)) {
            return null;
        }

        length = (int) goalDistanceEstimate(fight.getMap(), beginCell, endCell) - 1;
        Short[] Cells = new Short[length];

        for (int i = 0; i < length; i++) {

            actualCell = Pathfunction.nextCell(actualCell, direction);

            if (!fight.getMap().getCell(actualCell).walakable()) {
                return null;
            }

            if (withoutFighter && fight.getFighterOnCell(actualCell) != null) {
                return null;
            }

            Cells[i] = actualCell;

            if (Pathfunction.isStopCell(fight, null, actualCell, null)) {
                return null;
            }
        }

        return Cells;
    }


}
