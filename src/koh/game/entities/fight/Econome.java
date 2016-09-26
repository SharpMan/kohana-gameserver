package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Melancholia on 8/28/16.
 * 5,Tous les personnages ne doivent utiliser qu'une seule fois la même action durant toute la durée du combat.]
 */
public class Econome extends Challenge {

    private final HashMap<Fighter, Stack<FightAction>> models;

    public Econome(Fight fight, FightTeam team) {
        super(fight, team);
        this.models = new HashMap(team.getMyFighters().size());
        this.currentActions = new Stack<>();
    }

    private Stack<FightAction> currentActions;


    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {
        if(fight.getFightWorker().round == 1){
            this.models.put(fighter, new Stack<>());
        }else{
            this.currentActions.clear();
        }
    }

    @Override
    public void onTurnEnd(Fighter fighter) {
        for (FightAction action : models.get(fighter)) {
            if(currentActions.stream().noneMatch(a -> a.getAction() == action.getAction() && a.getParam().equalsIgnoreCase(action.getParam()))){
                this.failChallenge();
            }
        }
    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {

    }

    @Override
    public void onFighterMove(Fighter fighter, MovementPath path) {
        /*if(fight.getFightWorker().round == 1){
            models.get(fighter).push(new FightAction(FightActionType.MOVE, String.valueOf(path.transitCells.size())));
        }else{
            currentActions.push(new FightAction(FightActionType.MOVE, String.valueOf(path.transitCells.size())));
        }MOVE iS not an action */
    }

    @Override
    public void onFighterSetCell(Fighter fighter, short startCell, short endCell) {

    }

    @Override
    public void onFighterCastSpell(Fighter fighter, SpellLevel spell) {
        if(fight.getFightWorker().round == 1){
            models.get(fighter).push(new FightAction(FightActionType.CAST_SPELL, String.valueOf(spell.getId())));
        }else{
            currentActions.push(new FightAction(FightActionType.CAST_SPELL, String.valueOf(spell.getId())));
        }
    }

    @Override
    public void onFighterCastWeapon(Fighter fighter, Weapon weapon) {
        if(fight.getFightWorker().round == 1){
            models.get(fighter).push(new FightAction(FightActionType.CAST_WEAPOM, String.valueOf(weapon.getId())));
        }else{
            currentActions.push(new FightAction(FightActionType.CAST_WEAPOM, String.valueOf(weapon.getId())));
        }
    }

    @Override
    public void onFighterTackled(Fighter fighter) {

    }

    @Override
    public void onFighterLooseLife(Fighter fighter, EffectCast cast, int damage) {

    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
