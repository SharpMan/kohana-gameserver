package koh.game.entities.fight;

import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.item.Weapon;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.FightTypeEnum;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.context.fight.challenge.ChallengeTargetsListMessage;
import lombok.Generated;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Melancholia on 8/28/16.
 * Review survivant for summonings
 */
public abstract class Challenge {

    protected final Fight fight;
    protected final FightTeam team;
    /*@Generated*/ @Getter
    protected Fighter target;

    public Challenge(Fight fight, FightTeam team) {
        this.fight = fight;
        this.team = team;
    }

    public abstract void onFightStart();
    //only for team
    public abstract void onTurnStart(Fighter fighter);
    public abstract void onTurnEnd(Fighter fighter);
    public abstract void onFighterKilled(Fighter target, Fighter killer);
    public abstract void onFighterMove(Fighter fighter, MovementPath path);
    public abstract void onFighterSetCell(Fighter fighter, short startCell, short endCell);
    public abstract void onFighterCastSpell(Fighter fighter, SpellLevel spell);
    public abstract void onFighterCastWeapon(Fighter fighter, Weapon weapon);
    public abstract void onFighterTackled(Fighter fighter);
    //TODO must be greather than 0 and not summoned the caster
    public abstract void onFighterLooseLife(Fighter fighter, EffectCast cast, int damage);
    //TODO if aster is summoned pass
    public abstract void onFighterHealed(Fighter fighter,EffectCast cast, int heal);

    public void failChallenge(){
        //TODO: call fight
    }

    public void validate(){

    }

    public void sendSingleTarget(){
        this.fight.sendToField(new ChallengeTargetsListMessage(new int[]{ target.getID()}, new short[] { target.getCellId()}));
    }


    protected FightTeam getEnnemyTeam(){
        return fight.getEnnemyTeam(team);
    }


    public static boolean canBeUsed(Fight fight, FightTeam team, int id){
        switch (id){
            case 48:
            case 30:
            case 47:
                if(fight.getEnnemyTeam(team).getFighters().count() < 2L || team.getFighters().count() < 2L){
                    return false;
                }
            case 3:
            case 4:
            case 10:
            case 42:
            case 49:
                if(fight.getEnnemyTeam(team).getFighters().count() < 2L){
                    return false;
                }
            case 36:
                if(team.getFighters().count() < 2L){
                    return false;
                }
                //One target have to be close to the fighter
                if(!team.getFighters()
                        .allMatch(f -> Pathfunction.getCircleZone(f.getCellId(),f.getMP())
                                .stream()
                                .map(d -> f.getFight().getCell(d))
                                .anyMatch(fightCell -> fightCell.hasEnnemy(team) != null)
                        )){
                    return false;
                }
            case 37:
                if(team.getFighters().count() < 2L){
                    return false;
                }
            case 7: //TODO 70% players
                if(team.getFighters().filter(f-> f.isPlayer() && f.getPlayer().getMySpells().hasSpell(Jardinier.SPELL)).count() < 2L){
                    return false;
                }
            case 23:
                if(team.getFighters().anyMatch(f-> f.isPlayer() && f.getPlayer().getMySpells().getSpells().stream().anyMatch(sp ->
                        Arrays.stream(sp.getEffects()).anyMatch(e -> e.getEffectType() == StatsEnum.SUB_RANGE)))){
                    return false;
                }
            case 12:
                if(team.getFighters().filter(f-> f.isPlayer() && f.getPlayer().getMySpells().hasSpell(Fossoyeur.SPELL)).count() < 2L){
                    return false;
                }
            case 14:
                if(team.getFighters().anyMatch(f-> f.isPlayer() && f.getPlayer().getMySpells().hasSpell(RoyalCasino.SPELL))){
                    return false;
                }
            case 15://id
                if(team.getFighters().anyMatch(f-> f.isPlayer() && f.getPlayer().getMySpells().hasSpell(Araknophile.SPELL))){
                    return false;
                }

            default:
                return true;
        }
    }


    public static int getXPBonus(int id){
        switch (id){
            case 9: //60-75
                return 70;
            case 10: //40-80
                return 60;
            case 5: // 160 - 280
                return 170;
            case 12: // 10-20
                return 20;
            case 7:  //10-18
                return 18;
            case 11: //50-85
                return 73;
            case 8: // 25-55
                return 40;
            case 2: //25-55
                return 40;
            case 4: //20-55
                return 40;
            case 6: //50-85
                return 70;
            case 1: //25-55
                return 37;
            default:
                return 50;


        }
    }


}
