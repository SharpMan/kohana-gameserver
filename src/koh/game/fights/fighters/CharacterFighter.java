package koh.game.fights.fighters;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.mysql.ExpDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.fights.Fight;
import koh.game.fights.FightState;
import koh.game.fights.FightTypeEnum;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.types.ChallengeFight;
import koh.game.network.WorldClient;
import koh.look.EntityLookParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightVanishMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.stats.FighterStatsListMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.roleplay.CurrentMapMessage;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import koh.protocol.types.game.character.characteristic.CharacterCharacteristicsInformations;
import koh.protocol.types.game.character.characteristic.CharacterSpellModification;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberCharacterInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;
import koh.protocol.types.game.context.fight.GameFightCharacterInformations;
import koh.protocol.types.game.context.fight.GameFightMinimalStats;
import koh.protocol.types.game.context.fight.GameFightMinimalStatsPreparation;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public class CharacterFighter extends Fighter {

    public Player Character;
    public EntityLook Look;

    public CharacterFighter(Fight Fight, WorldClient Client) {
        super(Fight, null);
        this.TurnReady = false;
        this.Character = Client.character;

        this.Character.setFight(Fight);
        this.Character.setFighter(this);
        this.Character.stopRegen();
        //this.character.currentMap.unregisterPlayer(character);
        this.Fight.registerPlayer(Character);
        super.InitFighter(this.Character.stats, this.Character.ID);
        super.setLife(this.Character.life);
        super.setLifeMax(this.Character.getMaxLife());
        if (super.Life() == 0) {
            super.setLife(1);
        }
        this.entityLook = EntityLookParser.Copy(this.Character.getEntityLook());
    }

    @Override
    public GameFightMinimalStats GetGameFightMinimalStats(Player character) {
        if (this.Fight.FightState == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.Life(), this.MaxLife(), this.Character.getMaxLife(), this.Stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.getTotal(StatsEnum.NeutralElementResistPercent), this.Stats.getTotal(StatsEnum.EarthElementResistPercent), this.Stats.getTotal(StatsEnum.WaterElementResistPercent), this.Stats.getTotal(StatsEnum.AirElementResistPercent), this.Stats.getTotal(StatsEnum.FireElementResistPercent), this.Stats.getTotal(StatsEnum.NeutralElementReduction), this.Stats.getTotal(StatsEnum.EarthElementReduction), this.Stats.getTotal(StatsEnum.WaterElementReduction), this.Stats.getTotal(StatsEnum.AirElementReduction), this.Stats.getTotal(StatsEnum.FireElementReduction), this.Stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.getTotal(StatsEnum.DodgePALostProbability), this.Stats.getTotal(StatsEnum.DodgePMLostProbability), this.Stats.getTotal(StatsEnum.Add_TackleBlock), this.Stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character), this.Initiative(false));
        }
        return new GameFightMinimalStats(this.Life(), this.MaxLife(), this.Character.getMaxLife(), this.Stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.getTotal(StatsEnum.NeutralElementResistPercent), this.Stats.getTotal(StatsEnum.EarthElementResistPercent), this.Stats.getTotal(StatsEnum.WaterElementResistPercent), this.Stats.getTotal(StatsEnum.AirElementResistPercent), this.Stats.getTotal(StatsEnum.FireElementResistPercent), this.Stats.getTotal(StatsEnum.NeutralElementReduction), this.Stats.getTotal(StatsEnum.EarthElementReduction), this.Stats.getTotal(StatsEnum.WaterElementReduction), this.Stats.getTotal(StatsEnum.AirElementReduction), this.Stats.getTotal(StatsEnum.FireElementReduction), this.Stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.getTotal(StatsEnum.DodgePALostProbability), this.Stats.getTotal(StatsEnum.DodgePMLostProbability), this.Stats.getTotal(StatsEnum.Add_TackleBlock), this.Stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character));
    }

    public int fakeContextualId = -1000;

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightCharacterInformations(((fakeContextualId != -1000 && !this.IsMyFriend(character)) ? this.fakeContextualId : this.ID), this.getEntityLook(), this.getEntityDispositionInformations(character), this.Team.Id, this.wave, this.IsAlive(), this.GetGameFightMinimalStats(character), this.previousPositions, this.Character.nickName, this.Character.getPlayerStatus(), (byte) this.Level(), this.Character.getActorAlignmentInformations(), this.Character.breed, this.Character.hasSexe());
    }

    @Override
    public FightTeamMemberInformations GetFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, this.Character.nickName, (byte) this.Character.level);
    }

    @Override
    public void JoinFight() {
        this.Character.destroyFromMap();
    }

    @Override
    public void MiddleTurn() {
        /*if (this.character.client != null) {
         this.character.client.send(this.FighterStatsListMessagePacket());
         }*/
        super.MiddleTurn();
    }

    @Override
    public int EndTurn() {
        if (this.Character.client == null) {
            this.Fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 162, new String[]{this.Character.nickName, Integer.toString(this.TurnRunning)}));
            this.TurnRunning--;
        }
        return super.EndTurn();
    }

    public void CleanClone() {
        boolean updated = false;
        for (Fighter Clone : (Iterable<Fighter>) this.Team.GetAliveFighters().filter(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.Summoner == this)::iterator) {
            Clone.TryDie(this.ID);
            updated = true;
        }
        if (updated) {
            this.onCloneCleared();
        }
    }

    public void onCloneCleared() {
        this.Fight.observers.stream().filter(x -> !this.IsMyFriend(((Player) x))).forEach(o -> ((Player) o).send(new GameActionFightVanishMessage(1029, this.ID, fakeContextualId)));
        this.fakeContextualId = -1000;
        this.Buffs.Dispell(2763);

        this.send(this.FighterStatsListMessagePacket());
    }

    @Override
    public int BeginTurn() {
        this.CleanClone();
        if (this.Character.client == null && this.TurnRunning <= 0) {
            return this.TryDie(this.ID, true);
        }
        return super.BeginTurn();
    }

    @Override
    public int TryDie(int Caster, boolean Force) {

        this.CleanClone();
        return super.TryDie(Caster, Force);
    }

    @Override
    public void LeaveFight() {
        super.LeaveFight();
        this.EndFight();
    }

    @Override
    public int Level() {
        return this.Character.level;
    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

    @Override
    public void EndFight() {
        if (Fight.FightType != FightTypeEnum.FIGHT_TYPE_CHALLENGE) {
            if (super.Life() <= 0) {
                this.Character.life = 1;
            } else {
                this.Character.life = super.Life();
            }
        }

        this.Fight.unregisterPlayer(Character);

        if (this.Character.isInWorld) {
            this.Character.client.endGameAction(GameActionTypeEnum.FIGHT);
            this.Character.send(new GameContextDestroyMessage());
            this.Character.send(new GameContextCreateMessage((byte) 1));
            //this.character.send(new LifePointsRegenBeginMessage());
            this.Character.refreshStats(false);
            if (!(this.Fight instanceof ChallengeFight) && this.Team.Id == this.Fight.GetLoosers().Id) {
                this.Character.teleport(this.Character.savedMap, this.Character.savedCell);
            } else {
                this.Character.send(new CurrentMapMessage(this.Character.currentMap.id, "649ae451ca33ec53bbcbcc33becf15f4"));
                //this.character.send(new BasicTimeMessage((double) (new Date().getTime()), 0));
                this.Character.currentMap.spawnActor(this.Character);

            }
        }//elseSetstats mapid...
        this.Character.setFight(null);
        this.Character.setFighter(null);
    }

    @Override
    public short MapCell() {
        return this.Character.cell.id;
    }

    @Override
    public void send(Message Packet) {
        this.Character.send(Packet);
    }

    public FighterStatsListMessage FighterStatsListMessagePacket() {
        return new FighterStatsListMessage(new CharacterCharacteristicsInformations((double) Character.experience, (double) ExpDAOImpl.persoXpMin(Character.level), (double) ExpDAOImpl.persoXpMax(Character.level), Character.kamas, Character.statPoints, 0, Character.spellPoints, Character.getActorAlignmentExtendInformations(),
                Life(), MaxLife(), Character.energy, PlayerEnum.MaxEnergy,
                (short) this.AP(), (short) this.MP(),
                new CharacterBaseCharacteristic(this.Initiative(true), 0, Stats.getItem(StatsEnum.Initiative), 0, 0), Stats.getEffect(StatsEnum.Prospecting), Stats.getEffect(StatsEnum.ActionPoints),
                Stats.getEffect(StatsEnum.MovementPoints), Stats.getEffect(StatsEnum.Strength), Stats.getEffect(StatsEnum.Vitality),
                Stats.getEffect(StatsEnum.Wisdom), Stats.getEffect(StatsEnum.Chance), Stats.getEffect(StatsEnum.Agility),
                Stats.getEffect(StatsEnum.Intelligence), Stats.getEffect(StatsEnum.Add_Range), Stats.getEffect(StatsEnum.AddSummonLimit),
                Stats.getEffect(StatsEnum.DamageReflection), Stats.getEffect(StatsEnum.Add_CriticalHit), Character.inventoryCache.weaponCriticalHit(),
                Stats.getEffect(StatsEnum.CriticalMiss), Stats.getEffect(StatsEnum.Add_Heal_Bonus), Stats.getEffect(StatsEnum.AllDamagesBonus),
                Stats.getEffect(StatsEnum.WeaponDamagesBonusPercent), Stats.getEffect(StatsEnum.AddDamagePercent), Stats.getEffect(StatsEnum.TrapBonus),
                Stats.getEffect(StatsEnum.Trap_Damage_Percent), Stats.getEffect(StatsEnum.GlyphBonusPercent), Stats.getEffect(StatsEnum.PermanentDamagePercent), Stats.getEffect(StatsEnum.Add_TackleBlock),
                Stats.getEffect(StatsEnum.Add_TackleEvade), Stats.getEffect(StatsEnum.Add_RETRAIT_PA), Stats.getEffect(StatsEnum.Add_RETRAIT_PM), Stats.getEffect(StatsEnum.Add_Push_Damages_Bonus),
                Stats.getEffect(StatsEnum.Add_Critical_Damages), Stats.getEffect(StatsEnum.Add_Neutral_Damages_Bonus), Stats.getEffect(StatsEnum.Add_Earth_Damages_Bonus),
                Stats.getEffect(StatsEnum.Add_Water_Damages_Bonus), Stats.getEffect(StatsEnum.Add_Air_Damages_Bonus), Stats.getEffect(StatsEnum.Add_Fire_Damages_Bonus),
                Stats.getEffect(StatsEnum.DodgePALostProbability), Stats.getEffect(StatsEnum.DodgePMLostProbability), Stats.getEffect(StatsEnum.NeutralElementResistPercent),
                Stats.getEffect(StatsEnum.EarthElementResistPercent), Stats.getEffect(StatsEnum.WaterElementResistPercent), Stats.getEffect(StatsEnum.AirElementResistPercent),
                Stats.getEffect(StatsEnum.FireElementResistPercent), Stats.getEffect(StatsEnum.NeutralElementReduction), Stats.getEffect(StatsEnum.EarthElementReduction),
                Stats.getEffect(StatsEnum.WaterElementReduction), Stats.getEffect(StatsEnum.AirElementReduction), Stats.getEffect(StatsEnum.FireElementReduction),
                Stats.getEffect(StatsEnum.Add_Push_Damages_Reduction), Stats.getEffect(StatsEnum.Add_Critical_Damages_Reduction), Stats.getEffect(StatsEnum.PvpNeutralElementResistPercent),
                Stats.getEffect(StatsEnum.PvpEarthElementResistPercent), Stats.getEffect(StatsEnum.PvpWaterElementResistPercent), Stats.getEffect(StatsEnum.PvpAirElementResistPercent),
                Stats.getEffect(StatsEnum.PvpFireElementResistPercent), Stats.getEffect(StatsEnum.PvpNeutralElementReduction), Stats.getEffect(StatsEnum.PvpEarthElementReduction),
                Stats.getEffect(StatsEnum.PvpWaterElementReduction), Stats.getEffect(StatsEnum.PvpAirElementReduction), Stats.getEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0));
    }

    @Override
    public int compareTo(IFightObject obj) {
        return Priority().compareTo(obj.Priority());
    }

    @Override
    public int Initiative(boolean Base) {
        return this.Character.getInitiative(Base);
    }

}
