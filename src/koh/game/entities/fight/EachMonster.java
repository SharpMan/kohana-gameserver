package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;

import java.util.HashMap;

/**
 * Created by Melancholia on 9/25/16.
 * 46,Chacun son monstre,descriptionIdtype=Chaque personnage doit avoir achevé au moins un adversaire (qui ne soit pas une invocation) pendant le combat et lorsqu'un personnage attaque un adversaire, aucun autre personnage ne doit attaquer cet adversaire pendant toute la durée du combat.
 */
public class EachMonster extends Challenge {

    public EachMonster(Fight fight, FightTeam team) {
        super(fight, team);
        this.oppositions = new HashMap<>(8);
    }

    private HashMap<Fighter,Fighter> oppositions;
    private HashMap<Fighter, Integer> killReg;

    @Override
    public void onFightStart() {
        this.team.getAliveFighters().forEach(f -> killReg.put(f,0));
    }

    @Override
    public void onTurnStart(Fighter fighter) {

    }

    @Override
    public void onTurnEnd(Fighter fighter) {

    }

    @Override
    public void onFighterKilled(Fighter target, Fighter killer) {
        if(!killReg.containsKey(killer))
            return;
        final int finalScore = killReg.get(killer) + 1;
        killReg.replace(killer, finalScore);
        if(finalScore > 1){
            if(this.getEnnemyTeam().getAliveFighters().count() > this.team.getAliveFighters().count()){
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
        if(fighter.getTeam() != team && !cast.caster.hasSummoner()){
            if(oppositions.containsKey(fighter) && oppositions.get(fighter) != cast.caster){
                this.failChallenge();
            }
            else if(!oppositions.containsKey(fighter)){
                this.oppositions.put(fighter,cast.caster);
            }
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }

}
