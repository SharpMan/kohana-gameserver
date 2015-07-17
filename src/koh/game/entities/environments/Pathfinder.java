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

    public static double GetPathTime(int Len) {
        return ((Len >= 6 ? Pathfinder.RUN_SPEED : Pathfinder.WALK_SPEED) * 1000 * Len);
    }

    public static MovementPath DecodePath(DofusMap Map, short CurrentCell, byte CurrentDirection, short[] Path) {
        MovementPath MovementPath = new MovementPath();

        //MovementPath.AddCell(CurrentCell, CurrentDirection);
        for (short i : Path) {
            MovementPath.AddCell((short) (i & 4095), (byte) (i >> 12 & 7));
        }

        return MovementPath;
    }

    public static MovementPath IsValidPath(Fight Fight, Fighter Fighter, short CurrentCell, byte CurrentDirection, short[] EncodedPath) {
        MovementPath DecodedPath = Pathfinder.DecodePath(Fight.Map, CurrentCell, CurrentDirection, EncodedPath);
        MovementPath FinalPath = new MovementPath();

        int Index = 0;
        short TransitCell = 0;
        do {
            TransitCell = DecodedPath.TransitCells.get(Index);

            int Length = Pathfinder.IsValidLine(Fight, Fighter, FinalPath, TransitCell, DecodedPath.GetDirection(TransitCell), DecodedPath.TransitCells.get(Index + 1));
            if (Length == -1) {
                return null;
            } else if (Length == -2) {
                break;
            }

            Index++;

        } while (TransitCell != DecodedPath.LastStep());

        return FinalPath;
    }

    public static int IsValidLine(Fight Fight, Fighter Fighter, MovementPath Path, short BeginCell, byte Direction, int EndCell) {
        int Length = -1;
        Short ActualCell = BeginCell;

        if (!Pathfinder.InLine(Fight.Map, BeginCell, EndCell)) {
            return Length;
        }

        Length = (int) GoalDistanceEstimate(Fight.Map, BeginCell, EndCell);

        Path.AddCell(ActualCell, Direction);

        for (int i = 0; i < Length; i++) {

            ActualCell = (short) Pathfinder.NextCell(ActualCell, Direction);

            if (!Fight.Map.getCell(ActualCell).Walakable()) {
                return -2;
            }

            if (Fight.GetFighterOnCell(ActualCell) != null) {
                if (i == 0) {
                    Fighter.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 276));
                }
                return -2;
            }

            Path.AddCell(ActualCell, Direction);

            Path.MovementLength++;

            if (Pathfinder.IsStopCell(Fighter.Fight, Fighter.Team, ActualCell, Fighter)) {
                return -2;
            }
        }

        return Length;
    }

    public static boolean IsStopCell(Fight Fight, FightTeam Team, short CellId, Fighter Fighter) {
        // Un piege etc ?
        if (Fight.GetCell(CellId).HasGameObject(FightObjectType.OBJECT_TRAP) || Fight.GetCell(CellId).HasGameObject(FightObjectType.OBJECT_BOMB)) {
            //Fight.GetCell(CellId).GetObjects<FightTrap>().ForEach(x => x.onTraped(Fighter));
            return true;
        }
        if (Team != null) {
            return GetEnnemyNear(Fight, Team, CellId, true).size() > 0;
        } else {
            return false;
        }
    }

    public static ArrayList<Fighter> GetEnnemyNear(Fight Fight, FightTeam Team, short CellId) {
        return GetEnnemyNear(Fight, Team, CellId, false);
    }

    public static ArrayList<Fighter> GetEnnemyNear(Fight Fight, FightTeam Team, short CellId, boolean NotVisible) {
        ArrayList<Fighter> Ennemies = new ArrayList<>();

        for (byte Direction : Pathfinder.FIGHT_DIRECTIONS) {
            Fighter Ennemy = Fight.HasEnnemyInCell((short) Pathfinder.NextCell(CellId, Direction), Team);
            if (Ennemy != null) {
                if (!Ennemy.Dead && !(NotVisible && Ennemy.VisibleState != GameActionFightInvisibilityStateEnum.INVISIBLE)) {
                    Ennemies.add(Ennemy);
                }
            }
        }

        return Ennemies;
    }

    public static ArrayList<Fighter> GetEnnemyNearToTakle(Fight Fight, FightTeam Team, short CellId) {
        ArrayList<Fighter> Ennemies = new ArrayList<>();

        for (byte Direction : Pathfinder.FIGHT_DIRECTIONS) {
            Fighter Ennemy = Fight.HasEnnemyInCell((short) Pathfinder.NextCell(CellId, Direction), Team);
            if (Ennemy != null) {
                if (!Ennemy.Dead() && !Ennemy.States.HasState(FightStateEnum.Enraciné) && Ennemy.VisibleState != GameActionFightInvisibilityStateEnum.INVISIBLE) {
                    Ennemies.add(Ennemy);
                }
            }
        }

        return Ennemies;
    }

    public static byte OppositeDirection(byte Direction) {
        return (byte) (Direction >= 4 ? Direction - 4 : Direction + 4);
    }

    public static short NextCell(short Cell, byte Direction) //TOODO : Refaire tout la merde d'Ankama en static , pour ne pas faire tout ces instances de merde
    {
        try {
            return MapPoint.fromCellId(Cell).getNearestCellInDirection(Direction).get_cellId();
        } catch (Exception e) {
            return -1;
        }
    }

    public static byte GetDirection(DofusMap Map, int BeginCell, int EndCell) {
        return MapPoint.fromCellId(BeginCell).orientationTo(MapPoint.fromCellId(EndCell));
    }

    public static boolean InLine(DofusMap Map, int BeginCell, int EndCell) {
        try {
            return MapPoint.GetX(BeginCell) == MapPoint.GetX(EndCell) || MapPoint.GetY(BeginCell) == MapPoint.GetY(EndCell);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static double GoalDistanceEstimate(DofusMap Map, int BeginCell, int EndCell) {
        //return MapPoint.fromCellId(BeginCell).distanceToCell(MapPoint.fromCellId(EndCell));
        int loc7 = MapPoint.GetX(BeginCell) - MapPoint.GetX(EndCell);
        int loc8 = MapPoint.GetY(BeginCell) - MapPoint.GetY(EndCell);

        return Math.sqrt(Math.pow(loc7, 2) + Math.pow(loc8, 2));
    }

    public static int GoalDistance(DofusMap Map, int beginCell, int endCell) { //To not use
        return (int) (Math.abs(MapPoint.GetX(endCell) - MapPoint.GetX(beginCell)) + Math.abs(MapPoint.GetY(endCell) - MapPoint.GetY(beginCell)));
    }

    public static Short[] GetLineCellsBetween(Fight Fight, short BeginCell, byte Direction, int EndCell) {
        int Length = -1;
        Short ActualCell = BeginCell;

        if (!Pathfinder.InLine(Fight.Map, BeginCell, EndCell)) {
            return null;
        }

        Length = (int) GoalDistanceEstimate(Fight.Map, BeginCell, EndCell) - 1;
        Short[] Cells = new Short[Length];

        for (int i = 0; i < Length; i++) {

            ActualCell = (short) Pathfinder.NextCell(ActualCell, Direction);

            if (!Fight.Map.getCell(ActualCell).Walakable()) {
                return null;
            }

            if (Fight.GetFighterOnCell(ActualCell) != null) {
                return null;
            }

            Cells[i] = ActualCell;

            if (Pathfinder.IsStopCell(Fight, null, ActualCell, null)) {
                return null;
            }
        }

        return Cells;
    }

}
