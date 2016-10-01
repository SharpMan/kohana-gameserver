package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.Random;

/**
 * Created by Melancholia on 9/24/16.
 * 32=Toutes les attaques doivent être concentrées sur %1 jusqu'à ce qu'il meure.
 */
public class Elitiste extends Challenge {

    public Elitiste(Fight fight, FightTeam team) {
        super(fight, team);
    }

    @Override
    public void onFightStart() {
        this.target = this.getEnnemyTeam().getMyFighters().get(new Random().nextInt(this.getEnnemyTeam().getMyFighters().size()));
        this.sendSingleTarget();

    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {
        if(target == this.target){
            this.validate();
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
        if(target.isAlive() && fighter != target && fighter.isFriendlyWith(target) && cast.caster.isPlayer()){
            this.failChallenge();
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
