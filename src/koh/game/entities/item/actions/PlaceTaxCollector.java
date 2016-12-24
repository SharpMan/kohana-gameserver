package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.item.ItemAction;

import java.security.SecureRandom;

/**
 * Created by Melancholia on 12/9/16.
 */
public class PlaceTaxCollector  extends ItemAction {

    public PlaceTaxCollector(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }

    final static SecureRandom rnd = new SecureRandom();

    @Override
    public boolean execute(Player possessor, Player p, int cell) {
        if(!super.execute(possessor,p, cell)
                || p.getGuild() == null
                || p.getGuild().getTaxCollectors().size() >= p.getGuild().getEntity().maxTaxCollectors
                || DAO.getTaxCollectors().isPresentOn(p.getMapid()))
            return false;
        final TaxCollector summoned = new TaxCollector(p.getCell().getId(), p.getGuild(), rnd.nextInt(154) +1, rnd.nextInt(253) +1, p.getMapid(), 0, 0, 0, 0, p.getNickName(),0,"");
        if(!DAO.getTaxCollectors().insert(summoned))
            return false;
        summoned.setID(p.getCurrentMap().getNextActorId());
        summoned.setActorCell(p.getCell());
        p.getCurrentMap().spawnActor(summoned);

        return true;
    }
}
