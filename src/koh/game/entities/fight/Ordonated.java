package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

/**
 * Created by Melancholia on 9/24/16.
 * 25,Les adversaires doivent être achevés dans l'ordre décroissant de leurs niveaux.
 */
public class Ordonated extends Challenge {

    public Ordonated(Fight fight, FightTeam team) {
        super(fight, team);
    }


    @Override
    public void onFightStart() {
        this.target = fight.getEnnemyTeam(team).getAliveFightersUnsummoned()
                .sorted((e1, e2) -> Integer.compare(e1.getLevel(), e2.getLevel()))
                .findFirst()
                .get();
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

        if(!target.hasSummoner() && target.getTeam() != team)
        {
            if(target != this.target){
                this.failChallenge();
                return;
            }
            this.target = fight.getEnnemyTeam(team).getAliveFightersUnsummoned()
                    .sorted((e1, e2) -> Integer.compare(e1.getLevel(), e2.getLevel()))
                    .findFirst()
                    .orElse(null);
            if(this.target != null)
                this.sendSingleTarget();
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