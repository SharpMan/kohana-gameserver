package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

/**
 * Created by Melancholia on 8/28/16.
 * 6,Chaque joueur n'a le droit d'effectuer qu'une seule fois une même action pendant son tour de jeu.]
 */
public class Versatile extends Challenge {
    public Versatile(Fight fight, FightTeam team) {
        super(fight, team);
    }

    private FightAction action;

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {
        this.action = null;
    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {

    }

    @Override
    public void onFighterMove(Fighter fighter, MovementPath path) {

    }

    @Override
    public void onFighterSetCell(Fighter fighter, short startCell, short endCell) {

    }

    @Override
    public void onFighterCastSpell(Fighter fighter, SpellLevel spell) {
        if(action == null){
            this.action = new FightAction(FightActionType.CAST_SPELL, String.valueOf(spell.getId()));
        }else if(action.getAction() != FightActionType.CAST_SPELL || !action.getParam().equalsIgnoreCase(String.valueOf(spell.getId()))){
            this.failChallenge();
        }
    }

    @Override
    public void onFighterCastWeapon(Fighter fighter, Weapon weapon) {
        if(action == null){
            this.action = new FightAction(FightActionType.CAST_WEAPOM, String.valueOf(weapon.getId()));
        }else if(action.getAction() != FightActionType.CAST_WEAPOM || !action.getParam().equalsIgnoreCase(String.valueOf(weapon.getId()))){
            this.failChallenge();
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
