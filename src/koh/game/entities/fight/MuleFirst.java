package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

/**
 * Created by Melancholia on 9/25/16.
 * 48,La mule de plus faible niveau doit achever tous les adversaires (ça vous apprendra à "muler" comme un Porkass).
 */
public class MuleFirst extends Challenge{

    private Fighter shark;

    public MuleFirst(Fight fight, FightTeam team) {
        super(fight, team);
    }

    @Override
    public void onFightStart() {
        this.shark =  team.getAliveFighters()
                .filter(f -> f.isPlayer())
                .sorted((e1, e2) -> Integer.compare(e2.getLevel(), e1.getLevel()))
                .findFirst()
                .get();
    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {
        if(target.isEnnemyWith(shark) && killer != shark){
            this.failChallenge();
        }
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

    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
