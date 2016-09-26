package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 9/25/16.
 * 47,Dès qu'un allié perd des points de vie ou des points de bouclier, vous avez 5 tours de jeu pour achever votre allié.
 */
public class Contamination extends Challenge {
    public Contamination(Fight fight, FightTeam team) {
        super(fight, team);
        this.register = new HashMap<>(7);
    }

    private Map<Fighter,Integer> register;

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {
        if (register.containsKey(fighter) && register.get(fighter) + 5 < fight.getFightWorker().round) {
            this.failChallenge();
        }
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

    }

    @Override
    public void onFighterCastWeapon(Fighter fighter, Weapon weapon) {

    }

    @Override
    public void onFighterTackled(Fighter fighter) {

    }

    @Override
    public void onFighterLooseLife(Fighter fighter, EffectCast cast, int damage) {
        if(fighter.getTeam() == team && !register.containsKey(fighter) &&  fighter.isEnnemyWith(cast.caster)){
            this.register.put(fighter, fighter.getFight().getFightWorker().round);
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
