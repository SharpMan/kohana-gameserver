package koh.game.fights.fighters;

import java.util.ArrayList;
import java.util.Arrays;
import koh.game.Main;
import koh.game.dao.SpellDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectActivableObject;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.layer.FightBomb;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellableEffectMessage;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Couple;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BombFighter extends StaticFighter {

    public BombFighter(Fight Fight, Fighter Summoner, MonsterGrade Monster) {
        super(Fight, Summoner);
        this.Grade = Monster;
        super.InitFighter(this.Grade.GetStats(), Fight.GetNextContextualId());
        this.entityLook = EntityLookParser.Copy(this.Grade.Monster().GetEntityLook());
        this.AdjustStats();
        super.setLife(this.Life());
    }

    private void AdjustStats() {
        this.Stats.GetEffect(StatsEnum.Vitality).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Vitality).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
        this.Stats.GetEffect(StatsEnum.Intelligence).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Intelligence).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
        this.Stats.GetEffect(StatsEnum.Chance).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Chance).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
        this.Stats.GetEffect(StatsEnum.Strength).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Strength).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
        this.Stats.GetEffect(StatsEnum.Agility).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Agility).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
        this.Stats.GetEffect(StatsEnum.Wisdom).Base = (short) ((double) this.Stats.GetEffect(StatsEnum.Wisdom).Base * (1.0 + (double) this.Summoner.Level() / 100.0));
    }

    @Override
    public void CalculDamages(StatsEnum Effect, MutableInt Jet) {
        switch (Effect) {
            case Damage_Earth:
            case Steal_Earth:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.GetTotal(StatsEnum.Strength) + this.Stats.GetTotal(StatsEnum.Combo_Dammages)) / 100 + this.Stats.GetTotal(StatsEnum.AddDamagePhysic) + this.Stats.GetTotal(StatsEnum.AllDamagesBonus) + this.Stats.GetTotal(StatsEnum.Add_Earth_Damages_Bonus)));
                break;
            case Damage_Neutral:
            case Steal_Neutral:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.GetTotal(StatsEnum.Strength) + this.Stats.GetTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.Stats.GetTotal(StatsEnum.AddDamagePhysic) + this.Stats.GetTotal(StatsEnum.AllDamagesBonus) + this.Stats.GetTotal(StatsEnum.Add_Neutral_Damages_Bonus)));
                break;

            case Damage_Fire:
            case Steal_Fire:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.GetTotal(StatsEnum.Intelligence) + this.Stats.GetTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.Stats.GetTotal(StatsEnum.AddDamageMagic) + this.Stats.GetTotal(StatsEnum.AllDamagesBonus) + this.Stats.GetTotal(StatsEnum.Add_Fire_Damages_Bonus)));
                break;

            case Damage_Air:
            case Steal_Air:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.GetTotal(StatsEnum.Agility) + this.Stats.GetTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.Stats.GetTotal(StatsEnum.AddDamageMagic) + this.Stats.GetTotal(StatsEnum.AllDamagesBonus) + this.Stats.GetTotal(StatsEnum.Add_Air_Damages_Bonus)));
                break;

            case Damage_Water:
            case Steal_Water:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.GetTotal(StatsEnum.Chance) + this.Stats.GetTotal(StatsEnum.Combo_Dammages)) / 100
                        + this.Stats.GetTotal(StatsEnum.AddDamageMagic) + this.Stats.GetTotal(StatsEnum.AllDamagesBonus) + this.Stats.GetTotal(StatsEnum.Add_Water_Damages_Bonus)));
                break;
        }
    }

    @Override
    public int TryDie(int CasterId, boolean force) {
        if (this.Life() <= 0 || force) {
            Fight.LaunchSpell(this, SpellDAO.Spells.get(SpellDAO.Bombs.get(this.Grade.monsterId).explodSpellId).SpellLevel(this.Grade.Grade), this.CellId(), true, true, false);
        }
        return super.TryDie(CasterId, force);
    }
    
    public ArrayList<FightBomb> FightBombs;
    
    
    public void addBomb(FightBomb Bomb){
        if(FightBombs == null){
            FightBombs = new ArrayList<>(4);
        }
        this.FightBombs.add(Bomb);
    }

    @Override
    public int OnCellChanged() {
        if (this.myCell != null) {
            if(this.FightBombs != null){
                this.FightBombs.forEach(Bomb -> Bomb.Remove());
            }
            Short[] Cells;
            for (Fighter Friend : (Iterable<Fighter>) this.Team.GetAliveFighters().filter(Fighter -> (Fighter instanceof BombFighter) && Fighter.Summoner == this.Summoner && Pathfinder.InLine(null, this.CellId(), Fighter.CellId()) && this.Grade.monsterId == ((BombFighter)Fighter).Grade.monsterId)::iterator) {
                int Distance = Pathfinder.GoalDistance(null, CellId(), Friend.CellId());
                Main.Logs().writeDebug("Distance = " + Distance);
                if (Distance >= 2 && Distance <= 6) {
                    Cells = Pathfinder.GetLineCellsBetween(Fight, this.CellId(), Pathfinder.GetDirection(null, this.CellId(), Friend.CellId()), Friend.CellId());
                    if (Cells != null) {
                        Cells = (Short[])ArrayUtils.removeElement(Cells, this.CellId());
                        Cells = (Short[])ArrayUtils.removeElement(Cells, Friend.CellId());
                        FightBomb Bomb = new FightBomb(this.Summoner, SpellDAO.Spells.get(SpellDAO.Bombs.get(Grade.monsterId).wallSpellId).SpellLevel(this.Grade.Grade), EffectActivableObject.GetColor(SpellDAO.Bombs.get(Grade.monsterId).wallSpellId), Cells,new BombFighter[]{this,(BombFighter)Friend});
                        Fight.AddActivableObject(this.Summoner, Bomb);
                    }
                }
            }

            return this.Buffs.EndMove();
        }
        return -1;
    }

    @Override
    public int BeginTurn() {
        super.onBeginTurn();
        return super.BeginTurn();
    }

    @Override
    public int Level() {
        return this.Grade.Level;
    }

    @Override
    public short MapCell() {
        return 0;
    }

    @Override
    public GameContextActorInformations GetGameContextActorInformations(Player character) {
        return new GameFightMonsterInformations(this.ID, this.GetEntityLook(), this.GetEntityDispositionInformations(character), this.Team.Id, this.wave, this.IsAlive(), this.GetGameFightMinimalStats(character), this.previousPositions, this.Grade.monsterId, this.Grade.Grade);
    }

    @Override
    public FightTeamMemberInformations GetFightTeamMemberInformations() {
        return new FightTeamMemberMonsterInformations(this.ID, this.Grade.monsterId, this.Grade.Grade);
    }

    @Override
    public void Send(Message Packet) {

    }

    @Override
    public void JoinFight() {

    }

    @Override
    public EntityLook GetEntityLook() {
        return this.entityLook;
    }

}
