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
        this.Character = Client.Character;

        this.Character.SetFight(Fight);
        this.Character.SetFighter(this);
        this.Character.StopRegen();
        //this.Character.CurrentMap.unregisterPlayer(Character);
        this.Fight.registerPlayer(Character);
        super.InitFighter(this.Character.Stats, this.Character.ID);
        super.setLife(this.Character.Life);
        super.setLifeMax(this.Character.MaxLife());
        if (super.Life() == 0) {
            super.setLife(1);
        }
        this.entityLook = EntityLookParser.Copy(this.Character.GetEntityLook());
    }

    @Override
    public GameFightMinimalStats GetGameFightMinimalStats(Player character) {
        if (this.Fight.FightState == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.Life(), this.MaxLife(), this.Character.MaxLife(), this.Stats.GetTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.GetTotal(StatsEnum.NeutralElementResistPercent), this.Stats.GetTotal(StatsEnum.EarthElementResistPercent), this.Stats.GetTotal(StatsEnum.WaterElementResistPercent), this.Stats.GetTotal(StatsEnum.AirElementResistPercent), this.Stats.GetTotal(StatsEnum.FireElementResistPercent), this.Stats.GetTotal(StatsEnum.NeutralElementReduction), this.Stats.GetTotal(StatsEnum.EarthElementReduction), this.Stats.GetTotal(StatsEnum.WaterElementReduction), this.Stats.GetTotal(StatsEnum.AirElementReduction), this.Stats.GetTotal(StatsEnum.FireElementReduction), this.Stats.GetTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.GetTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.GetTotal(StatsEnum.DodgePALostProbability), this.Stats.GetTotal(StatsEnum.DodgePMLostProbability), this.Stats.GetTotal(StatsEnum.Add_TackleBlock), this.Stats.GetTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character), this.Initiative(false));
        }
        return new GameFightMinimalStats(this.Life(), this.MaxLife(), this.Character.MaxLife(), this.Stats.GetTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.AP(), this.MaxAP(), this.MP(), this.MaxMP(), Summoner(), Summoner() != 0, this.Stats.GetTotal(StatsEnum.NeutralElementResistPercent), this.Stats.GetTotal(StatsEnum.EarthElementResistPercent), this.Stats.GetTotal(StatsEnum.WaterElementResistPercent), this.Stats.GetTotal(StatsEnum.AirElementResistPercent), this.Stats.GetTotal(StatsEnum.FireElementResistPercent), this.Stats.GetTotal(StatsEnum.NeutralElementReduction), this.Stats.GetTotal(StatsEnum.EarthElementReduction), this.Stats.GetTotal(StatsEnum.WaterElementReduction), this.Stats.GetTotal(StatsEnum.AirElementReduction), this.Stats.GetTotal(StatsEnum.FireElementReduction), this.Stats.GetTotal(StatsEnum.Add_Push_Damages_Reduction), this.Stats.GetTotal(StatsEnum.Add_Critical_Damages_Reduction), this.Stats.GetTotal(StatsEnum.DodgePALostProbability), this.Stats.GetTotal(StatsEnum.DodgePMLostProbability), this.Stats.GetTotal(StatsEnum.Add_TackleBlock), this.Stats.GetTotal(StatsEnum.Add_TackleEvade), character == null ? this.VisibleState.value : this.GetVisibleStateFor(character));
    }

    public int fakeContextualId = -1000;

    @Override
    public GameContextActorInformations GetGameContextActorInformations(Player character) {
        return new GameFightCharacterInformations(((fakeContextualId != -1000 && !this.IsMyFriend(character)) ? this.fakeContextualId : this.ID), this.GetEntityLook(), this.GetEntityDispositionInformations(character), this.Team.Id, this.wave, this.IsAlive(), this.GetGameFightMinimalStats(character), this.previousPositions, this.Character.NickName, this.Character.PlayerStatus(), (byte) this.Level(), this.Character.GetActorAlignmentInformations(), this.Character.Breed, this.Character.Sexe());
    }

    @Override
    public FightTeamMemberInformations GetFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, this.Character.NickName, (byte) this.Character.Level);
    }

    @Override
    public void JoinFight() {
        this.Character.DestroyFromMap();
    }

    @Override
    public void MiddleTurn() {
        /*if (this.Character.Client != null) {
         this.Character.Client.Send(this.FighterStatsListMessagePacket());
         }*/
        super.MiddleTurn();
    }

    @Override
    public int EndTurn() {
        if (this.Character.Client == null) {
            this.Fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 162, new String[]{this.Character.NickName, Integer.toString(this.TurnRunning)}));
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
        this.Fight.observers.stream().filter(x -> !this.IsMyFriend(((Player) x))).forEach(o -> ((Player) o).Send(new GameActionFightVanishMessage(1029, this.ID, fakeContextualId)));
        this.fakeContextualId = -1000;
        this.Buffs.Dispell(2763);

        this.Send(this.FighterStatsListMessagePacket());
    }

    @Override
    public int BeginTurn() {
        this.CleanClone();
        if (this.Character.Client == null && this.TurnRunning <= 0) {
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
        return this.Character.Level;
    }

    @Override
    public EntityLook GetEntityLook() {
        return this.entityLook;
    }

    @Override
    public void EndFight() {
        if (Fight.FightType != FightTypeEnum.FIGHT_TYPE_CHALLENGE) {
            if (super.Life() <= 0) {
                this.Character.Life = 1;
            } else {
                this.Character.Life = super.Life();
            }
        }

        this.Fight.unregisterPlayer(Character);

        if (this.Character.IsInWorld) {
            this.Character.Client.EndGameAction(GameActionTypeEnum.FIGHT);
            this.Character.Send(new GameContextDestroyMessage());
            this.Character.Send(new GameContextCreateMessage((byte) 1));
            //this.Character.Send(new LifePointsRegenBeginMessage());
            this.Character.RefreshStats(false);
            if (!(this.Fight instanceof ChallengeFight) && this.Team.Id == this.Fight.GetLoosers().Id) {
                this.Character.teleport(this.Character.SavedMap, this.Character.SavedCell);
            } else {
                this.Character.Send(new CurrentMapMessage(this.Character.CurrentMap.Id, "649ae451ca33ec53bbcbcc33becf15f4"));
                //this.Character.Send(new BasicTimeMessage((double) (new Date().getTime()), 0));
                this.Character.CurrentMap.SpawnActor(this.Character);

            }
        }//elseSetstats mapid...
        this.Character.SetFight(null);
        this.Character.SetFighter(null);
    }

    @Override
    public short MapCell() {
        return this.Character.Cell.Id;
    }

    @Override
    public void Send(Message Packet) {
        this.Character.Send(Packet);
    }

    public FighterStatsListMessage FighterStatsListMessagePacket() {
        return new FighterStatsListMessage(new CharacterCharacteristicsInformations((double) Character.Experience, (double) ExpDAOImpl.persoXpMin(Character.Level), (double) ExpDAOImpl.persoXpMax(Character.Level), Character.Kamas, Character.StatPoints, 0, Character.SpellPoints, Character.GetActorAlignmentExtendInformations(),
                Life(), MaxLife(), Character.Energy, PlayerEnum.MaxEnergy,
                (short) this.AP(), (short) this.MP(),
                new CharacterBaseCharacteristic(this.Initiative(true), 0, Stats.GetItem(StatsEnum.Initiative), 0, 0), Stats.GetEffect(StatsEnum.Prospecting), Stats.GetEffect(StatsEnum.ActionPoints),
                Stats.GetEffect(StatsEnum.MovementPoints), Stats.GetEffect(StatsEnum.Strength), Stats.GetEffect(StatsEnum.Vitality),
                Stats.GetEffect(StatsEnum.Wisdom), Stats.GetEffect(StatsEnum.Chance), Stats.GetEffect(StatsEnum.Agility),
                Stats.GetEffect(StatsEnum.Intelligence), Stats.GetEffect(StatsEnum.Add_Range), Stats.GetEffect(StatsEnum.AddSummonLimit),
                Stats.GetEffect(StatsEnum.DamageReflection), Stats.GetEffect(StatsEnum.Add_CriticalHit), Character.InventoryCache.WeaponCriticalHit(),
                Stats.GetEffect(StatsEnum.CriticalMiss), Stats.GetEffect(StatsEnum.Add_Heal_Bonus), Stats.GetEffect(StatsEnum.AllDamagesBonus),
                Stats.GetEffect(StatsEnum.WeaponDamagesBonusPercent), Stats.GetEffect(StatsEnum.AddDamagePercent), Stats.GetEffect(StatsEnum.TrapBonus),
                Stats.GetEffect(StatsEnum.Trap_Damage_Percent), Stats.GetEffect(StatsEnum.GlyphBonusPercent), Stats.GetEffect(StatsEnum.PermanentDamagePercent), Stats.GetEffect(StatsEnum.Add_TackleBlock),
                Stats.GetEffect(StatsEnum.Add_TackleEvade), Stats.GetEffect(StatsEnum.Add_RETRAIT_PA), Stats.GetEffect(StatsEnum.Add_RETRAIT_PM), Stats.GetEffect(StatsEnum.Add_Push_Damages_Bonus),
                Stats.GetEffect(StatsEnum.Add_Critical_Damages), Stats.GetEffect(StatsEnum.Add_Neutral_Damages_Bonus), Stats.GetEffect(StatsEnum.Add_Earth_Damages_Bonus),
                Stats.GetEffect(StatsEnum.Add_Water_Damages_Bonus), Stats.GetEffect(StatsEnum.Add_Air_Damages_Bonus), Stats.GetEffect(StatsEnum.Add_Fire_Damages_Bonus),
                Stats.GetEffect(StatsEnum.DodgePALostProbability), Stats.GetEffect(StatsEnum.DodgePMLostProbability), Stats.GetEffect(StatsEnum.NeutralElementResistPercent),
                Stats.GetEffect(StatsEnum.EarthElementResistPercent), Stats.GetEffect(StatsEnum.WaterElementResistPercent), Stats.GetEffect(StatsEnum.AirElementResistPercent),
                Stats.GetEffect(StatsEnum.FireElementResistPercent), Stats.GetEffect(StatsEnum.NeutralElementReduction), Stats.GetEffect(StatsEnum.EarthElementReduction),
                Stats.GetEffect(StatsEnum.WaterElementReduction), Stats.GetEffect(StatsEnum.AirElementReduction), Stats.GetEffect(StatsEnum.FireElementReduction),
                Stats.GetEffect(StatsEnum.Add_Push_Damages_Reduction), Stats.GetEffect(StatsEnum.Add_Critical_Damages_Reduction), Stats.GetEffect(StatsEnum.PvpNeutralElementResistPercent),
                Stats.GetEffect(StatsEnum.PvpEarthElementResistPercent), Stats.GetEffect(StatsEnum.PvpWaterElementResistPercent), Stats.GetEffect(StatsEnum.PvpAirElementResistPercent),
                Stats.GetEffect(StatsEnum.PvpFireElementResistPercent), Stats.GetEffect(StatsEnum.PvpNeutralElementReduction), Stats.GetEffect(StatsEnum.PvpEarthElementReduction),
                Stats.GetEffect(StatsEnum.PvpWaterElementReduction), Stats.GetEffect(StatsEnum.PvpAirElementReduction), Stats.GetEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0));
    }

    @Override
    public int compareTo(IFightObject obj) {
        return Priority().compareTo(obj.Priority());
    }

    @Override
    public int Initiative(boolean Base) {
        return this.Character.Initiative(Base);
    }

}
