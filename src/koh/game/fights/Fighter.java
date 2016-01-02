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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public abstract class Fighter extends IGameActor implements IFightObject {

    public Fighter(Fight fight, Fighter summoner) {
        this.fight = fight;
        this.buff = new FighterBuff();
        this.spellsController = new FighterSpell(buff);
        this.states = new FighterState(this);
        this.summoner = summoner;
    }

    @Getter
    private static final Random RANDOM = new Random();

    protected boolean dead;
    @Setter @Getter
    protected boolean left;
    @Getter @Setter
    protected FightTeam team;
    @Getter @Setter
    protected Fight fight;
    @Getter @Setter
    protected FightCell myCell;
    @Getter @Setter
    protected int usedAP, usedMP, turnRunning = 19;
    @Getter
    protected GenericStats stats;
    @Getter
    protected Fighter summoner;
    @Getter @Setter
    protected GameActionFightInvisibilityStateEnum visibleState = GameActionFightInvisibilityStateEnum.VISIBLE;
    protected int[] previousPositions = new int[0];
    @Getter
    protected CopyOnWriteArrayList<Short> previousCellPos = new CopyOnWriteArrayList<>(), previousFirstCellPos = new CopyOnWriteArrayList<>();
    private final MapPoint mapPointCache = MapPoint.fromCellId(0);
    @Getter
    protected final byte wave = 0; //Dimension obscure
    @Getter
    protected FighterSpell spellsController;
    @Getter
    protected FighterBuff buff;
    @Getter
    protected FighterState states;
    @Getter
    protected AtomicInteger nextBuffUid = new AtomicInteger();
    protected short nextCell;
    protected byte nextDir;
    @Getter @Setter
    protected boolean turnReady = true;

    /**
     * Virtual Method
     */
    public void endFight() {

    }

    public MapPoint getMapPoint() {
        return mapPointCache;
    }

    public int setCell(FightCell cell) {
        return this.setCell(cell, true);
    }

    public int setCell(FightCell cell, boolean runEvent) {
        if (this.myCell != null) {
            this.myCell.RemoveObject(this); // On vire le fighter de la cell:
            if (this.myCell.HasGameObject(FightObjectType.OBJECT_PORTAL)) {
                ((FightPortal) this.myCell.GetObjects(FightObjectType.OBJECT_PORTAL)[0]).enable(this, true);
            }
            if (this.fight.getFightState() == FightState.STATE_PLACE) {
                if (!ArrayUtils.contains(previousPositions, myCell.Id)) {
                    this.previousPositions = ArrayUtils.add(previousPositions, this.myCell.Id);
                }
            } else {
                this.previousCellPos.add(this.myCell.Id);
            }
        }
        this.myCell = cell;

        if (this.myCell != null) {
            this.mapPointCache.set_cellId(myCell.Id);
            int AddResult = this.myCell.AddObject(this, runEvent);

            if (AddResult == -3 || AddResult == -2) {
                return AddResult;
            }
        }

        if (runEvent) {
            int Result = onCellChanged();
            if (Result != -1) {
                return Result;
            }
        }
        return this.tryDie(this.ID);
    }

    public int onCellChanged() {
        if (this.myCell != null) {
            return this.buff.endMove();
        }
        return -1;
    }

    protected void initFighter(GenericStats statsToMerge, int ID) {
        this.stats = new GenericStats();
        this.stats.merge(statsToMerge);

        this.ID = ID;
    }

    public boolean isMarkedDead(){
        return this.isDead(); //TODO delete above dead function
    }

    /*public int GetFighterOutcome() {
     if(this.left)
     return FightOutcomeEnum.RESULT_LOST;
     boolean flag1 = !this.team.hasFighterAlive();
     boolean flag2 = !this.team.hasFighterAlive();
     if (!flag1 && flag2) {
     return FightOutcomeEnum.RESULT_VICTORY;
     }
     return flag1 && !flag2 ? FightOutcomeEnum.RESULT_LOST : FightOutcomeEnum.RESULT_DRAW;
     }*/
    public void leaveFight() {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            // On le vire de l'equipe
            team.fighterLeave(this);
        }

        this.left = true;

        // On le vire de la cell
        myCell.RemoveObject(this);
    }

    public int tryDie(int casterId) {
        return tryDie(casterId, false);
    }

    public int tryDie(int casterId, boolean force) {
        if (force) {
            this.setLife(0);
        }
        if (this.getLife() <= 0) {
            this.fight.startSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH);
            //SendGameFightLeaveMessage
            this.fight.sendToField(new GameActionFightDeathMessage(GameActionTypeEnum.FIGHT_KILLFIGHTER.value, casterId, this.ID));

            this.team.getAliveFighters().filter(x -> x.summoner != null && x.summoner.getID() == this.ID).forEach(Fighter -> Fighter.tryDie(this.ID, true));

            if (this.fight.getActivableObjects().containsKey(this)) {
                this.fight.getActivableObjects().get(this).stream().forEach(y -> y.remove());

            }

            myCell.RemoveObject(this);

            this.fight.endSequence(SequenceTypeEnum.SEQUENCE_CHARACTER_DEATH, false);

            if (this.fight.tryEndFight()) {
                return -3;
            }
            if (this.fight.getCurrentFighter() == this) {
                this.fight.fightLoopState = FightLoopState.STATE_END_TURN;
            }
            return -2;
        }
        return -1;
    }

    public int beginTurn() {
        if (this.fight.getActivableObjects().containsKey(this)) {
            this.fight.getActivableObjects().get(this).stream().filter((Object) -> (Object instanceof FightPortal)).forEach((Object) -> {
                ((FightPortal) Object).ForceEnable(this);
            });
        }

        int buffResult = this.buff.beginTurn();
        if (buffResult != -1) {
            return buffResult;
        }
        this.previousFirstCellPos.add(this.myCell.Id);
        return myCell.BeginTurn(this);
    }

    public void middleTurn() {
        this.usedAP = 0;
        this.usedMP = 0;
    }

    public int endTurn() {
        this.fight.getActivableObjects().values().stream().forEach((Objects) -> {
            Objects.stream().filter((Object) -> (Object instanceof FightPortal)).forEach((Object) -> {
                ((FightPortal) Object).enable(this);
            });
        });
        this.spellsController.endTurn();
        int buffResult = this.buff.endTurn();
        if (buffResult != -1) {
            return buffResult;
        }
        return myCell.EndTurn(this);
    }

    public Stream<FightActivableObject> getActivableObjects() {
        try {
            return this.fight.getActivableObjects().get(this).stream();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public boolean isDead() {
        return this.getLife() <= 0;
    }

    public int currentLife, currentLifeMax;

    public void setLife(int value) {
        this.currentLife = value - (this.stats.getTotal(StatsEnum.Vitality) + this.stats.getTotal(StatsEnum.Heal)) /*+ this.stats.getTotal(StatsEnum.AddVie)*/;
    }

    public int getLife() {
        return this.currentLife + (this.stats.getTotal(StatsEnum.Vitality) + this.stats.getTotal(StatsEnum.Heal));
    }

    public void setLifeMax(int value) {
        this.currentLifeMax = value - (this.stats.getTotal(StatsEnum.Vitality) + this.stats.getTotal(StatsEnum.Heal));
    }

    public int getMaxLife() {
        return this.currentLifeMax + this.stats.getTotal(StatsEnum.Vitality) + this.stats.getTotal(StatsEnum.Heal);
    }

    public int getLifePercentage() {
        double percentage = ((double) getLife() / (double) this.getMaxLife());
        return (int) (percentage * 100);
    }

    public void showCell(int Cell, boolean team) {
        if (team) {
            this.team.sendToField(new ShowCellMessage(this.ID, Cell));
        } else {
            this.fight.sendToField(new ShowCellMessage(this.ID, Cell));
        }
    }

    public abstract int getLevel();

    public abstract short getMapCell();



    public boolean canBeginTurn() {
        if (this.isDead()) {
            return false; // Mort    
        }
        return true;
    }

    public int getMaxAP() {
        return this.stats.getTotal(StatsEnum.ActionPoints);
    }

    public int getMaxMP() {
        return this.stats.getTotal(StatsEnum.MovementPoints);
    }

    public int getAP() {
        return this.stats.getTotal(StatsEnum.ActionPoints, this instanceof CharacterFighter) - this.usedAP;

    }

    public int getMP() {
        return this.stats.getTotal(StatsEnum.MovementPoints, this instanceof CharacterFighter) - this.usedMP;

    }

    public boolean isAlive() {
        return this.getLife() > 0 && !this.left && !this.dead;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightFighterInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions);
    }

    /*public EntityDispositionInformations getEntityDispositionInformations(player character) {
     return new FightEntityDispositionInformations(character != null ? (this.isVisibleFor(character) ? this.getCellId() : -1) : this.getCellId(), this.direction, getCarriedActor());
     }*/
    public int getAPCancelling() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.Wisdom) / 4) + this.stats.getTotal(StatsEnum.Add_RETRAIT_PA);
    }

    public int getMPCancelling() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.Wisdom) / 4) + this.stats.getTotal(StatsEnum.Add_RETRAIT_PM);
    }

    public int getAPDodge() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.Wisdom) / 4) + this.stats.getTotal(StatsEnum.DodgePALostProbability);
    }

    public int getMPDodge() {
        return (int) Math.floor((double) this.stats.getTotal(StatsEnum.Wisdom) / 4) + this.stats.getTotal(StatsEnum.DodgePMLostProbability);
    }

    public int getTackledMP() { //Sould be implements in summoner,getMonster return 0
        if (this.visibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter Tackler : Pathfinder.GetEnnemyNear(fight, team, this.getCellId(), true)) {
            if (num1 == 0.0) {
                num1 = this.getTacklePercent(Tackler);
            } else {
                num1 *= this.getTacklePercent(Tackler);
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
        return (int) Math.ceil((double) this.getMP() * num2);
    }

    public Short[] getCastZone(SpellLevel spellLevel) {
        int num = spellLevel.getRange();
        for (BuffEffect Buff : (Iterable<BuffEffect>) this.buff.getAllBuffs().filter(buff -> buff instanceof BuffSpellDommage)::iterator) {
            num += Buff.CastInfos.Effect.value;
        }

        if (spellLevel.isRangeCanBeBoosted()) {
            int val1 = num + this.stats.getTotal(StatsEnum.Add_Range);
            if (val1 < spellLevel.getMinRange()) {
                val1 = spellLevel.getMinRange();
            }
            num = Math.min(val1, 280);
        }
        IZone shape;
        if (spellLevel.isCastInDiagonal() && spellLevel.isCastInLine()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num) {
                {
                    AllDirections = true;
                }
            };
        } else if (spellLevel.isCastInLine()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num);
        } else if (spellLevel.isCastInDiagonal()) {
            shape = new CrossZone((byte) spellLevel.getMinRange(), (byte) num) {
                {
                    Diagonal = true;
                }
            };
        } else {
            shape = new Lozenge((byte) spellLevel.getMinRange(), (byte) num, this.fight.getMap());
        }
        return shape.getCells(this.getCellId());
    }

    public boolean hasState(int stateId) {
        return this.states.hasState(FightStateEnum.valueOf(stateId));
    }

    public int getTackledAP() {
        if (this.visibleState != GameActionFightInvisibilityStateEnum.VISIBLE) {
            return 0;
        }
        double num1 = 0.0;
        for (Fighter Tackler : Pathfinder.GetEnnemyNear(fight, team, this.getCellId())) {
            {
                if (num1 == 0.0) {
                    num1 = this.getTacklePercent(Tackler);
                } else {
                    num1 *= this.getTacklePercent(Tackler);
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
        return (int) Math.ceil((double) this.getAP() * num2);
    }

    protected double getTacklePercent(Fighter tackler) {
        if (tackler.stats.getTotal(StatsEnum.Add_TackleBlock) == -2) {
            return 0.0;
        } else {
            return (double) (this.stats.getTotal(StatsEnum.Add_TackleEvade) + 2) / (2.0 * (double) (tackler.stats.getTotal(StatsEnum.Add_TackleBlock) + 2));
        }
    }

    @Override
    public EntityDispositionInformations getEntityDispositionInformations(Player character) {
        return new FightEntityDispositionInformations(character != null ? (this.isVisibleFor(character) ? this.getCellId() : -1) : this.getCellId(), this.direction, getCarriedActor());
    }

    public int getCarriedActor() {
        Optional<BuffEffect> Option = this.buff.getAllBuffs().filter(x -> x instanceof BuffPorter && x.duration != 0).findFirst();
        return Option.isPresent() ? Option.get().caster.getID() : 0;
    }

    public boolean isVisibleFor(Player character) {
        return this.getVisibleStateFor(character) != GameActionFightInvisibilityStateEnum.INVISIBLE.value;
    }

    public int shieldPoints() {
        return 0;
    }

    public int getSummonerID() {
        return this.summoner == null ? 0 : this.summoner.getID();
    }

    public byte getVisibleStateFor(Player character) {
        if (this.team.getAliveFighters().anyMatch(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.summoner.getID() == this.ID)) {
            return GameActionFightInvisibilityStateEnum.VISIBLE.value;
        } else if (character == null || character.getClient() == null || character.getFighter() == null || character.getFight() != this.fight) {
            return this.visibleState.value;
        } else {
            return !character.getFighter().isFriendlyWith(this) || this.visibleState == GameActionFightInvisibilityStateEnum.VISIBLE ? this.visibleState.value : GameActionFightInvisibilityStateEnum.DETECTED.value;
        }
    }

    public boolean isMyFriend(Player character) {
        if (character == null || character.getClient() == null || character.getFighter() == null || character.getFight() != this.fight) {
            return false;
        }
        return character.getFighter().isFriendlyWith(this);
    }

    public int getInitiative(boolean Base) { //FIXME : Considerate stats ?
        return (int) Math.floor((double) (this.getMaxLife() / 4 + this.stats.getTotal(StatsEnum.Initiative)) * (double) (this.getLife() / this.getMaxLife()));
    }

    public GameFightMinimalStats getGameFightMinimalStats(Player character) {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.getLife(), this.getMaxLife(), (int) this.stats.getBase(StatsEnum.Vitality), this.stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NeutralElementResistPercent), this.stats.getTotal(StatsEnum.EarthElementResistPercent), this.stats.getTotal(StatsEnum.WaterElementResistPercent), this.stats.getTotal(StatsEnum.AirElementResistPercent), this.stats.getTotal(StatsEnum.FireElementResistPercent), this.stats.getTotal(StatsEnum.NeutralElementReduction), this.stats.getTotal(StatsEnum.EarthElementReduction), this.stats.getTotal(StatsEnum.WaterElementReduction), this.stats.getTotal(StatsEnum.AirElementReduction), this.stats.getTotal(StatsEnum.FireElementReduction), this.stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.stats.getTotal(StatsEnum.DodgePALostProbability), this.stats.getTotal(StatsEnum.DodgePMLostProbability), this.stats.getTotal(StatsEnum.Add_TackleBlock), this.stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.visibleState.value : this.getVisibleStateFor(character), this.getInitiative(false));
        }
        return new GameFightMinimalStats(this.getLife(), this.getMaxLife(), (int) this.stats.getBase(StatsEnum.Vitality), this.stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NeutralElementResistPercent), this.stats.getTotal(StatsEnum.EarthElementResistPercent), this.stats.getTotal(StatsEnum.WaterElementResistPercent), this.stats.getTotal(StatsEnum.AirElementResistPercent), this.stats.getTotal(StatsEnum.FireElementResistPercent), this.stats.getTotal(StatsEnum.NeutralElementReduction), this.stats.getTotal(StatsEnum.EarthElementReduction), this.stats.getTotal(StatsEnum.WaterElementReduction), this.stats.getTotal(StatsEnum.AirElementReduction), this.stats.getTotal(StatsEnum.FireElementReduction), this.stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.stats.getTotal(StatsEnum.DodgePALostProbability), this.stats.getTotal(StatsEnum.DodgePMLostProbability), this.stats.getTotal(StatsEnum.Add_TackleBlock), this.stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.visibleState.value : this.getVisibleStateFor(character));
    }

    public IdentifiedEntityDispositionInformations GetIdentifiedEntityDispositionInformations() {
        return new IdentifiedEntityDispositionInformations(this.getCellId(), this.direction, this.ID);
    }

    public abstract FightTeamMemberInformations getFightTeamMemberInformations();

    @Override
    public abstract void send(Message Packet);

    public abstract void joinFight();

    @Override
    public abstract EntityLook getEntityLook();

    @Override
    public FightObjectType getObjectType() {
        return FightObjectType.OBJECT_FIGHTER;
    }

    @Override
    public short getCellId() {
        return this.myCell.Id;
    }

    @Override
    public boolean canWalk() {
        return false;
    }

    @Override
    public boolean canStack() {
        return false;
    }

    public boolean isFriendlyWith(Fighter actor) {
        return actor.team.id == this.team.id;
    }

    public boolean isEnnemyWith(Fighter actor) {
        return !this.isFriendlyWith(actor);
    }

    public void computeDamages(StatsEnum effect, MutableInt jet) {
        switch (effect) {
            case Damage_Earth:
            case Steal_Earth:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100 + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Earth_Damages_Bonus)));
                break;
            case Damage_Earth_Per_Pm_Percent:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100 + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Earth_Damages_Bonus)) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
            case Damage_Neutral:
            case Steal_Neutral:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Neutral_Damages_Bonus)));
                break;
            case Damage_Neutral_Per_Pm_Percent:
                jet.setValue(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Strength) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamagePhysic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Neutral_Damages_Bonus)) * (((double) (this.getMP() / this.getMaxMP())) * 100)));
                break;
            case Damage_Fire:
            case Steal_Fire:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus)));
                break;
            case Damage_Fire_Per_Pm_Percent:
                jet.setValue(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))) * ((((double) this.getMP() / (double) this.getMaxMP()))));
                System.out.println((this.getMP() / this.getMaxMP()));
                System.out.println(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))));
                System.out.println(Math.floor((jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Fire_Damages_Bonus))) * (((this.getMP() / this.getMaxMP()))));
                break;
            case Damage_Air:
            case Steal_Air:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Agility) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Air_Damages_Bonus)));
                break;
            case Damage_Air_Per_Pm_Percent:
                jet.setValue(((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Agility) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Air_Damages_Bonus))) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
            case Damage_Water:
            case Steal_Water:
                jet.setValue((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Chance) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Water_Damages_Bonus)));
                break;
            case Damage_Water_Per_Pm_Percent:
                jet.setValue(((int) Math.floor(jet.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Chance) + this.stats.getTotal(StatsEnum.AddDamagePercent) + this.stats.getTotal(StatsEnum.AddDamageMultiplicator)) / 100
                        + this.stats.getTotal(StatsEnum.AddDamageMagic) + this.stats.getTotal(StatsEnum.AllDamagesBonus) + this.stats.getTotal(StatsEnum.Add_Water_Damages_Bonus))) * (((double) (this.getMP() / this.getMaxMP())) * 100));
                break;
        }

    }

    public static final byte EFFECTSHAPE_DEFAULT_AREA_SIZE = 1;

    public static final byte EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE = 0;

    public static final byte EFFECTSHAPE_DEFAULT_EFFICIENCY = 10;

    public static final byte EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY = 4;

    public double getPortalsSpellEfficiencyBonus(short param1, Fight fight) {
        boolean _loc3_ = false;
        FightPortal[] _loc8_ = new FightPortal[0];
        int _loc9_ = 0;
        FightPortal _loc10_ = null;
        FightPortal _loc11_ = null;
        int _loc12_ = 0;
        double _loc13_ = 0;
        double _loc2_ = 1;

        for (CopyOnWriteArrayList<FightActivableObject> Objects : fight.getActivableObjects().values()) {
            for (FightActivableObject Object : Objects) {
                if (Object instanceof FightPortal && ((FightPortal) Object).Enabled) {
                    _loc8_ = ArrayUtils.add(_loc8_, (FightPortal) Object);
                    if (Object.getCellId() == param1) {
                        _loc3_ = true;
                    }
                }
            }
        }

        if (!_loc3_) {
            return _loc2_;
        }
        final int[] _loc6_ = LinkedCellsManager.getLinks(MapPoint.fromCellId(param1), Arrays.stream(_loc8_).map(x -> x.getMapPoint()).toArray(MapPoint[]::new));
        int _loc7_ = _loc6_.length;
        if (_loc7_ > 1) {
            while (_loc9_ < _loc7_) {
                _loc10_ = _loc8_[_loc9_];
                _loc12_ = Math.max(_loc12_, _loc10_.damageValue);
                if (_loc11_ != null) {
                    _loc13_ = _loc13_ + MapPoint.fromCellId(_loc10_.getCellId()).distanceToCell(MapPoint.fromCellId(_loc11_.getCellId()));
                }
                _loc11_ = _loc10_;
                _loc9_++;
            }
            _loc2_ = 1 + (_loc12_ + _loc8_.length * _loc13_) / 100;
        }
        return _loc2_;
    }

    public void calculBonusDamages(EffectInstanceDice effect, MutableInt jet, short castCell, short targetCell, short truedCell) {

        double bonus = this.stats.getTotal(StatsEnum.Add_Damage_Final_Percent);

        bonus += getShapeEfficiency(effect.zoneShape(), castCell, targetCell, effect.ZoneSize() != -100000 ? effect.ZoneSize() : EFFECTSHAPE_DEFAULT_AREA_SIZE, effect.zoneMinSize() != -100000 ? effect.zoneMinSize() : EFFECTSHAPE_DEFAULT_MIN_AREA_SIZE, effect.zoneEfficiencyPercent() != -100000 ? effect.zoneEfficiencyPercent() : EFFECTSHAPE_DEFAULT_EFFICIENCY, effect.zoneMaxEfficiency() != -100000 ? effect.zoneMaxEfficiency() : EFFECTSHAPE_DEFAULT_MAX_EFFICIENCY_APPLY);

        bonus *= getPortalsSpellEfficiencyBonus(truedCell, this.fight);

        jet.setValue((jet.floatValue() * bonus));
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

    public void calculReduceDamages(StatsEnum effect, MutableInt damages) {
        switch (effect) {
            case Damage_Neutral:
            case Steal_Neutral:
                damages.setValue(damages.intValue() * (100 - this.stats.getTotal(StatsEnum.NeutralElementResistPercent) - (fight instanceof AgressionFight ? stats.getTotal(StatsEnum.PvpNeutralElementResistPercent) : 0)) / 100
                        - this.stats.getTotal(StatsEnum.NeutralElementReduction) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpNeutralElementReduction) : 0) - this.stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Earth:
            case Steal_Earth:
                damages.setValue(damages.intValue() * (100 - this.stats.getTotal(StatsEnum.EarthElementResistPercent) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpEarthElementResistPercent) : 0)) / 100
                        - this.stats.getTotal(StatsEnum.EarthElementReduction) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpEarthElementReduction) : 0) - this.stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Fire:
            case Steal_Fire:
                damages.setValue(damages.intValue() * (100 - this.stats.getTotal(StatsEnum.FireElementResistPercent) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpFireElementResistPercent) : 0)) / 100
                        - this.stats.getTotal(StatsEnum.FireElementReduction) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpFireElementReduction) : 0) - this.stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Air:
            case Steal_Air:
                damages.setValue(damages.intValue() * (100 - this.stats.getTotal(StatsEnum.AirElementResistPercent) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpAirElementResistPercent) : 0)) / 100
                        - this.stats.getTotal(StatsEnum.AirElementReduction) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpAirElementReduction) : 0) - this.stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;

            case Damage_Water:
            case Steal_Water:
                damages.setValue(damages.intValue() * (100 - this.stats.getTotal(StatsEnum.WaterElementResistPercent) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpWaterElementResistPercent) : 0)) / 100
                        - this.stats.getTotal(StatsEnum.WaterElementReduction) - (fight instanceof AgressionFight ? this.stats.getTotal(StatsEnum.PvpWaterElementReduction) : 0) - this.stats.getTotal(StatsEnum.Add_Magic_Reduction));
                break;
        }
    }

    /// <summary>
    /// Calcul des PA/PM perdus
    /// </summary>
    /// <param name="caster"></param> TODO: Supprimer les appel variable inutile qui occupent de la mémoire et referencer direct les var
    /// <param name="LostPoint"></param>
    /// <param name="getMP"></param>
    /// <returns></returns>
    public int calculDodgeAPMP(Fighter caster, int lostPoint, boolean MP, boolean isBuff) {
        int realLostPoint = 0;

        if (!MP) {
            int dodgeAPCaster = caster.getAPCancelling() + 1;
            int dodgeAPTarget = this.getAPDodge() + 1;

            for (int i = 0; i < lostPoint; i++) {
                int ActualAP = (isBuff && this.ID == this.fight.getCurrentFighter().ID ? this.getMaxAP() : this.getAP()) - realLostPoint;
                int realAP = getAP();
                if (realAP == 0) {
                    realAP = 1;
                }
                double PercentLastAP = ActualAP / realAP;
                double Chance = 0.5 * (dodgeAPCaster / dodgeAPTarget) * PercentLastAP;
                int PercentChance = (int) (Chance * 100);

                if (PercentChance > 100) {
                    PercentChance = 90;
                }
                if (PercentChance < 10) {
                    PercentChance = 10;
                }

                if (RANDOM.nextInt(99) < PercentChance) {
                    realLostPoint++;
                }
            }
        } else {
            int dodgeMPCaster = caster.getMPCancelling() + 1;
            int dodgeMPTarget = this.getMPDodge() + 1;

            for (int i = 0; i < lostPoint; i++) {
                int ActualMP = (isBuff && this.ID == this.fight.getCurrentFighter().ID ? this.getMaxMP() : this.getMP()) - realLostPoint;
                int realMP = getMP();
                if (realMP == 0) {
                    realMP = 1;
                }
                double PercentLastMP = ActualMP / realMP;
                double Chance = 0.5 * (dodgeMPCaster / dodgeMPTarget) * PercentLastMP;
                int PercentChance = (int) (Chance * 100);

                if (PercentChance > 100) {
                    PercentChance = 90;
                }
                if (PercentChance < 10) {
                    PercentChance = 10;
                }

                if (RANDOM.nextInt(99) < PercentChance) {
                    realLostPoint++;
                }
            }
        }

        return realLostPoint;
    }

    public void calculheal(MutableInt heal) {
        heal.setValue(Math.floor(heal.doubleValue() * (100 + this.stats.getTotal(StatsEnum.Intelligence)) / 100 + this.stats.getTotal(StatsEnum.Add_Heal_Bonus)));
    }

    public int calculArmor(StatsEnum DamageEffect) {
        /*switch (DamageEffect) {
         default:
         return this.stats.getTotal(StatsEnum.AddArmor);
         }*/
        return this.stats.getTotal(StatsEnum.AddArmor);
    }

    public int getReflectedDamage() {
        return ((1 + (this.stats.getTotal(StatsEnum.Wisdom) / 100)) * this.stats.getTotal(StatsEnum.Damage_Return)) + this.stats.getTotal(StatsEnum.DamageReflection);
    }

    @Override
    public Integer getPriority() {
        return 0;
    }

    @Override
    public boolean canGoThrough() {
        return false;
    }

}
