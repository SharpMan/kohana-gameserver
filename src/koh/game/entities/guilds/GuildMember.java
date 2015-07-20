package koh.game.entities.guilds;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import koh.game.dao.GuildDAO;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.GuildRightsBitEnum;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "guilds_members")
public class GuildMember {

    public GuildMember() {

    }

    public void Save() {
        GuildDAO.Update(this);
    }

    public GuildMember(int GID) {
        this.GuildID = GID;
    }

    @DatabaseField(columnName = "char_id", dataType = DataType.INTEGER, id = true)
    public int CharacterID;

    @DatabaseField(columnName = "acc_id", dataType = DataType.INTEGER)
    public int AccountID;

    @DatabaseField(columnName = "guild_id", dataType = DataType.INTEGER)
    public int GuildID;

    @DatabaseField(columnName = "sex", dataType = DataType.BOOLEAN)
    public boolean Sex;

    @DatabaseField(columnName = "level", dataType = DataType.INTEGER)
    public int Level;

    @DatabaseField(columnName = "name", dataType = DataType.STRING)
    public String Name;

    @DatabaseField(columnName = "breed", dataType = DataType.INTEGER)
    public int Breed;

    @DatabaseField(columnName = "rank_id", dataType = DataType.INTEGER)
    public int Rank;

    @DatabaseField(columnName = "rights", dataType = DataType.INTEGER)
    public int Rights;

    @DatabaseField(columnName = "experience", dataType = DataType.STRING)
    public String Experience;

    @DatabaseField(columnName = "xp_percent", dataType = DataType.INTEGER)
    public int experienceGivenPercent;

    @DatabaseField(columnName = "alignement", dataType = DataType.INTEGER)
    public int alignmentSide;

    @DatabaseField(columnName = "last_connection", dataType = DataType.STRING)
    public String LastConnection;

    @DatabaseField(columnName = "achievement_points", dataType = DataType.INTEGER)
    public int achievementPoints;

    public short RankId() {
        return (short) ((this.Rank < 0 || this.Rank > 35) ? 0 : this.Rank);
    }

    public long Experience() {
        return Long.valueOf(this.Experience);
    }

    public void AddExperience(long b) {
        this.Experience = String.valueOf(Long.valueOf(this.Experience) + b);
        this.Save();
    }

    public boolean isBoss() {
        return this.RankId() == 1;
    }

    //Fucking code of course java don't have FlagBitsCheck on the Enumerate..
    public boolean manageGuildBoosts() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((2 & this.Rights) > 0))));
    }

    public boolean manageRights() {
        return (((this.isBoss()) || (((4 & this.Rights) > 0))));
    }

    public boolean inviteNewMembers() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((8 & this.Rights) > 0))));
    }

    public boolean banMembers() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((16 & this.Rights) > 0))));
    }

    public boolean manageXPContribution() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((32 & this.Rights) > 0))));
    }

    public boolean manageRanks() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((64 & this.Rights) > 0))));
    }

    public boolean hireTaxCollector() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((128 & this.Rights) > 0))));
    }

    public boolean manageMyXpContribution() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0100 & this.Rights) > 0))));
    }

    public boolean collect() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0200 & this.Rights) > 0))));
    }

    public boolean manageLightRights() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x0400 & this.Rights) > 0))));
    }

    public boolean useFarms() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x1000 & this.Rights) > 0))));
    }

    public boolean organizeFarms() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x2000 & this.Rights) > 0))));
    }

    public boolean takeOthersRidesInFarm() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x4000 & this.Rights) > 0))));
    }

    public boolean prioritizeMeInDefense() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((0x8000 & this.Rights) > 0))));
    }

    public boolean collectMyTaxCollectors() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((65536 & this.Rights) > 0))));
    }

    public boolean setAlliancePrism() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((131072 & this.Rights) > 0))));
    }

    public boolean talkInAllianceChannel() {
        return (((((this.isBoss()) || (this.manageRights()))) || (((262144 & this.Rights) > 0))));
    }

}
