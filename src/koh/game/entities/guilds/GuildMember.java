package koh.game.entities.guilds;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import koh.game.dao.DAO;
import koh.game.dao.sqlite.GuildDAOImpl;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "guilds_members")
public class GuildMember {

    public GuildMember() { }

    public void save() { DAO.getGuildMembers().update(this); }

    public GuildMember(int GID) {
        this.guildID = GID;
    }

    @DatabaseField(columnName = "char_id", dataType = DataType.INTEGER, id = true)
    public int characterID;

    @DatabaseField(columnName = "acc_id", dataType = DataType.INTEGER)
    public int accountID;

    @DatabaseField(columnName = "guild_id", dataType = DataType.INTEGER)
    public int guildID;

    @DatabaseField(columnName = "sex", dataType = DataType.BOOLEAN)
    public boolean sex;

    @DatabaseField(columnName = "level", dataType = DataType.INTEGER)
    public int level;

    @DatabaseField(columnName = "name", dataType = DataType.STRING)
    public String name;

    @DatabaseField(columnName = "breed", dataType = DataType.INTEGER)
    public int breed;

    @DatabaseField(columnName = "rank_id", dataType = DataType.INTEGER)
    public int rank;

    @DatabaseField(columnName = "rights", dataType = DataType.INTEGER)
    public int rights;

    @DatabaseField(columnName = "experience", dataType = DataType.STRING)
    public String experience;

    @DatabaseField(columnName = "xp_percent", dataType = DataType.INTEGER)
    public int experienceGivenPercent;

    @DatabaseField(columnName = "alignement", dataType = DataType.INTEGER)
    public int alignmentSide;

    @DatabaseField(columnName = "last_connection", dataType = DataType.STRING)
    public String lastConnection;

    @DatabaseField(columnName = "achievement_points", dataType = DataType.INTEGER)
    public int achievementPoints;

    public short getRankId() {
        return (short) ((this.rank < 0 || this.rank > 35) ? 0 : this.rank);
    }

    public long getExperience() {
        return Long.valueOf(this.experience);
    }

    public void addExperience(long value) {
        this.experience = String.valueOf(Long.valueOf(this.experience) + value);
        this.save();
    }

    public boolean isBoss() {
        return this.getRankId() == 1;
    }

    public boolean manageGuildBoosts() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((2 & this.rights) > 0))));
    }

    public boolean hasRight(int right) {
        return (((((this.isBoss()) || (this.manageRights()))) || (((right & this.rights) > 0))));
    }

    public boolean manageRights() {
        return (((this.isBoss()) || (((4 & this.rights) > 0))));
    }

    public boolean inviteNewMembers() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((8 & this.rights) > 0))));
    }

    public boolean banMembers() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((16 & this.rights) > 0))));
    }

    public boolean manageXPContribution() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((32 & this.rights) > 0))));
    }

    public boolean manageRanks() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((64 & this.rights) > 0))));
    }

    public boolean hireTaxCollector() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((128 & this.rights) > 0))));
    }

    public boolean manageMyXpContribution() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0100 & this.rights) > 0))));
    }

    public boolean collect() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0200 & this.rights) > 0))));
    }

    public boolean manageLightRights() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0400 & this.rights) > 0))));
    }

    public boolean useFarms() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x1000 & this.rights) > 0))));
    }

    public boolean organizeFarms() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x2000 & this.rights) > 0))));
    }

    public boolean takeOthersRidesInFarm() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x4000 & this.rights) > 0))));
    }

    public boolean prioritizeMeInDefense() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x8000 & this.rights) > 0))));
    }

    public boolean collectMyTaxCollectors() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((65536 & this.rights) > 0))));
    }

    public boolean setAlliancePrism() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((131072 & this.rights) > 0))));
    }

    public boolean talkInAllianceChannel() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((262144 & this.rights) > 0))));
    }

}
