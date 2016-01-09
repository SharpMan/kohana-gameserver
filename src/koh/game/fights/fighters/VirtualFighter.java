package koh.game.fights.fighters;

import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.protocol.client.Message;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Melancholia on 12/30/15.
 */
public abstract class VirtualFighter extends Fighter {

    //@Getter private final AIProcessor mind;

    public VirtualFighter(Fight fight) {
        super(fight, null);
    }

    public VirtualFighter(Fight fight, Fighter summoner) {
        super(fight, summoner);
    }


    @Override
    public void joinFight(){
        //this.Mind = new AIProcessor(Fight, this)
    }


}
