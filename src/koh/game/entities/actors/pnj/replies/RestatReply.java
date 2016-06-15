package koh.game.entities.actors.pnj.replies;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 * Created by Melancholia on 4/5/16.
 */
public class RestatReply extends NpcReply {

    @Override
    public boolean execute(Player p) {
        if (!super.execute(p)) {
            return false;
        }
        p.setLife(Math.max(p.getLife() - p.getVitality(), 0));
        p.setVitality(0);
        p.setStrength(0);
        p.setIntell(0);
        p.setAgility(0);
        p.setChance(0);
        p.setWisdom(0);
        //p.getStats().resetBase();
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
        p.refreshStats();
        p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE,470));
        return true;
    }

}
