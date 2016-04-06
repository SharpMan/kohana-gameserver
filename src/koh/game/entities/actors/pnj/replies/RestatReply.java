package koh.game.entities.actors.pnj.replies;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;
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
        p.getStats().resetBase();
        p.setStatPoints((p.getLevel() - 1) * 5);
        p.refreshStats();
        p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE,470));
        return true;
    }

}
