package koh.game.fights.fighters;

import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.protocol.client.Message;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public class SummonedFighter extends Fighter {

    public SummonedFighter(koh.game.fights.Fight Fight, Fighter Invocator) {
        super(Fight, Invocator);
    }

    @Override
    public void EndFight() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int Level() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short MapCell() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FightTeamMemberInformations GetFightTeamMemberInformations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Send(Message Packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void JoinFight() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityLook GetEntityLook() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
     @Override
    public int compareTo(IFightObject obj) {
        return Priority().compareTo(obj.Priority());
    }

    @Override
    public int Initiative(boolean Base) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
