package koh.game.fights.AI;

import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Melancholia on 1/11/16.
 */
public class AINeuron {

    public List<Fighter> myEnnemies = new ArrayList<Fighter>();
    @Getter @Setter
    private boolean attacked = false;
    public List<Short> myReachableCells = new ArrayList<Short>();
    public SpellLevel myBestSpell;
    public int myBestScore = 0;
    public short myBestMoveCell, myBestCastCell;
    public boolean myFirstTargetIsMe;
    public Map<Integer,Double> myScoreInvocations = new HashMap<>();
    public Map<Integer,Double> myScoreSpells = new HashMap<>();

    @Override
    public void finalize(){
        try {
            if (myEnnemies != null)
            {
                myEnnemies.clear();
                myEnnemies = null;
            }
            attacked = false;
            if (myReachableCells != null)
            {
                myReachableCells.clear();
                myReachableCells = null;
            }
            if (myScoreInvocations != null)
            {
                myScoreInvocations.clear();
                myScoreInvocations = null;
            }
            myBestSpell = null;
            myFirstTargetIsMe = false;
            myBestScore =  0;
            myBestMoveCell = myBestCastCell = 0;
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
