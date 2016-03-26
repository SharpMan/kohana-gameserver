package koh.game.entities.kolissium;

import koh.game.entities.actors.character.Party;

import java.util.Comparator;

/**
 * Created by Melancholia on 3/20/16.
 */
public class PlayersPartySorter implements Comparator<Party> {

    private boolean croisant;

    public PlayersPartySorter(boolean lth) {
        croisant = lth;
    }

    @Override
    public int compare(Party o1, Party o2) {
        return (o2.getMoyLevel() - o1.getMoyLevel()) * (croisant ? 1 : -1);
    }
}
