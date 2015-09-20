package koh.game.fights.fighters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import koh.game.Main;
import koh.game.dao.SpellDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.environments.cells.Zone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.EffectActivableObject;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layer.FightBomb;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberMonsterInformations;
import koh.protocol.types.game.context.fight.GameFightMonsterInformations;
import koh.protocol.types.game.look.EntityLook;
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
        super.AdjustStats();
        super.setLife(this.Life());
        super.setLifeMax(this.MaxLife());
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

    public boolean Boosted = false;

    public synchronized void SlefMurder(int Caster) {
        if (Boosted) {
            return;
        }
        int TotalCombo = 0;
        ArrayList<BombFighter> Targets = new ArrayList<>(6);
        for (short aCell : (new Zone(SpellShapeEnum.C, (byte) 2, MapPoint.fromCellId(this.CellId()).advancedOrientationTo(MapPoint.fromCellId(this.CellId()), true), this.Fight.Map)).GetCells(this.CellId())) {
            FightCell FightCell = Fight.GetCell(aCell);
            if (FightCell != null) {
                if (FightCell.HasGameObject(IFightObject.FightObjectType.OBJECT_STATIC)) {
                    for (Fighter Target : FightCell.GetObjectsAsFighter()) {
                        if (Target.ID == this.ID) {
                            continue;
                        }
                        if (Target instanceof BombFighter /*&& ((BombFighter) Target).Grade.monsterId == this.Grade.monsterId*/ && ((BombFighter) Target).Summoner == this.Summoner && !((BombFighter) Target).Boosted) {
                            Targets.add((BombFighter) Target);
                            TotalCombo += 40;
                        }
                    }
                }
            }
        }
        if (TotalCombo == 0) {
            return;
        }
        Fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 0, new String[]{"Combo : +" + TotalCombo + "% dommages d'explosion"}));
        Stats.AddBoost(StatsEnum.Combo_Dammages, TotalCombo);
        this.Boosted = true;
        for (Fighter Bomb : Targets) {
            Bomb.Stats.AddBoost(StatsEnum.Combo_Dammages, TotalCombo);
            Boosted = true;
        }
        Targets.forEach(Bomb -> Bomb.TryDie(Caster, true));
    }

    @Override
    public int TryDie(int CasterId, boolean force) {
        if (this.Life() <= 0 && !force) {
            if (this.Buffs.GetAllBuffs().anyMatch(x -> x.ActiveType == BuffActiveType.ACTIVE_ON_DIE)) {
                return this.Buffs.GetAllBuffs().filter(x -> x.ActiveType == BuffActiveType.ACTIVE_ON_DIE).findFirst().get().ApplyEffect(null, null);
            } else {
                if (this.FightBombs != null) {
                    this.FightBombs.forEach(Bomb -> Bomb.Remove());
                }
                return super.TryDie(CasterId, force);
            }
        }
        if (this.Life() <= 0 || force) {
            SlefMurder(CasterId);
            Fight.LaunchSpell(this, SpellDAO.Spells.get(SpellDAO.Bombs.get(this.Grade.monsterId).explodSpellId).SpellLevel(this.Grade.Grade), this.CellId(), true, true, false);
            if (this.FightBombs != null) {
                this.FightBombs.forEach(Bomb -> Bomb.Remove());
            }
        }
        return super.TryDie(CasterId, force);
    }

    public ArrayList<FightBomb> FightBombs;

    public void addBomb(FightBomb Bomb) {
        if (FightBombs == null) {
            FightBombs = new ArrayList<>(4);
        }
        this.FightBombs.add(Bomb);
    }

    @Override
    public int OnCellChanged() {
        if (this.myCell != null) {
            if (this.Dead()) {
                return -2;
            }
            if (this.FightBombs != null) {
                this.FightBombs.forEach(Bomb -> Bomb.Remove());
            }
            if (this.myCell.HasGameObject(FightObjectType.OBJECT_BOMB)) {
                Arrays.stream(this.myCell.GetObjects(FightObjectType.OBJECT_BOMB)).forEach(Object -> ((FightBomb) Object).Remove());
            }
            Short[] Cells;
            for (Fighter Friend : (Iterable<Fighter>) this.Team.GetAliveFighters().filter(Fighter -> (Fighter instanceof BombFighter) && Fighter.Summoner == this.Summoner && Pathfinder.InLine(null, this.CellId(), Fighter.CellId()) && this.Grade.monsterId == ((BombFighter) Fighter).Grade.monsterId)::iterator) {
                int Distance = Pathfinder.GoalDistance(null, CellId(), Friend.CellId());
                Main.Logs().writeDebug("Distance = " + Distance);
                if (Distance >= 2 && Distance <= 7) {
                    Cells = Pathfinder.GetLineCellsBetweenBomb(Fight, this.CellId(), Pathfinder.GetDirection(null, this.CellId(), Friend.CellId()), Friend.CellId(), false);
                    if (Cells != null) {
                        Cells = (Short[]) ArrayUtils.removeElement(Cells, this.CellId());
                        Cells = (Short[]) ArrayUtils.removeElement(Cells, Friend.CellId());
                        FightBomb Bomb = new FightBomb(this.Summoner, SpellDAO.Spells.get(SpellDAO.Bombs.get(Grade.monsterId).wallSpellId).SpellLevel(this.Grade.Grade), EffectActivableObject.GetColor(SpellDAO.Bombs.get(Grade.monsterId).wallSpellId), Cells, new BombFighter[]{this, (BombFighter) Friend});
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
