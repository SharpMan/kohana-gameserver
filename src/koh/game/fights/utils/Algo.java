package koh.game.fights.utils;

import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.Pathfinder;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.utils.Couple;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Algo {

    private static final Random RANDOM = new Random();

    private static Couple<Short, Short> getRandomBaseCellPlaces(DofusMap map) {
        short team1_baseCell = map.getRandomWalkableCell();
        short team2_baseCell = map.getRandomWalkableCell();

        if (Pathfinder.getGoalDistance(map, team1_baseCell, team2_baseCell) < 3) {
            return getRandomBaseCellPlaces(map);
        } else {
            return new Couple<>(team1_baseCell, team2_baseCell);
        }
    }

    public static Couple<ArrayList<FightCell>, ArrayList<FightCell>> genRandomFightPlaces(Fight fight) {
        ArrayList<FightCell> team1 = new ArrayList<>();
        ArrayList<FightCell> team2 = new ArrayList<>();

            /*
             * BaseCells
             */
        Couple<Short, Short> baseCells = getRandomBaseCellPlaces(fight.getMap());
        team1.add(fight.getCell(baseCells.first));
        team2.add(fight.getCell(baseCells.second));

            /*
             * Remplissage
             */
        int boucles = 0;
        while (team1.size() < 8) {
            if (boucles > 500) {
                break;
            }
            if (boucles > 25) {
                short randomCellId = fight.getMap().getRandomCell();
                FightCell cell = fight.getCell(randomCellId);
                if (cell != null && cell.isWalkable()) {
                    if (!team1.contains(cell)) {
                        team1.add(cell);
                    }
                }
                boucles++;
                continue;
            }
            boucles++;
            FightCell toDir = team1.get(random(0, team1.size() - 1));
            if (toDir == null) {
                continue;
            }
            FightCell randomCell = fight.getCell(fight.getMap().getRandomAdjacentFreeCell(toDir.Id).getId());
            if (randomCell != null) {
                if (!team1.contains(randomCell) && randomCell.isWalkable()) {
                    team1.add(randomCell);
                }
            }
        }

        boucles = 0;
        while (team2.size() < 8) {
            if (boucles > 500) {
                break;
            }
            if (boucles > 25) {
                short randomCellId = fight.getMap().getRandomCell();
                FightCell cell = fight.getCell(randomCellId);
                if (cell != null && cell.isWalkable()) {
                    if (!team1.contains(cell) && !team2.contains(cell)) {
                        team2.add(cell);
                    }
                }
                boucles++;
                continue;
            }
            boucles++;
            FightCell toDir = team2.get(random(0, team2.size() - 1));
            if (toDir == null) {
                continue;
            }
            FightCell randomCell = fight.getCell(fight.getMap().getRandomAdjacentFreeCell(toDir.Id).getId());
            if (randomCell != null) {
                if (!team1.contains(randomCell) && !team2.contains(randomCell) && randomCell.isWalkable()) {
                    team2.add(randomCell);
                }
            }
        }

        return new Couple<>(team1, team2);
    }

    public static int random(int i1, int i2) {
        return RANDOM.nextInt(i2 - i1 + 1) + i1;
    }

    public static byte randomDiretion() {
        return (byte) RANDOM.nextInt(7);
    }

}