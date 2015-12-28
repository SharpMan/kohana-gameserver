package koh.game.fights.fighters;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
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
import koh.protocol.types.game.context.roleplay.HumanOptionEmote;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public class CharacterFighter extends Fighter {

    public Player character;
    public EntityLook Look;

    public CharacterFighter(Fight Fight, WorldClient client) {
        super(Fight, null);
        this.turnReady = false;
        this.character = client.getCharacter();

        this.character.setFight(Fight);
        this.character.setFighter(this);
        this.character.stopRegen();
        this.character.removeHumanOption(HumanOptionEmote.class);
        //this.character.currentMap.unregisterPlayer(character);
        this.fight.registerPlayer(character);
        super.initFighter(this.character.getStats(), this.character.getID());
        super.setLife(this.character.getLife());
        super.setLifeMax(this.character.getMaxLife());
        if (super.getLife() == 0) {
            super.setLife(1);
        }
        this.entityLook = EntityLookParser.Copy(this.character.getEntityLook());
    }

    @Override
    public GameFightMinimalStats getGameFightMinimalStats(Player character) {
        if (this.fight.getFightState() == FightState.STATE_PLACE) {
            return new GameFightMinimalStatsPreparation(this.getLife(), this.getMaxLife(), this.character.getMaxLife(), this.stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NeutralElementResistPercent), this.stats.getTotal(StatsEnum.EarthElementResistPercent), this.stats.getTotal(StatsEnum.WaterElementResistPercent), this.stats.getTotal(StatsEnum.AirElementResistPercent), this.stats.getTotal(StatsEnum.FireElementResistPercent), this.stats.getTotal(StatsEnum.NeutralElementReduction), this.stats.getTotal(StatsEnum.EarthElementReduction), this.stats.getTotal(StatsEnum.WaterElementReduction), this.stats.getTotal(StatsEnum.AirElementReduction), this.stats.getTotal(StatsEnum.FireElementReduction), this.stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.stats.getTotal(StatsEnum.DodgePALostProbability), this.stats.getTotal(StatsEnum.DodgePMLostProbability), this.stats.getTotal(StatsEnum.Add_TackleBlock), this.stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.visibleState.value : this.getVisibleStateFor(character), this.getInitiative(false));
        }
        return new GameFightMinimalStats(this.getLife(), this.getMaxLife(), this.character.getMaxLife(), this.stats.getTotal(StatsEnum.PermanentDamagePercent), this.shieldPoints(), this.getAP(), this.getMaxAP(), this.getMP(), this.getMaxMP(), getSummonerID(), getSummonerID() != 0, this.stats.getTotal(StatsEnum.NeutralElementResistPercent), this.stats.getTotal(StatsEnum.EarthElementResistPercent), this.stats.getTotal(StatsEnum.WaterElementResistPercent), this.stats.getTotal(StatsEnum.AirElementResistPercent), this.stats.getTotal(StatsEnum.FireElementResistPercent), this.stats.getTotal(StatsEnum.NeutralElementReduction), this.stats.getTotal(StatsEnum.EarthElementReduction), this.stats.getTotal(StatsEnum.WaterElementReduction), this.stats.getTotal(StatsEnum.AirElementReduction), this.stats.getTotal(StatsEnum.FireElementReduction), this.stats.getTotal(StatsEnum.Add_Push_Damages_Reduction), this.stats.getTotal(StatsEnum.Add_Critical_Damages_Reduction), this.stats.getTotal(StatsEnum.DodgePALostProbability), this.stats.getTotal(StatsEnum.DodgePMLostProbability), this.stats.getTotal(StatsEnum.Add_TackleBlock), this.stats.getTotal(StatsEnum.Add_TackleEvade), character == null ? this.visibleState.value : this.getVisibleStateFor(character));
    }

    public int fakeContextualId = -1000;

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        return new GameFightCharacterInformations(((fakeContextualId != -1000 && !this.isMyFriend(character)) ? this.fakeContextualId : this.ID), this.getEntityLook(), this.getEntityDispositionInformations(character), this.team.id, this.wave, this.isAlive(), this.getGameFightMinimalStats(character), this.previousPositions, this.character.getNickName(), this.character.getPlayerStatus(), (byte) this.getLevel(), this.character.getActorAlignmentInformations(), this.character.getBreed(), this.character.hasSexe());
    }

    @Override
    public FightTeamMemberInformations getFightTeamMemberInformations() {
        return new FightTeamMemberCharacterInformations(this.ID, this.character.getNickName(), (byte) this.character.getLevel());
    }

    @Override
    public void JoinFight() {
        this.character.destroyFromMap();
    }

    @Override
    public void middleTurn() {
        /*if (this.character.client != null) {
         this.character.client.send(this.FighterStatsListMessagePacket());
         }*/
        super.middleTurn();
    }

    @Override
    public int endTurn() {
        if (this.character.getClient() == null) {
            this.fight.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 162, new String[]{this.character.getNickName(), Integer.toString(this.turnRunning)}));
            this.turnRunning--;
        }
        return super.endTurn();
    }

    public void CleanClone() {
        boolean updated = false;
        for (Fighter Clone : (Iterable<Fighter>) this.team.getAliveFighters().filter(Fighter -> (Fighter instanceof IllusionFighter) && Fighter.getSummoner() == this)::iterator) {
            Clone.tryDie(this.ID);
            updated = true;
        }
        if (updated) {
            this.onCloneCleared();
        }
    }

    public void onCloneCleared() {
        this.fight.observers.stream().filter(x -> !this.isMyFriend(((Player) x))).forEach(o -> ((Player) o).send(new GameActionFightVanishMessage(1029, this.ID, fakeContextualId)));
        this.fakeContextualId = -1000;
        this.buff.dispell(2763);

        this.send(this.FighterStatsListMessagePacket());
    }

    @Override
    public int beginTurn() {
        this.CleanClone();
        if (this.character.getClient() == null && this.turnRunning <= 0) {
            return this.tryDie(this.ID, true);
        }
        return super.beginTurn();
    }

    @Override
    public int tryDie(int casterId, boolean Force) {

        this.CleanClone();
        return super.tryDie(casterId, Force);
    }

    @Override
    public void leaveFight() {
        super.leaveFight();
        this.EndFight();
    }

    @Override
    public int getLevel() {
        return this.character.getLevel();
    }

    @Override
    public EntityLook getEntityLook() {
        return this.entityLook;
    }

    @Override
    public void EndFight() {
        if (fight.getFightType() != FightTypeEnum.FIGHT_TYPE_CHALLENGE) {
            if (super.getLife() <= 0) {
                this.character.setLife(1);
            } else {
                this.character.setLife(super.getLife());
            }
        }

        this.fight.unregisterPlayer(character);

        if (this.character.isInWorld()) {
            this.character.getClient().endGameAction(GameActionTypeEnum.FIGHT);
            this.character.send(new GameContextDestroyMessage());
            this.character.send(new GameContextCreateMessage((byte) 1));
            //this.character.send(new LifePointsRegenBeginMessage());
            this.character.refreshStats(false,true);
            if (!(this.fight instanceof ChallengeFight) && this.team.id == this.fight.getLoosers().id) {
                this.character.teleport(this.character.getSavedMap(), this.character.getSavedCell());
            } else {
                this.character.send(new CurrentMapMessage(this.character.getCurrentMap().getId(), "649ae451ca33ec53bbcbcc33becf15f4"));
                //this.character.send(new BasicTimeMessage((double) (new Date().getTime()), 0));
                this.character.getCurrentMap().spawnActor(this.character);

            }
        }//elseSetstats mapid...
        this.character.setFight(null);
        this.character.setFighter(null);
    }

    @Override
    public short getMapCell() {
        return this.character.getCell().getId();
    }

    @Override
    public void send(Message Packet) {
        this.character.send(Packet);
    }

    public FighterStatsListMessage FighterStatsListMessagePacket() {
        return new FighterStatsListMessage(new CharacterCharacteristicsInformations((double) character.getExperience(), (double) DAO.getExps().getPlayerMinExp(character.getLevel()), (double) DAO.getExps().getPlayerMaxExp(character.getLevel()), character.getKamas(), character.getStatPoints(), 0, character.getSpellPoints(), character.getActorAlignmentExtendInformations(),
                getLife(), getMaxLife(), character.getEnergy(), PlayerEnum.MaxEnergy,
                (short) this.getAP(), (short) this.getMP(),
                new CharacterBaseCharacteristic(this.getInitiative(true), 0, stats.getItem(StatsEnum.Initiative), 0, 0), stats.getEffect(StatsEnum.Prospecting), stats.getEffect(StatsEnum.ActionPoints),
                stats.getEffect(StatsEnum.MovementPoints), stats.getEffect(StatsEnum.Strength), stats.getEffect(StatsEnum.Vitality),
                stats.getEffect(StatsEnum.Wisdom), stats.getEffect(StatsEnum.Chance), stats.getEffect(StatsEnum.Agility),
                stats.getEffect(StatsEnum.Intelligence), stats.getEffect(StatsEnum.Add_Range), stats.getEffect(StatsEnum.AddSummonLimit),
                stats.getEffect(StatsEnum.DamageReflection), stats.getEffect(StatsEnum.Add_CriticalHit), character.getInventoryCache().weaponCriticalHit(),
                stats.getEffect(StatsEnum.CriticalMiss), stats.getEffect(StatsEnum.Add_Heal_Bonus), stats.getEffect(StatsEnum.AllDamagesBonus),
                stats.getEffect(StatsEnum.WeaponDamagesBonusPercent), stats.getEffect(StatsEnum.AddDamagePercent), stats.getEffect(StatsEnum.TrapBonus),
                stats.getEffect(StatsEnum.Trap_Damage_Percent), stats.getEffect(StatsEnum.GlyphBonusPercent), stats.getEffect(StatsEnum.PermanentDamagePercent), stats.getEffect(StatsEnum.Add_TackleBlock),
                stats.getEffect(StatsEnum.Add_TackleEvade), stats.getEffect(StatsEnum.Add_RETRAIT_PA), stats.getEffect(StatsEnum.Add_RETRAIT_PM), stats.getEffect(StatsEnum.Add_Push_Damages_Bonus),
                stats.getEffect(StatsEnum.Add_Critical_Damages), stats.getEffect(StatsEnum.Add_Neutral_Damages_Bonus), stats.getEffect(StatsEnum.Add_Earth_Damages_Bonus),
                stats.getEffect(StatsEnum.Add_Water_Damages_Bonus), stats.getEffect(StatsEnum.Add_Air_Damages_Bonus), stats.getEffect(StatsEnum.Add_Fire_Damages_Bonus),
                stats.getEffect(StatsEnum.DodgePALostProbability), stats.getEffect(StatsEnum.DodgePMLostProbability), stats.getEffect(StatsEnum.NeutralElementResistPercent),
                stats.getEffect(StatsEnum.EarthElementResistPercent), stats.getEffect(StatsEnum.WaterElementResistPercent), stats.getEffect(StatsEnum.AirElementResistPercent),
                stats.getEffect(StatsEnum.FireElementResistPercent), stats.getEffect(StatsEnum.NeutralElementReduction), stats.getEffect(StatsEnum.EarthElementReduction),
                stats.getEffect(StatsEnum.WaterElementReduction), stats.getEffect(StatsEnum.AirElementReduction), stats.getEffect(StatsEnum.FireElementReduction),
                stats.getEffect(StatsEnum.Add_Push_Damages_Reduction), stats.getEffect(StatsEnum.Add_Critical_Damages_Reduction), stats.getEffect(StatsEnum.PvpNeutralElementResistPercent),
                stats.getEffect(StatsEnum.PvpEarthElementResistPercent), stats.getEffect(StatsEnum.PvpWaterElementResistPercent), stats.getEffect(StatsEnum.PvpAirElementResistPercent),
                stats.getEffect(StatsEnum.PvpFireElementResistPercent), stats.getEffect(StatsEnum.PvpNeutralElementReduction), stats.getEffect(StatsEnum.PvpEarthElementReduction),
                stats.getEffect(StatsEnum.PvpWaterElementReduction), stats.getEffect(StatsEnum.PvpAirElementReduction), stats.getEffect(StatsEnum.PvpFireElementReduction),
                new CharacterSpellModification[0], (short) 0));
    }

    @Override
    public int compareTo(IFightObject obj) {
        return getPriority().compareTo(obj.getPriority());
    }

    @Override
    public int getInitiative(boolean Base) {
        return this.character.getInitiative(Base);
    }

}
