package koh.game.fights;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.environments.CrossZone;
import koh.game.entities.environments.cells.IZone;
import koh.game.entities.environments.cells.Lozenge;
import koh.game.entities.maps.pathfinding.LinkedCellsManager;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight.FightLoopState;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffPorter;
import koh.game.fights.effects.buff.BuffSpellDommage;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.IllusionFighter;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightPortal;
import koh.game.fights.types.AgressionFight;
import koh.protocol.client.Message;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.GameActionFightInvisibilityStateEnum;
import koh.protocol.client.enums.GameActionTypeEnum;
import koh.protocol.client.enums.SequenceTypeEnum;
import koh.protocol.client.enums.SpellShapeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDeathMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.FightEntityDispositionInformations;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.IdentifiedEntityDispositionInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.GameFightFighterInformations;
import koh.protocol.types.game.context.fight.GameFightMinimalStats;
import koh.protocol.types.game.context.fight.GameFightMinimalStatsPreparation;
import koh.protocol.types.game.look.EntityLook;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public abstract class Fighter extends IGameActor implements IFightObject {

    public Fighter(Fight Fight, Fighter Summoner) {
        this.Fight = Fight;
        this.Buffs = new FighterBuff();
        this.SpellsController = new FighterSpell(Buffs);
        this.States = new FighterState(this);
        this.Summoner = Summoner;
    }

    private static Random Random = new Random();
    public boolean Dead, Left;
    public FightTeam Team;
    public Fight Fight;
    public FightCell myCell;
    public int UsedAP, UsedMP, TurnRunning = 19;
    public GenericStats Stats;
    public Fighter Summoner;
    public GameActionFightInvisibilityStateEnum VisibleState = GameActionFightInvisibilityStateEnum.VISIBLE;
    public int[] previousPositions = new int[0];
    public CopyOnWriteArrayList<Short> previousCellPos = new CopyOnWriteArrayList<>(), previousFirstCellPos = new CopyOnWriteArrayList<>();
    private final MapPoint MapPointCache = MapPoint.fromCellId(0);
    public byte wave = 0;
    public FighterSpell SpellsController;
    public FighterBuff Buffs;
    public FighterState States;
    public AtomicInteger NextBuffUid = new AtomicInteger();

    /**
     * Virtual Method
     */
    public void EndFight() {

    }

    public MapPoint MapPoint() {
        return MapPointCache;
    }

    public int SetCell(FightCell Cell) {
        return this.SetCell(Cell, true);
    }

    public int SetCell(FightCell Cell, boolean RunEvent) {
        if (this.myCell != null) {
            this.myCell.RemoveObject(this); // On vire le fighter de la cell:
            if (this.myCell.HasGameObject(FightObjectType.OBJECT_PORTAL)) {
                ((FightPortal) this.myCell.GetObjects(FightObjectType.OBJECT_PORTAL)[0]).Enable(this, true);
            }
            if (this.Fight.FightState == FightState.STATE_PLACE) {
                if (!ArrayUtils.contains(previousPositions, myCell.Id)) {
                    this.previousPositions = ArrayUtils.add(previousPositions, this.myCell.Id);
                }
            } else {
                this.previousCellPos.add(this.myCell.Id);
            }
        }
        this.myCell = Cell;

        if (this.myCell != null) {
            this.MapPointCache.set_cellId(myCell.Id);
            int AddResult = this.myCell.AddObject(this, RunEvent);

            if (AddResult == -3 || AddResult == -2) {
                return AddResult;
            }
        }

        if (RunEvent) {
            int Result = OnCellChanged();
            if (Result != -1) {
                return Result;
            }
        }
        return this.TryDie(this.ID);
    }

    public int OnCellChanged() {
        if (this.myCell != null) {
            return this.Buffs.EndMove();
        }
        return -1;
    }

    protected void InitFighter(GenericStats StatsToMerge, int ID) {
        this.Stats = new GenericStats();
        this.Stats.merge(StatsToMerge);

        this.ID = ID;
    }

    /*public int GetFighterOutcome() {
     if(this.Left)
     return FightOutcomeEnum.RESULT_LOST;
     boolean flag1 = !this.Team.HasFighterAlive();
     boolean flag2 = !this.Team.HasFighterAlive();
     if (!flag1 && flag2) {
     return FightOutcomeEnum.RESULT_VICTORY;
     }
     return flag1 && !flag2 ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_DRAW;
     }*/
    public void LeaveFight() {
        if (this.Fight.FightState == FightState.STATE_PLACE) {
            // On le vire de l'equipe
            Team.FighterLeave(this);
        }

        this.Left = true;

        // On le vire de la cell
        myCell.RemoveObject(this);
    }

    public int TryDie(int CasterId) {
        return TryDie(CasterId, false);
    }

    public int TryDie(int CasterId, boolean force) {
        if (force) {
            this.setLife(0);
        }
        if (this.Life() <= 0) {
            this.Fight.StartSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH);
            //SendGameFightLeaveMessage
            this.Fight.sendToField(new GameActionFightDeathMessage(GameActionTypeEnum.FIGHT_KILLFIGHTER.value, CasterId, this.ID));

            this.Team.GetAliveFighters().filter(x -> x.Summoner != null && x.Summoner.ID == this.ID).forEach(Fighter -> Fighter.TryDie(this.ID, true));

            if (this.Fight.m_activableObjects.containsKey(this)) {
                this.Fight.m_activableObjects.get(this).stream().forEach(y -> y.Remove());

            }

            myCell.RemoveObject(this);

            this.Fight.EndSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH, false);

            if (this.Fight.TryEndFight()) {
                return -3;
            }
            if (this.Fight.CurrentFighter == this) {
                this.Fight.FightLoopState = FightLoopState.STATE_END_TURN;
            }
            return -2;
        }
        return -1;
    }

    public int BeginTurn() {
        if (this.Fight.m_activableObjects.containsKey(this)) {
            this.Fight.m_activableObjects.get(this).stream().filter((Object) -> (Object instanceof FightPortal)).forEach((Object) -> {
                ((FightPortal) Object).ForceEnable(this);
            });
        }

        int buffResult = this.Buffs.BeginTurn();
        if (buffResult != -1) {
            return buffResult;
        }
        this.previousFirstCellPos.add(this.myCell.Id);
        return myCell.BeginTurn(this);
    }

    public void MiddleTurn() {
        this.UsedAP = 0;
        this.UsedMP = 0;
    }

    public int EndTurn() {
        this.Fight.m_activableObjects.values().stream().forEach((Objects) -> {
            Objects.stream().filter((Object) -> (Object instanceof FightPortal)).forEach((Object) -> {
                ((FightPortal) Object).Enable(this);
            });
        });
        this.SpellsController.EndTurn();
        int buffResult = this.Buffs.EndTurn();
        if (buffResult != -1) {
            return buffResult;
        }
        return myCell.EndTurn(this);
    }

    public Stream<FightActivableObject> GetActivableObjects() {
        try {
            return this.Fight.m_activableObjects.get(this).stream();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public boolean Dead() {
        return this.Life() <= 0;
    }

    public int CurrentLife, CurrentLifeMax;

    public void setLife(int value) {
        this.CurrentLife = value - (this.Stats.getTotal(StatsEnum.Vitality) + this.Stats.getTotal(StatsEnum.Heal)) /*+ this.stats.getTotal(StatsEnum.AddVie)*/;
    }

    public int Life() {
        return this.CurrentLife + (this.Stats.getTotal(StatsEnum.Vitality) + this.Stats.getTotal(StatsEnum.Heal));
    }

    public void setLifeMax(int value) {
        this.CurrentLifeMax = value - (this.Stats.getTotal(StatsEnum.Vitality) + this.Stats.getTotal(StatsEnum.Heal));
    }

    public int MaxLife() {
        return this.CurrentLifeMax + this.Stats.getTotal(StatsEnum.Vitality) + this.Stats.getTotal(StatsEnum.Heal);
    }

    public int LifePercentage() {
        double percentage = ((double) Life() / (double) this.MaxLife());
        return (int) (percentage * 100);
    }

    public void ShowCell(int Cell, boolean team) {
        if (team) {
            this.Team.sendToField(new ShowCellMessage(this.ID, Cell));
        } else {
            this.Fight.sendToField(new ShowCellMessage(this.ID, Cell));
        }
    }

    public short NextCell;
    public byte NextDir;

    public abstract int Level();

    public abstract short MapCell();

    public boolean TurnReady = true;

    public boolean CanBeginTurn() {
        if (this.Dead()) {
            return false; // Mort    
        }
        return true;
    }

    public int MaxAP() {
        return this.Stats.getTotal(StatsEnum.ActionPoints);
    }

    public int MaxMP() {
        return this.Stats.getTotal(StatsEnum.MovementPoints);
    }

    public int AP() {
        return this.Stats.getTotal(StatsEnum.ActionPoints, this instanceof CharacterFighter) - this.UsedAP;

    }

    public int MP() {
        return this.Stats.getTotal(StatsEnum.MovementPoints, this instanceof CharacterFighter) - this.UsedMP;

    }

    public boolean IsAlive() {
        return this.Life() > 0 && !this.Left && !this.Dead;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightFighterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.Team.Id, this.wave, this.IsAlive(), this.GetGameFightMinimalStats(character), this.previousPositions);
    }

    /*public EntityDispositionInformations getEntityDispositionInformations(player character) {
     return new FightEntityDispositionInformations(character != null ? (this.IsVisibleFor(character) ? this.CellId() : -1) : this.CellId(), this.direction, GetCarriedActor());
     }*/
    public int APCancelling() {
        return (int) Math.floor((double) this.Stats.getTotal(StatsEnum.Wisdom) / 4) + this.Stats.getTotal(StatsEnum.Add_RETRAIT_PA);
    }

    public int MPCancelling() {
        return (int) Math.floor((double) this.Stats.getTotal(StatsEnum.Wisdom) / 4) + this.Stats.getTotal(StatsEnum.Add_RETRAIT_PM);
    }

    public int APDodge() {
        return (int) Math.floor((double) this.Stats.getTotal(StatsEnum.Wisdom) / 4) + this.Stats.getTotal(StatsEnum.DodgePALostProbability);
    }

    public int MPDodge() {
        return (int) Math.floor((double) this.Stats.getTotal(StatsEnum.Wisdom) / 4) + this.Stats.getTotal(StatsEnum.DodgePMLostProbability);
    }

    public int GetTackledMP() { //Sould be implements in Summoner,Monster return 0
        if (this.VisibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter Tackler : Pathfinder.GetEnnemyNear(Fight, Team, this.CellId(), true)) {
            if (num1 == 0.0) {
                num1 = this.GetTacklePercent(Tackler);
            } else {
                num1 *= this.GetTacklePercent(Tackler);
            }
        }
        if (num1 == 0.0) {
            return 0;
        }
        double num2 = 1.0 - num1;
        if (num2 < 0.0) {
            num2 = 0.0;
        } else if (num2 > 1.0) {
            num2 = 1.0;
        }
        return (int) Math.ceil((double) this.MP() * num2);
    }

    public Short[] GetCastZone(SpellLevel spellLevel) {
        int num = spellLevel.range;
        for (BuffEffect Buff : (Iterable<BuffEffect>) this.Buffs.GetAllBuffs().filter(Buff -> Buff instanceof BuffSpellDommage)::iterator) {
            num += Buff.CastInfos.Effect.value;
        }

        if (spellLevel.rangeCanBeBoosted) {
            int val1 = num + this.Stats.getTotal(StatsEnum.Add_Range);
            if (val1 < spellLevel.minRange) {
                val1 = spellLevel.minRange;
            }
            num = Math.min(val1, 280);
        }
        IZone shape;
        if (spellLevel.castInDiagonal && spellLevel.castInLine) {
            shape = new CrossZone((byte) spellLevel.minRange, (byte) num) {
                {
                    AllDirections = true;
                }
            };
        } else if (spellLevel.castInLine) {
            shape = new CrossZone((byte) spellLevel.minRange, (byte) num);
        } else if (spellLevel.castInDiagonal) {
            shape = new CrossZone((byte) spellLevel.minRange, (byte) num) {
                {
                    Diagonal = true;
                }
            };
        } else {
            shape = new Lozenge((byte) spellLevel.minRange, (byte) num, this.Fight.Map);
        }
        return shape.getCells(this.CellId());
    }

    public boolean HasState(int stateId) {
        return this.States.HasState(FightStateEnum.valueOf(stateId));
    }

    public int GetTackledAP() {
        if (this.VisibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter Tackler : Pathfinder.GetEnnemyNear(Fight, Team, this.CellId())) {
            {
                if (num1 == 0.0) {
                    num1 = this.GetTacklePercent(Tackler);
                } else {
                    num1 *= this.GetTacklePercent(Tackler);
                }
            }
        }
        if (num1 == 0.0) {
            return 0;
        }
        double num2 = 1.0 - num1;
        if (num2 < 0.0) {
            num2 = 0.0;
        } else if (num2 > 1.0) {
            num2 = 1.0;
        }
        return (int) Math.ceil((double) this.AP() * num2);
    }

    private double GetTacklePercent(Fighter tackler) {
        if (tackler.Stats.getTotal(StatsEnum.Add_TackleBlock) == -2) {
            return 0.0;
        } else {
            return (double) (this.Stats.getTotal(StatsEnum.Add_TackleEvade) + 2) / (2.0 * (double) (tackler.Stats.getTotal(StatsEnum.Add_TackleBlock) + 2));
        }
    }

    @Override
    public EntityDispositionInformations getEntityDispositionInformations(Player character) {
        return new FightEntityDispositionInformations(character != null ? (this.IsVisibleFor(character) ? this.CellId() : -1) : this.CellId(), this.direction, GetCarriedActor());
    }

    public int GetCarriedActor() {
        Optional<BuffEffect> Option = this.Buffs.GetAllBuffs().filter(x -> x instanceof BuffPorter && x.Duration != 0).findFirst();
        return Option.isPresent() ? Option.get().Caster.ID : 0;
    }

    public boolean IsVisibleFor(Player character) {
        return this.GetVisibleStateFor(character) != GameActionFightInvisibilityStateEnum.INVISIBLE.value;
    }

    public int shieldPoints() {
        return 0;
    }

    public int Summoner() {
        return this.Summoner == null ? 0 : this.Summoner.ID;
    }

    public byte GetVisibleStateFor(Player Character) {
        if (this.Team.GetAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.Summoner.ID == this.ID)) {
            return GameActionFightInvisibilityStateEnum.VISIBLE.value;
        } else if (Character == null || Character.client == null || Character.getFighter() == null || Character.getFight() != this.Fight) {
            return this.VisibleState.value;
        } else {
            return !Character.getFighter().IsFriendlyWith(this) || this.VisibleState == GameActionFightInvisibilityStateEnum.VISIBLE ? this.VisibleState.value : GameActionFightInvisibilityStateEnum.DETECTED.value;
        }
    }

    public boolean IsMyFriend(Player Character) {
        if (Character == null || Character.client == null || Character.getFighter() == null || Character.getFight() != this.Fight) {
            return false;
        }
        return Character.getFighter().IsFriendlyWith(this);
    }

    public int Initiative(boolean Base) { //FIXME : Considerate stats ?
        return (int) Math.floor((double) (this.MaxLife() / 4 + this.Stats.getTotal(StatsEnum.Initiative)) * (double) (this.Life() / this.MaxLife()));
    }

    public GameFightMinimalStats GetGameFightMinimalStats(Player character) {
        if (this.Fight.FightState == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.Life(), this.MaxLife(), (int) this.Stats.getBase(StatsEnum.Vitality), this.Stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.getTotal(StatsEnum.NeutralElementResistPercent), this.Stats.getTotal(StatsEnum.EarthElementResistPercent), this.Stats.getTotal(StatsEnum.WaterElementResistPercent), this.Stats.getTotal(StatsEnum.AirElementResistPercent), this.Stats.getTotal(StatsEnum.FireElementResistPercent), this.Stats.getTotal(StatsEnum.NeutralElementReduction), this.Stats.getTotal(StatsEnum.EarthElementReduction), this.Stats.getTotal(StatsEnum.WaterElementReduction), this.Stats.getTotal(StatsEnum.AirElementReduction), this.Stats.getTotal(StatsEnum.FireElementReduction), this.Stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.getTotal(StatsEnum.DodgePALostProbability), this.Stats.getTotal(StatsEnum.DodgePMLostProbability), this.Stats.getTotal(StatsEnum.Add_TackleBlock), this.Stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character), this.Initiative(false));
        }
        return new GameFightMinimalStats(this.Life(), this.MaxLife(), (int) this.Stats.getBase(StatsEnum.Vitality), this.Stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.getTotal(StatsEnum.NeutralElementResistPercent), this.Stats.getTotal(StatsEnum.EarthElementResistPercent), this.Stats.getTotal(StatsEnum.WaterElementResistPercent), this.Stats.getTotal(StatsEnum.AirElementResistPercent), this.Stats.getTotal(StatsEnum.FireElementResistPercent), this.Stats.getTotal(StatsEnum.NeutralElementReduction), this.Stats.getTotal(StatsEnum.EarthElementReduction), this.Stats.getTotal(StatsEnum.WaterElementReduction), this.Stats.getTotal(StatsEnum.AirElementReduction), this.Stats.getTotal(StatsEnum.FireElementReduction), this.Stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.getTotal(StatsEnum.DodgePALostProbability), this.Stats.getTotal(StatsEnum.DodgePMLostProbability), this.Stats.getTotal(StatsEnum.Add_TackleBlock), this.Stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character));
    }

    public IdentifiedEntityDispositionInformations GetIdentifiedEntityDispositionInformations() {
        return new IdentifiedEntityDispositionInformations(this.CellId(), this.direction, this.ID);
    }

    public abstract FightTeamMemberInformations GetFightTeamMemberInformations();

    @Override
    public abstract void send(Message Packet);

    public abstract void JoinFight();

    @Override
    public abstract EntityLook getEntityLook();

    @Override
    public FightObjectType ObjectType() {
        return FightObjectType.OBJECT_FIGHTER;
    }

    @Override
    public short CellId() {
        return this.myCell.Id;
    }

    @Override
    public boolean CanWalk() {
        return false;
    }

    @Override
    public boolean CanStack() {
        return false;
    }

    public boolean IsFriendlyWith(Fighter actor) {
        return actor.Team.Id == this.Team.Id;
    }

    public boolean IsEnnemyWith(Fighter actor) {
        return !this.IsFriendlyWith(actor);
    }

    public void CalculDamages(StatsEnum Effect, MutableInt Jet) {
        switch (Effect) {
            case Damage_Earth:
            case Steal_Earth:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Strength) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100 + this.Stats.getTotal(StatsEnum.AddDamagePhysic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Earth_Damages_Bonus)));
                break;
            case Damage_Earth_Per_Pm_Percent:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Strength) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100 + this.Stats.getTotal(StatsEnum.AddDamagePhysic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Earth_Damages_Bonus)) * (((double) (this.MP() / this.MaxMP())) * 100));
                break;
            case Damage_Neutral:
            case Steal_Neutral:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Strength) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamagePhysic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Neutral_Damages_Bonus)));
                break;
            case Damage_Neutral_Per_Pm_Percent:
                Jet.setValue(Math.floor((Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Strength) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamagePhysic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Neutral_Damages_Bonus)) * (((double) (this.MP() / this.MaxMP())) * 100)));
                break;
            case Damage_Fire:
            case Steal_Fire:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Intelligence) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus)));
                break;
            case Damage_Fire_Per_Pm_Percent:
                Jet.setValue(Math.floor((Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Intelligence) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))) * ((((double) this.MP() / (double) this.MaxMP()))));
                System.out.println((this.MP() / this.MaxMP()));
                System.out.println(Math.floor((Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Intelligence) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))));
                System.out.println(Math.floor((Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Intelligence) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))) * (((this.MP() / this.MaxMP()))));
                break;
            case Damage_Air:
            case Steal_Air:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Agility) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Air_Damages_Bonus)));
                break;
            case Damage_Air_Per_Pm_Percent:
                Jet.setValue(((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Agility) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Air_Damages_Bonus))) * (((double) (this.MP() / this.MaxMP())) * 100));
                break;
            case Damage_Water:
            case Steal_Water:
                Jet.setValue((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Chance) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Water_Damages_Bonus)));
                break;
            case Damage_Water_Per_Pm_Percent:
                Jet.setValue(((int) Math.floor(Jet.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Chance) + this.Stats.getTotal(StatsEnum.AddDamagePercent) + this.Stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.Stats.getTotal(StatsEnum.AddDamageMagic) + this.Stats.getTotal(StatsEnum.AllDamagesBonus) + this.Stats.getTotal(StatsEnum.Add_Water_Damages_Bonus))) * (((double) (this.MP() / this.MaxMP())) * 100));
                break;
        }

    }

    public static final byte EFFECTSHAPE_DEFAULT_AREA_SIZE = 1;

    public static final byte EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE = 0;

    public static final byte EFFECTSHAPE_DEFAULT_EFFICIENCY = 10;

    public static final byte EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY = 4;

    public double getPortalsSpellEfficiencyBonus(short param1, Fight Fight) {
        boolean _loc3_ = false;
        FightPortal[] _loc8_ = new FightPortal[0];
        int _loc9_ = 0;
        FightPortal _loc10_ = null;
        FightPortal _loc11_ = null;
        int _loc12_ = 0;
        double _loc13_ = 0;
        double _loc2_ = 1;

        for (CopyOnWriteArrayList<FightActivableObject> Objects : Fight.m_activableObjects.values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled) {
                    _loc8_ = ArrayUtils.add(_loc8_, (FightPortal) Object);
                    if (Object.CellId() == param1) {
                        _loc3_ = true;
                    }
                }
            }
        }

        if (!_loc3_) {
            return _loc2_;
        }
        final int[] _loc6_ = LinkedCellsManager.getLinks(MapPoint.fromCellId(param1), Arrays.stream(_loc8_).map(x -> x.MapPoint()).toArray(MapPoint[]::new));
        int _loc7_ = _loc6_.length;
        if (_loc7_ > 1) {
            while (_loc9_ < _loc7_) {
                _loc10_ = _loc8_[_loc9_];
                _loc12_ = Math.max(_loc12_, _loc10_.damageValue);
                if (_loc11_ != null) {
                    _loc13_ = _loc13_ + MapPoint.fromCellId(_loc10_.CellId()).distanceToCell(MapPoint.fromCellId(_loc11_.CellId()));
                }
                _loc11_ = _loc10_;
                _loc9_++;
            }
            _loc2_ = 1 + (_loc12_ + _loc8_.length * _loc13_) / 100;
        }
        return _loc2_;
    }

    public void CalculBonusDamages(EffectInstanceDice Effect, MutableInt Jet, short CastCell, short TargetCell, short TruedCell) {

        double Bonus = this.Stats.getTotal(StatsEnum.Add_Damage_Final_Percent);

        Bonus += getShapeEfficiency(Effect.zoneShape(), CastCell, TargetCell, Effect.ZoneSize() != -100000 ? Effect.ZoneSize() : EFFECTSHAPE_DEFAULT_AREA_SIZE, Effect.zoneMinSize() != -100000 ? Effect.zoneMinSize() : EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE, Effect.zoneEfficiencyPercent() != -100000 ? Effect.zoneEfficiencyPercent() : EFFECTSHAPE_DEFAULT_EFFICIENCY, Effect.zoneMaxEfficiency() != -100000 ? Effect.zoneMaxEfficiency() : EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY);

        Bonus *= getPortalsSpellEfficiencyBonus(TruedCell, this.Fight);

        Jet.setValue((Jet.floatValue() * Bonus));
    }

    public static double getShapeEfficiency(int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        if (SpellShapeEnum.valueOf(param1) == null) {
            return getSimpleEfficiency(Pathfinder.getDistance(param2, param3), param4, param5, param6, param7);
        }
        int _loc8_ = 0;

        switch (SpellShapeEnum.valueOf(param1)) {
            case A:
            case a:
            case Z:
            case I:
            case O:
            case semicolon:
            case empty:
            case P:
                return DAMAGE_NOT_BOOSTED;
            case B:
            case V:
            case G:
            case W:
                _loc8_ = Pathfinder.getSquareDistance(param2, param3);
                break;
            case minus:
            case plus:
            case U:
                _loc8_ = Pathfinder.getDistance(param2, param3) / 2;
                break;
            default:
                _loc8_ = Pathfinder.getDistance(param2, param3);
        }
        return getSimpleEfficiency(_loc8_, param4, param5, param6, param7);
    }

    private static final int DAMAGE_NOT_BOOSTED = 1, UNLIMITED_ZONE_SIZE = 50;

    public static double getSimpleEfficiency(int param1, int param2, int param3, int param4, int param5) {
        if (param4 == 0) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param2 <= 0 || param2 >= UNLIMITED_ZONE_SIZE) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param1 > param2) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param4 <= 0) {
            return DAMAGE_NOT_BOOSTED;
        }
        if (param3 != 0) {
            if (param1 <= param3) {
                return DAMAGE_NOT_BOOSTED;
            }
            return Math.max(0, DAMAGE_NOT_BOOSTED - 0.01 * Math.min(param1 - param3, param5) * param4);
        }
        return Math.max(0, DAMAGE_NOT_BOOSTED - 0.01 * Math.min(param1, param5) * param4);
    }

    public void CalculReduceDamages(StatsEnum Effect, MutableInt Damages) {
        switch (Effect) {
            case Damage_Neutral:
            case Steal_Neutral:
                Damages.setValue(Damages.intValue() * (100 - this.Stats.getTotal(StatsEnum.NeutralElementResistPercent) - (Fight instanceof AgressionFight ? Stats.getTotal(StatsEnum.PvpNeutralElementResistPercent) : 0)) / 100
                        - this.Stats.getTotal(StatsEnum.NeutralElementReduction) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpNeutralElementReduction) : 0) - this.Stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Earth:
            case Steal_Earth:
                Damages.setValue(Damages.intValue() * (100 - this.Stats.getTotal(StatsEnum.EarthElementResistPercent) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpEarthElementResistPercent) : 0)) / 100
                        - this.Stats.getTotal(StatsEnum.EarthElementReduction) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpEarthElementReduction) : 0) - this.Stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Fire:
            case Steal_Fire:
                Damages.setValue(Damages.intValue() * (100 - this.Stats.getTotal(StatsEnum.FireElementResistPercent) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpFireElementResistPercent) : 0)) / 100
                        - this.Stats.getTotal(StatsEnum.FireElementReduction) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpFireElementReduction) : 0) - this.Stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Air:
            case Steal_Air:
                Damages.setValue(Damages.intValue() * (100 - this.Stats.getTotal(StatsEnum.AirElementResistPercent) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpAirElementResistPercent) : 0)) / 100
                        - this.Stats.getTotal(StatsEnum.AirElementReduction) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpAirElementReduction) : 0) - this.Stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Water:
            case Steal_Water:
                Damages.setValue(Damages.intValue() * (100 - this.Stats.getTotal(StatsEnum.WaterElementResistPercent) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpWaterElementResistPercent) : 0)) / 100
                        - this.Stats.getTotal(StatsEnum.WaterElementReduction) - (Fight instanceof AgressionFight ? this.Stats.getTotal(StatsEnum.PvpWaterElementReduction) : 0) - this.Stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;
        }
    }

    /// <summary>
    /// Calcul des PA/PM perdus
    /// </summary>
    /// <param name="Caster"></param> TODO: Supprimer les appel variable inutile qui occupent de la mémoire et referencer direct les var
    /// <param name="LostPoint"></param>
    /// <param name="MP"></param>
    /// <returns></returns>
    public int CalculDodgeAPMP(Fighter Caster, int LostPoint, boolean MP, boolean isBuff) {
        int RealLostPoint = 0;

        if (!MP) {
            int DodgeAPCaster = Caster.APCancelling() + 1;
            int DodgeAPTarget = this.APDodge() + 1;

            for (int i = 0; i < LostPoint; i++) {
                int ActualAP = (isBuff && this.ID == this.Fight.CurrentFighter.ID ? this.MaxAP() : this.AP()) - RealLostPoint;
                int realAP = AP();
                if (realAP == 0) {
                    realAP = 1;
                }
                double PercentLastAP = ActualAP / realAP;
                double Chance = 0.5 * (DodgeAPCaster / DodgeAPTarget) * PercentLastAP;
                int PercentChance = (int) (Chance * 100);

                if (PercentChance > 100) {
                    PercentChance = 90;
                }
                if (PercentChance < 10) {
                    PercentChance = 10;
                }

                if (Random.nextInt(99) < PercentChance) {
                    RealLostPoint++;
                }
            }
        } else {
            int DodgeMPCaster = Caster.MPCancelling() + 1;
            int DodgeMPTarget = this.MPDodge() + 1;

            for (int i = 0; i < LostPoint; i++) {
                int ActualMP = (isBuff && this.ID == this.Fight.CurrentFighter.ID ? this.MaxMP() : this.MP()) - RealLostPoint;
                int realMP = MP();
                if (realMP == 0) {
                    realMP = 1;
                }
                double PercentLastMP = ActualMP / realMP;
                double Chance = 0.5 * (DodgeMPCaster / DodgeMPTarget) * PercentLastMP;
                int PercentChance = (int) (Chance * 100);

                if (PercentChance > 100) {
                    PercentChance = 90;
                }
                if (PercentChance < 10) {
                    PercentChance = 10;
                }

                if (Random.nextInt(99) < PercentChance) {
                    RealLostPoint++;
                }
            }
        }

        return RealLostPoint;
    }

    public void CalculHeal(MutableInt Heal) {
        Heal.setValue(Math.floor(Heal.doubleValue() * (100 + this.Stats.getTotal(StatsEnum.Intelligence)) / 100 + this.Stats.getTotal(StatsEnum.Add_Heal_Bonus)));
    }

    public int CalculArmor(StatsEnum DamageEffect) {
        /*switch (DamageEffect) {
         default:
         return this.stats.getTotal(StatsEnum.AddArmor);
         }*/
        return this.Stats.getTotal(StatsEnum.AddArmor);
    }

    public int ReflectDamage() {
        return ((1 + (this.Stats.getTotal(StatsEnum.Wisdom) / 100)) * this.Stats.getTotal(StatsEnum.Damage_Return)) + this.Stats.getTotal(StatsEnum.DamageReflection);
    }

    @Override
    public Integer Priority() {
        return 0;
    }

    @Override
    public boolean CanGoThrough() {
        return false;
    }

}
