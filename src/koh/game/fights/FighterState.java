package koh.game.fights;

import java.util.HashMap;

import koh.game.entities.actors.Player;
import koh.game.fights.effects.buff.BuffEffect;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_MAKE_INVISIBLE;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightInvisibilityMessage;
import koh.protocol.messages.game.context.fight.character.GameFightRefreshFighterMessage;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class FighterState {

    @Getter
    private HashMap<FightStateEnum, BuffEffect> myStates = new HashMap<>();

    private Fighter fighter;

    public FighterState(Fighter Fighter) {
        this.fighter = Fighter;
    }

    public boolean canState(FightStateEnum State) {
        switch (State) {
            case CARRIED:
            case CARRIER:
                return !hasState(FightStateEnum.HEAVY);
           /* case Hypoglyphe:
                return false;*/
        }
        return !hasState(State);
    }

    public boolean hasState(FightStateEnum State) {
        return this.myStates.containsKey(State);
    }

    public boolean hasState(FightStateEnum... states) {
        for(FightStateEnum state : states)
            if(!this.myStates.containsKey(state))
                return false;
        return true;
    }

    public BuffEffect getBuffByState(FightStateEnum fse) {
        return myStates.get(fse);
    }

    public void addState(BuffEffect Buff) {
        switch (Buff.castInfos.effectType) {
            case INVISIBILITY:
                fighter.setLastCellSeen(fighter.getCellId());
                fighter.setVisibleState(GameActionFightInvisibilityStateEnum.INVISIBLE);
                for (Player o : this.fighter.getFight().observable$Stream()) {
                    o.send(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, Buff.caster.getID(), fighter.getID(), fighter.getVisibleStateFor(o)));
                }
                this.myStates.put(FightStateEnum.INVISIBLE, Buff);
                return;

            case REFLECT_SPELL:
                this.myStates.put(FightStateEnum.STATE_REFLECT_SPELL, Buff);
                return;

            default:
                // Buff.target.fight.SendToFight(new GameActionMessage((int)EffectEnum.addState, this.fighter.ActorId, this.fighter.ActorId + "," + Buff.castInfos.Value3 + ",1"));
                break;
        }

        this.myStates.put(FightStateEnum.valueOf(Buff.castInfos.effect.value), Buff);
    }

    public void delState(BuffEffect Buff) {
        switch (Buff.castInfos.effectType) {
            case INVISIBILITY:
                this.fighter.setVisibleState(GameActionFightInvisibilityStateEnum.VISIBLE);
                this.fighter.getFight().sendToField(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, Buff.caster.getID(), fighter.getID(), fighter.getVisibleStateFor(null)));
                this.fighter.getFight().sendToField(new GameFightRefreshFighterMessage(fighter.getGameContextActorInformations(null)));
                this.myStates.remove(FightStateEnum.INVISIBLE);
                return;
            case REFLECT_SPELL:
                this.myStates.remove(FightStateEnum.STATE_REFLECT_SPELL);
                return;

            default:
                // Buff.target.fight.SendToFight(new GameActionMessage((int) EffectEnum.addState, this.fighter.ActorId, this.fighter.ActorId + "," + Buff.castInfos.Value3 + ",0"));
                break;
        }

        this.myStates.remove(FightStateEnum.valueOf(Buff.castInfos.effect.value));
    }

    public void removeState(FightStateEnum State) {
        if (this.hasState(State)) {
            this.myStates.get(State).removeEffect();
        }
    }
    
    public BuffEffect findState(FightStateEnum State) {
        if (this.hasState(State)) {
            return this.myStates.get(State);
        }
        return null;
    }

    public void debuff() {
        for (BuffEffect State : this.myStates.values()) {
            State.removeEffect();
        }

        this.myStates.clear();
    }
    
    public void fakeState(FightStateEnum State , boolean add){
        if(add)
            this.myStates.put(State, null);
        else
            this.myStates.remove(State);
    }

}
