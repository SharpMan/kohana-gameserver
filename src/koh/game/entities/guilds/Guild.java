package koh.game.entities.guilds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import koh.game.Main;
import koh.game.dao.DAO;
import koh.game.dao.api.AccountDataDAO;
import koh.game.dao.sqlite.GuildDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.network.ChatChannel;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class Guild extends IWorldEventObserver {

    private static final Logger logger = LogManager.getLogger(Guild.class);

    public volatile ChatChannel chatChannel = new ChatChannel();

    private final Map<Integer, Player> characters = new ConcurrentHashMap<>();
    private final Map<Integer, GuildMember> members = new ConcurrentHashMap<>();

    public final GuildEntity entity;
    private GuildMember bossCache;
    private GuildEmblem emblemeCache;
    public byte[] spellLevel;

    public Guild(GuildEntity Entity) {
        this.entity = Entity;
        this.spellLevel = Enumerable.StringToByteArray(Entity.spells);
    }

    public void onFighterAddedExperience(GuildMember member, long XP) {
        member.addExperience(XP);
        this.entity.addExperience(XP);
        while (this.entity.getExperience() >= DAO.getExps().getLevel(this.entity.level + 1).getGuild() && this.entity.level < DAO.getSettings().getIntElement("Max.GuildLevel")) {
            this.entity.level++;
            this.entity.boost += 5;
            this.sendToField(new GuildLevelUpMessage(this.entity.level));
        }
    }

    public GuildMember getBoss() {
        if (this.bossCache == null) {
            this.members.values().stream().filter(GuildMember::isBoss).forEach(GM -> {
                if (this.bossCache != null) {
                    logger.error("There is at least two boss in guild {0} ({1}) BossSecond {2}", this.entity.guildID, this.entity.name, GM.name);
                }
                this.bossCache = GM;
            });
            if (this.bossCache == null) {
                if (this.members.isEmpty()) {
                    throw new Error(String.format("guild {0} ({1}) is empty", this.entity.guildID, this.entity.name));
                }

            }
        }
        return this.bossCache;
    }

    public void setBoss(GuildMember GuildMember) {
        if (this.bossCache != GuildMember) {
            if (this.bossCache != null) {
                this.bossCache.rank = 0;
                this.bossCache.rights = GuildRightsBitEnum.GUILD_RIGHT_NONE;
                this.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 199, new String[]{GuildMember.name, this.bossCache.name, this.entity.name}));
                this.updateMember(this.bossCache);
            }
            this.bossCache = GuildMember;
            this.bossCache.rank = 1;
            this.bossCache.rights = GuildRightsBitEnum.GUILD_RIGHT_BOSS;
            this.updateMember(GuildMember);
        }
    }

    public void addMember(GuildMember member, Player Client) {
        this.members.put(member.characterID, member);
        Client.guild = this;
        Client.refreshActor();
        Client.send(new GuildJoinedMessage(this.toGuildInformations(), member.rights, true));
    }

    public synchronized boolean changeParameters(Player modifier, GuildMember member, int rank, byte xpPercent, int rights) {
        boolean result;

        if (modifier.guild.entity.guildID != member.guildID) {
            result = false;
        } else {
            if (modifier.getGuildMember() != member && modifier.getGuildMember().isBoss() && rank == 1) {
                this.setBoss(member);
            } else {
                if (modifier.getGuildMember() == member || !member.isBoss()) {
                    if (modifier.getGuildMember().manageRanks() && rank >= 0 && rank <= 35) {
                        member.rank = rank;
                    }
                    if (modifier.getGuildMember().manageRights()) {
                        member.rights = rights;
                    }
                }
            }
            if (modifier.getGuildMember().manageXPContribution() || (modifier.getGuildMember() == member && modifier.getGuildMember().manageMyXpContribution())) {
                member.experienceGivenPercent = (xpPercent < 90) ? xpPercent : 90;
            }
            this.updateMember(member);
            if (this.characters.containsKey(member.characterID)) {
                this.characters.get(member.characterID).send(new GuildMembershipMessage(this.toGuildInformations(), member.rights, true));
            }

            result = true;
        }

        return result;
    }

    public boolean kickMember(GuildMember target, boolean kicked) {
        if (target.isBoss() && this.members.size() > 1) {
            return false;
        }
        if (DAO.getPlayers().getCharacter(target.characterID) != null) {
            DAO.getPlayers().getCharacter(target.characterID).guild = null;
        }
        if (this.characters.containsKey(target.characterID)) {
            this.characters.get(target.characterID).refreshActor();
            this.characters.get(target.characterID).send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 176, new String[0]));
            this.characters.get(target.characterID).send(new GuildLeftMessage());
            this.unregisterPlayer(this.characters.get(target.characterID));
        }
        this.members.remove(target.characterID);
        this.sendToField(new GuildMemberLeavingMessage(true, target.characterID));
        if (target.isBoss() && this.members.isEmpty()) {
            this.deleteGuild();
        }

        DAO.getGuildMembers().delete(target);

        return true;
    }

    public void deleteGuild() {
        DAO.getGuilds().remove(entity);
        //TODO : TAX
    }

    public boolean kickMember(Player kicker, GuildMember kickedMember) {
        if (kicker.getGuildMember() != kickedMember && (!kicker.getGuildMember().banMembers() || kickedMember.isBoss())) {
            return false;
        }
        if (kicker.ID != kickedMember.characterID) {
            kicker.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 117, new String[]{kickedMember.name}));
        }
        return this.kickMember(kickedMember, kickedMember.characterID == kicker.ID);
    }

    private void onMemberConnected(GuildMember member) {
        this.sendToField(new FieldNotification(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 224, new String[]{member.name})) {
            @Override
            public boolean can(Player perso) {
                return perso.account != null && perso.account.accountData != null && perso.account.accountData.guild_warn_on_login;
            }
        });
        this.sendToField(new GuildMemberOnlineStatusMessage(member.characterID, true));
    }

    private void onMemberDisconnected(GuildMember member) {
        this.sendToField(new GuildMemberOnlineStatusMessage(member.characterID, false));
        this.sendToField(new GuildMemberLeavingMessage(false, member.characterID));
    }

    public GuildInfosUpgradeMessage toGuildInfosUpgradeMessage() {
        return new GuildInfosUpgradeMessage((byte) this.entity.maxTaxCollectors, /*byte taxCollectorsCoun*/ (byte) 0, getTaxCollectorHealth(), gettaxCollectorDamageBonuses(), this.entity.pods, this.entity.prospecting, this.entity.wisdom, this.entity.boost, TAX_COLLECTOR_SPELLS, this.spellLevel);
    }

    public PaddockContentInformations[] toPaddockContentInformations() {
        return new PaddockContentInformations[0];
    }

    public HouseInformationsForGuild[] toHouseInformationsForGuild() {
        return new HouseInformationsForGuild[0];
    }

    public TaxCollectorListMessage toTaxCollectorListMessage() {
        return new TaxCollectorListMessage(new TaxCollectorInformations[0], (byte) this.entity.maxTaxCollectors, new TaxCollectorFightersInformation[0]);
    }

    public int gettaxCollectorDamageBonuses() {
        return this.entity.level;
    }

    public int getTaxCollectorHealth() {
        return TaxCollector.BASE_HEALTH + (int) (20 * this.entity.level);

    }

    public GuildInformationsGeneralMessage toGeneralInfos() {
        return new GuildInformationsGeneralMessage(true, false, (byte) this.entity.level, (long) DAO.getExps().getLevel(this.entity.level).getGuild(), this.entity.getExperience(), (long) DAO.getExps().getLevel(this.entity.level + 1).getGuild(), this.entity.creationDate, this.members.size(), this.characters.size());
    }

    protected void updateMember(GuildMember member) {
        this.sendToField(new GuildInformationsMemberUpdateMessage(toGM(member)));
        member.save();
    }

    public koh.protocol.types.game.guild.GuildMember[] allGuildMembers() {
        return this.members.values().stream().map(x -> this.toGM(x)).toArray(koh.protocol.types.game.guild.GuildMember[]::new);
    }

    public koh.protocol.types.game.guild.GuildMember toGM(GuildMember m) {
        return new koh.protocol.types.game.guild.GuildMember(m.characterID, m.level, m.name, (byte) m.breed, m.sex, m.rank, m.getExperience(), (byte) m.experienceGivenPercent, m.rights, this.characters.containsKey(m.characterID) ? (byte) 1 : (byte) 0, (byte) m.alignmentSide, (short) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Long.parseLong(m.lastConnection)), this.characters.containsKey(m.characterID) ? this.characters.get(m.characterID).moodSmiley : (byte) -1, m.accountID, m.achievementPoints, new PlayerStatus(this.characters.containsKey(m.characterID) ? this.characters.get(m.characterID).status.value() : 0));
    }

    @Override
    public void registerPlayer(Player p) {
        if (this.characters.containsKey(p.ID)) {
            throw new Error("Two different Clients logged ! ");
        }
        this.characters.put(p.ID, p);
        this.onMemberConnected(this.members.get(p.ID));
        super.registerPlayer(p);

        p.send(new GuildMembershipMessage(this.toGuildInformations(), this.members.get(p.ID).rights, true));
        p.send(this.toGeneralInfos());
        p.send(new GuildInformationsMembersMessage(this.allGuildMembers()));
    }

    public boolean canAddMember() {
        return this.members.size() < 50;
    }

    public GuildEmblem getGuildEmblem() {
        if (emblemeCache == null) {
            //EmblemSymbols getTemplate = GuildEmblemDAOImpl.dofusMaps.get(this.entity.emblemForegroundShape);
            this.emblemeCache = new GuildEmblem(this.entity.emblemForegroundShape, this.entity.emblemForegroundColor, (byte) this.entity.emblemBackgroundShape, this.entity.emblemBackgroundColor);
        }
        return emblemeCache;
    }

    public GuildInformations toGuildInformations() {
        return new GuildInformations(this.entity.guildID, this.entity.name, this.getGuildEmblem());
    }

    @Override
    public void unregisterPlayer(Player p) {
        if (this.characters != null) {
            this.characters.remove(p.ID);
        }
        super.unregisterPlayer(p);
        this.onMemberDisconnected(this.members.get(p.ID));
    }

    public Stream<Player> playerStream() {
        return characters.values().stream();
    }

    public Stream<GuildMember> memberStream() {
        return members.values().stream();
    }

    public BasicGuildInformations getBasicGuildInformations() {
        //TODO cache that !
        return new BasicGuildInformations(this.entity.guildID, this.entity.name);
    }

    public void addMember(GuildMember member) {
        members.put(member.characterID, member);
    }

    public GuildMember getMember(int playerId) {
        return members.get(playerId);
    }

    /*private static final double[][] XP_PER_GAP = new double[][]{
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
    };*/
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

}
