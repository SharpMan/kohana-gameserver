package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.StatsEnum;

/**
 * Created by Melancholia on 8/29/16.
 * 20,Utiliser le même élément d'attaque pendant toute la durée du combat.
 */
public class Elemantaire extends Challenge {

    public Elemantaire(Fight fight, FightTeam team) {
        super(fight, team);
    }


    private StatsEnum firstElement;

    @Override
    public void onFightStart() {

    }

    @Override
    public void onTurnStart(Fighter fighter) {

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
            if(cast.caster.getTeam() != team || !cast.caster.isPlayer())
                return;
        StatsEnum general;
        switch (cast.effectType) {
            case DAMAGE_NEUTRAL:
            case STEAL_NEUTRAL:
            case LIFE_LEFT_TO_THE_ATTACKER_NEUTRAL_DAMAGES:
                general =StatsEnum.DAMAGE_NEUTRAL;
                break;

            case DAMAGE_EARTH:
            case STEAL_EARTH:
            case LIFE_LEFT_TO_THE_ATTACKER_EARTH_DAMAGES:
                general = StatsEnum.DAMAGE_EARTH;
                break;

            case DAMAGE_FIRE:
            case STEAL_FIRE:
            case LIFE_LEFT_TO_THE_ATTACKER_FIRE_DAMAGES:
                general = StatsEnum.DAMAGE_FIRE;
                break;

            case DAMAGE_AIR:
            case STEAL_AIR:
            case LIFE_LEFT_TO_THE_ATTACKER_AIR_DAMAGES:
            case PA_USED_LOST_X_PDV:
                general = StatsEnum.DAMAGE_AIR;
                break;

            case DAMAGE_WATER:
            case STEAL_WATER:
            case LIFE_LEFT_TO_THE_ATTACKER_WATER_DAMAGES:
                general = StatsEnum.DAMAGE_WATER;
                break;
            default:
                return;
        }
        if(firstElement == null){
            this.firstElement = general;
        }else{
            if(this.firstElement != general){
                this.failChallenge();
            }
        }
    }

    @Override
    public void onFighterHealed(Fighter fighter, EffectCast cast, int heal) {

    }
}
