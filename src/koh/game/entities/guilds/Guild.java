package koh.game.entities.guilds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import koh.game.Main;
import koh.game.dao.mysql.ExpDAO;
import koh.game.dao.sqlite.GuildDAO;
import koh.game.dao.mysql.PlayerDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.network.ChatChannel;
import koh.game.utils.Settings;
import koh.protocol.client.enums.GuildRightsBitEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.guild.*;
import koh.protocol.messages.game.guild.tax.TaxCollectorListMessage;
import koh.protocol.types.game.context.roleplay.BasicGuildInformations;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.guild.GuildEmblem;
import koh.protocol.types.game.guild.tax.TaxCollectorFightersInformation;
import koh.protocol.types.game.guild.tax.TaxCollectorInformations;
import koh.protocol.types.game.house.HouseInformationsForGuild;
import koh.protocol.types.game.paddock.PaddockContentInformations;
import koh.utils.Enumerable;

/**
 *
 * @author Neo-Craft
 */
public class Guild extends IWorldEventObserver {

    public volatile ChatChannel ChatChannel = new ChatChannel();
    public Map<Integer, Player> Characters;
    public Map<Integer, GuildMember> Members;
    public GuildEntity Entity;
    private GuildMember BossCache;
    private GuildEmblem EmblemeCache;
    public byte[] SpellLevel;

    public Guild(GuildEntity Entity) {
        this.Entity = Entity;
        this.Members = Collections.synchronizedMap(new HashMap<>());
        this.SpellLevel = Enumerable.StringToByteArray(Entity.Spells);
    }

    public void onFighterAddedExperience(GuildMember Member, long XP) {
        Member.AddExperience(XP);
        this.Entity.AddExperience(XP);
        while (this.Entity.Experience() >= ExpDAO.GetFloorByLevel(this.Entity.Level + 1).Guild && this.Entity.Level < Settings.GetIntElement("Max.GuildLevel")) {
            this.Entity.Level++;
            this.Entity.Boost += 5;
            this.sendToField(new GuildLevelUpMessage(this.Entity.Level));
        }
    }

    public GuildMember GetBoss() {
        if (this.BossCache == null) {
            for (GuildMember GM : this.Members.values()) {
                if (GM.isBoss()) {
                    if (this.BossCache != null) {
                        Main.Logs().writeError(String.format("There is at least two boss in guild {0} ({1}) BossSecond {3}", this.Entity.GuildID, this.Entity.Name, GM.Name));
                    }
                    this.BossCache = GM;
                }
            }
            if (this.BossCache == null) {
                if (this.Members.isEmpty()) {
                    throw new Error(String.format("Guild {0} ({1}) is empty", this.Entity.GuildID, this.Entity.Name));
                }

            }
        }
        return this.BossCache;
    }

    public void SetBoss(GuildMember GuildMember) {
        if (this.BossCache != GuildMember) {
            if (this.BossCache != null) {
                this.BossCache.Rank = 0;
                this.BossCache.Rights = GuildRightsBitEnum.GUILD_RIGHT_NONE;
                this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 199, new String[]{GuildMember.Name, this.BossCache.Name, this.Entity.Name}));
                this.UpdateMember(this.BossCache);
            }
            this.BossCache = GuildMember;
            this.BossCache.Rank = 1;
            this.BossCache.Rights = GuildRightsBitEnum.GUILD_RIGHT_BOSS;
            this.UpdateMember(GuildMember);
        }
    }

    public void addMember(GuildMember member, Player Client) {
        this.Members.put(member.CharacterID, member);
        Client.Guild = this;
        Client.RefreshActor();
        Client.Send(new GuildJoinedMessage(this.toGuildInformations(), member.Rights, true));
    }

    public synchronized boolean ChangeParameters(Player modifier, GuildMember member, int rank, byte xpPercent, int rights) {
        boolean result;

        if (modifier.Guild.Entity.GuildID != member.GuildID) {
            result = false;
        } else {
            if (modifier.GuildMember() != member && modifier.GuildMember().isBoss() && rank == 1) {
                this.SetBoss(member);
            } else {
                if (modifier.GuildMember() == member || !member.isBoss()) {
                    if (modifier.GuildMember().manageRanks() && rank >= 0 && rank <= 35) {
                        member.Rank = rank;
                    }
                    if (modifier.GuildMember().manageRights()) {
                        member.Rights = rights;
                    }
                }
            }
            if (modifier.GuildMember().manageXPContribution() || (modifier.GuildMember() == member && modifier.GuildMember().manageMyXpContribution())) {
                member.experienceGivenPercent = (xpPercent < 90) ? xpPercent : 90;
            }
            this.UpdateMember(member);
            if (this.Characters.containsKey(member.CharacterID)) {
                this.Characters.get(member.CharacterID).Send(new GuildMembershipMessage(this.toGuildInformations(), member.Rights, true));
            }

            result = true;
        }

        return result;
    }

    public boolean KickMember(GuildMember kickedMember, boolean kicked) {
        if (kickedMember.isBoss() && this.Members.size() > 1) {
            return false;
        }
        if (PlayerDAO.GetCharacter(kickedMember.CharacterID) != null) {
            PlayerDAO.GetCharacter(kickedMember.CharacterID).Guild = null;
        }
        if (this.Characters.containsKey(kickedMember.CharacterID)) {
            this.Characters.get(kickedMember.CharacterID).RefreshActor();
            this.Characters.get(kickedMember.CharacterID).Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 176, new String[0]));
            this.Characters.get(kickedMember.CharacterID).Send(new GuildLeftMessage());
            this.unregisterPlayer(this.Characters.get(kickedMember.CharacterID));
        }
        this.Members.remove(kickedMember.CharacterID);
        this.sendToField(new GuildMemberLeavingMessage(true, kickedMember.CharacterID));
        if (kickedMember.isBoss() && this.Members.isEmpty()) {
            this.DeleteGuild();
        }
        GuildDAO.Remove(kickedMember);
        return true;
    }

    public void DeleteGuild() {
        GuildDAO.Remove(Entity);
        //TODO : TAX
    }

    public boolean KickMember(Player kicker, GuildMember kickedMember) {
        if (kicker.GuildMember() != kickedMember && (!kicker.GuildMember().banMembers() || kickedMember.isBoss())) {
            return false;
        }
        if (kicker.ID != kickedMember.CharacterID) {
            kicker.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 117, new String[]{kickedMember.Name}));
        }
        return this.KickMember(kickedMember, kickedMember.CharacterID == kicker.ID);
    }

    private void OnMemberConnected(GuildMember member) {
        this.sendToField(new FieldNotification(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 224, new String[]{member.Name})) {
            @Override
            public boolean can(Player perso) {
                return perso.Account != null && perso.Account.Data != null && perso.Account.Data.guild_warn_on_login;
            }
        });
        this.sendToField(new GuildMemberOnlineStatusMessage(member.CharacterID, true));
    }

    private void OnMemberDisconnected(GuildMember member) {
        this.sendToField(new GuildMemberOnlineStatusMessage(member.CharacterID, false));
        this.sendToField(new GuildMemberLeavingMessage(false, member.CharacterID));
    }

    public GuildInfosUpgradeMessage toGuildInfosUpgradeMessage() {
        return new GuildInfosUpgradeMessage((byte) this.Entity.MaxTaxCollectors, /*byte taxCollectorsCoun*/ (byte) 0, TaxCollectorHealth(), TaxCollectorDamageBonuses(), this.Entity.Pods, this.Entity.Prospecting, this.Entity.Wisdom, this.Entity.Boost, TAX_COLLECTOR_SPELLS, this.SpellLevel);
    }

    public PaddockContentInformations[] toPaddockContentInformations() {
        return new PaddockContentInformations[0];
    }

    public HouseInformationsForGuild[] toHouseInformationsForGuild() {
        return new HouseInformationsForGuild[0];
    }

    public TaxCollectorListMessage toTaxCollectorListMessage() {
        return new TaxCollectorListMessage(new TaxCollectorInformations[0], (byte) this.Entity.MaxTaxCollectors, new TaxCollectorFightersInformation[0]);
    }

    public int TaxCollectorDamageBonuses() {
        return this.Entity.Level;
    }

    public int TaxCollectorHealth() {
        return TaxCollector.BaseHealth + (int) (20 * this.Entity.Level);

    }

    public GuildInformationsGeneralMessage toGeneralInfos() {
        return new GuildInformationsGeneralMessage(true, false, (byte) this.Entity.Level, (long) ExpDAO.GetFloorByLevel(this.Entity.Level).Guild, this.Entity.Experience(), (long) ExpDAO.GetFloorByLevel(this.Entity.Level + 1).Guild, this.Entity.CreationDate, this.Members.size(), this.Characters.size());
    }

    protected void UpdateMember(GuildMember member) {
        this.sendToField(new GuildInformationsMemberUpdateMessage(toGM(member)));
        member.Save();
    }

    public koh.protocol.types.game.guild.GuildMember[] allGuildMembers() {
        return this.Members.values().stream().map(x -> this.toGM(x)).toArray(koh.protocol.types.game.guild.GuildMember[]::new);
    }

    public koh.protocol.types.game.guild.GuildMember toGM(GuildMember m) {
        return new koh.protocol.types.game.guild.GuildMember(m.CharacterID, m.Level, m.Name, (byte) m.Breed, m.Sex, m.Rank, m.Experience(), (byte) m.experienceGivenPercent, m.Rights, this.Characters.containsKey(m.CharacterID) ? (byte) 1 : (byte) 0, (byte) m.alignmentSide, (short) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Long.parseLong(m.LastConnection)), this.Characters.containsKey(m.CharacterID) ? this.Characters.get(m.CharacterID).MoodSmiley : (byte) -1, m.AccountID, m.achievementPoints, new PlayerStatus(this.Characters.containsKey(m.CharacterID) ? this.Characters.get(m.CharacterID).Status.value() : 0));
    }

    @Override
    public void registerPlayer(Player p) {
        if (this.Characters == null) {
            this.Characters = Collections.synchronizedMap(new HashMap<Integer, Player>());
        }
        if (this.Characters.containsKey(p.ID)) {
            throw new Error("Two different Clients logged ! ");
        }
        this.Characters.put(p.ID, p);
        this.OnMemberConnected(this.Members.get(p.ID));
        super.registerPlayer(p);

        p.Send(new GuildMembershipMessage(this.toGuildInformations(), this.Members.get(p.ID).Rights, true));
        p.Send(this.toGeneralInfos());
        p.Send(new GuildInformationsMembersMessage(this.allGuildMembers()));
    }

    public boolean canAddMember() {
        return this.Members.size() < 50;
    }

    public GuildEmblem GetGuildEmblem() {
        if (EmblemeCache == null) {
            //EmblemSymbols Template = GuildEmblemDAO.Cache.get(this.Entity.EmblemForegroundShape);
            this.EmblemeCache = new GuildEmblem(this.Entity.EmblemForegroundShape, this.Entity.EmblemForegroundColor, (byte) this.Entity.EmblemBackgroundShape, this.Entity.EmblemBackgroundColor);
        }
        return EmblemeCache;
    }

    public GuildInformations toGuildInformations() {
        return new GuildInformations(this.Entity.GuildID, this.Entity.Name, this.GetGuildEmblem());
    }

    @Override
    public void unregisterPlayer(Player p) {
        if (this.Characters != null) {
            this.Characters.remove(p.ID);
        }
        super.unregisterPlayer(p);
        this.OnMemberDisconnected(this.Members.get(p.ID));
    }

    private static final double[][] XP_PER_GAP = new double[][]{
        new double[]{
            0.0,
            10.0
        },
        new double[]{
            10.0,
            8.0
        },
        new double[]{
            20.0,
            6.0
        },
        new double[]{
            30.0,
            4.0
        },
        new double[]{
            40.0,
            3.0
        },
        new double[]{
            50.0,
            2.0
        },
        new double[]{
            60.0,
            1.5
        },
        new double[]{
            70.0,
            1.0
        }
    };
    public static final int[] TAX_COLLECTOR_SPELLS = new int[]{
        458,
        457,
        456,
        455,
        462,
        460,
        459,
        451,
        453,
        454,
        452,
        461
    };

    public BasicGuildInformations GetBasicGuildInformations() {

        return new BasicGuildInformations(this.Entity.GuildID, this.Entity.Name);
    }

}
