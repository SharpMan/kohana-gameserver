package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.StatsEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class Restat  extends ItemAction {

    public Restat(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        p.setLife(Math.max(p.getLife() - p.getVitality(), 0));
        p.setVitality(0);
        p.setStrength(0);
        p.setIntell(0);
        p.setAgility(0);
        p.setChance(0);
        p.getStats().getStats().get(StatsEnum.VITALITY).base = 0;
        p.getStats().getStats().get(StatsEnum.WISDOM).base = 0;
        p.getStats().getStats().get(StatsEnum.STRENGTH).base = 0;
        p.getStats().getStats().get(StatsEnum.INTELLIGENCE).base = 0;
        p.getStats().getStats().get(StatsEnum.AGILITY).base = 0;
        p.getStats().getStats().get(StatsEnum.CHANCE).base = 0;
        p.setStatPoints((p.getLevel() - 1) * 5);
        p.getAdditionalStats().forEach((stat, number) -> {
            p.getStats().addBase(StatsEnum.valueOf(stat), number);
            switch (StatsEnum.valueOf(stat)) {
                case STRENGTH:
                    p.setStrength(p.getStrength() + number);
                    break;

                case VITALITY:
                    p.setVitality(p.getVitality() + number);
                    p.healLife(p.getLife() + number); // on boost la life
                    break;

                case WISDOM:
                    p.setWisdom(p.getWisdom() + number);
                    break;

                case INTELLIGENCE:
                    p.setIntell(p.getIntell() + number);
                    break;

                case CHANCE:
                    p.setChance(p.getChance() + number);
                    break;

                case AGILITY:
                    p.setAgility(p.getAgility() + number);
                    break;
            }
        });
        p.getAdditionalStats().clear();
        p.refreshStats();
        return true;
    }
}
