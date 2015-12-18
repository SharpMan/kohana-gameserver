package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.dao.sqlite.GuildDAOImpl;
import koh.game.entities.guilds.GuildMember;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.GuildInvitationStateEnum;
import koh.protocol.client.enums.GuildRightsBitEnum;
import koh.protocol.messages.game.guild.GuildInvitationStateRecrutedMessage;
import koh.protocol.messages.game.guild.GuildInvitationStateRecruterMessage;

/**
 *
 * @author Neo-Craft
 */
public class GuildJoinRequest extends GameBaseRequest {

    public GuildJoinRequest(WorldClient Client, WorldClient Target) {
        super(Client, Target);
    }

    @Override
    public boolean accept() {
        if (!super.declin()) {
            return false;
        }

        try {
            this.requester.send(new GuildInvitationStateRecruterMessage(this.requested.getCharacter().getNickName(), GuildInvitationStateEnum.GUILD_INVITATION_OK));
            this.requested.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_OK));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            if (this.requester.getCharacter().getGuild() != null) {
                this.requester.getCharacter().getGuild().addMember(new GuildMember(this.requester.getCharacter().getGuild().entity.guildID) {
                    {
                        this.accountID = requested.getAccount().id;
                        this.breed = requested.getCharacter().getBreed();
                        this.characterID = requested.getCharacter().ID;
                        this.lastConnection = System.currentTimeMillis() + "";
                        this.level = requested.getCharacter().getLevel();
                        this.name = requested.getCharacter().getNickName();
                        this.rank = 0;
                        this.experience = "0";
                        this.rights = GuildRightsBitEnum.GUILD_RIGHT_NONE;
                        this.sex = requested.getCharacter().hasSexe();
                        this.achievementPoints = requested.getCharacter().getAchievementPoints();
                        this.alignmentSide = requested.getCharacter().getAlignmentSide().value;
                        DAO.getGuildMembers().insert(this);
                    }
                }, this.requested.getCharacter());
                this.requester.getCharacter().getGuild().registerPlayer(requested.getCharacter());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.requester.setBaseRequest(null);
            this.requested.setBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean declin() {
        if (!super.declin()) {
            return false;
        }

        try {
            this.requester.send(new GuildInvitationStateRecruterMessage(this.requested.getCharacter().getNickName(), GuildInvitationStateEnum.GUILD_INVITATION_CANCELED));
            this.requested.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_CANCELED));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.requester.setBaseRequest(null);
            this.requested.setBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum action) {
        return false;
    }

}
