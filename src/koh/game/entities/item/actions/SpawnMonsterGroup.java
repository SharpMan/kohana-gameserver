package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class SpawnMonsterGroup  extends ItemAction {

    private boolean delObj;
    private boolean inArena;

    public  SpawnMonsterGroup(String[] args, String criteria) {
        super(args, criteria);
        delObj = args[0].equals("true");
        inArena = args[1].equals("true");
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || p.client.isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        //TODO: arena if(inArena && !World.isArenaMap(perso.get_curCarte().get_id()))return;
        //TODO2: Parse MonsterEffect
        return true;
    }
}
