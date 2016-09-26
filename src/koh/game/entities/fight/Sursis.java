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
 * Created by Melancholia on 8/28/16.
 * 4,Tuer %1 en dernier.
 */
public class Sursis extends Challenge {
    public Sursis(Fight fight, FightTeam team) {
        super(fight, team);
    }


    @Override
    public void onFightStart() {
        final int rnd = (new Random().nextInt(fight.getEnnemyTeam(team).getMyFighters().size()));
        this.target =  fight.getEnnemyTeam(team).getMyFighters().get(rnd);
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
            if(target.getTeam().getAliveFighters().filter(f -> !f.hasSummoner()).count() == 1L){
                this.validate();
            }else{
                this.failChallenge();
            }
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
