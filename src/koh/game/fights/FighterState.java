package koh.game.fights;

import java.util.HashMap;

import koh.game.entities.actors.Player;
import koh.game.fights.effects.buff.BuffEffect;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_MAKE_INVISIBLE;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightInvisibilityMessage;
import koh.protocol.messages.game.context.fight.character.GameFightRefreshFighterMessage;

/**
 *
 * @author Neo-Craft
 */
public class FighterState {

    private HashMap<FightStateEnum, BuffEffect> myStates = new HashMap<>();

    private Fighter myFighter;

    public FighterState(Fighter Fighter) {
        this.myFighter = Fighter;
    }

    public boolean canState(FightStateEnum State) {
        switch (State) {
            case Porté:
            case Porteur:
                return !hasState(FightStateEnum.Lourd);
        }
        return !hasState(State);
    }

    public boolean hasState(FightStateEnum State) {
        return this.myStates.containsKey(State);
    }

    public BuffEffect getBuffByState(FightStateEnum fse) {
        return myStates.get(fse);
    }

    public void addState(BuffEffect Buff) {
        switch (Buff.CastInfos.EffectType) {
            case Invisibility:
                myFighter.setVisibleState(GameActionFightInvisibilityStateEnum.INVISIBLE);
                for (Player o : this.myFighter.getFight().Observable$stream()) {
                    o.send(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, Buff.caster.getID(), myFighter.getID(), myFighter.getVisibleStateFor(o)));
                }
                this.myStates.put(FightStateEnum.Invisible, Buff);
                return;

            case REFLECT_SPELL:
                this.myStates.put(FightStateEnum.STATE_REFLECT_SPELL, Buff);
                return;

            default:
                // Buff.target.fight.SendToFight(new GameActionMessage((int)EffectEnum.addState, this.myFighter.ActorId, this.myFighter.ActorId + "," + Buff.CastInfos.Value3 + ",1"));
                break;
        }

        this.myStates.put(FightStateEnum.valueOf(Buff.CastInfos.Effect.value), Buff);
    }

    public void delState(BuffEffect Buff) {
        switch (Buff.CastInfos.EffectType) {
            case Invisibility:
                this.myFighter.setVisibleState(GameActionFightInvisibilityStateEnum.VISIBLE);
                this.myFighter.getFight().sendToField(new GameActionFightInvisibilityMessage(ACTION_CHARACTER_MAKE_INVISIBLE, Buff.caster.getID(), myFighter.getID(), myFighter.getVisibleStateFor(null)));
                this.myFighter.getFight().sendToField(new GameFightRefreshFighterMessage(myFighter.getGameContextActorInformations(null)));
                this.myStates.remove(FightStateEnum.Invisible);
                return;
            case REFLECT_SPELL:
                this.myStates.remove(FightStateEnum.STATE_REFLECT_SPELL);
                return;

            default:
                // Buff.target.fight.SendToFight(new GameActionMessage((int) EffectEnum.addState, this.myFighter.ActorId, this.myFighter.ActorId + "," + Buff.CastInfos.Value3 + ",0"));
                break;
        }

        this.myStates.remove(FightStateEnum.valueOf(Buff.CastInfos.Effect.value));
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
    
    public void fakeState(FightStateEnum State , boolean Add){
        if(Add)
            this.myStates.put(State, null);
        else
            this.myStates.remove(State);
    }

}
