package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.sqlite.GuildDAO;
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
            this.requester.send(new GuildInvitationStateRecruterMessage(this.requested.character.nickName, GuildInvitationStateEnum.GUILD_INVITATION_OK));
            this.requested.send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_OK));

            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            if (this.requester.character.guild != null) {
                this.requester.character.guild.addMember(new GuildMember(this.requester.character.guild.entity.guildID) {
                    {
                        this.accountID = requested.getAccount().id;
                        this.breed = requested.character.breed;
                        this.characterID = requested.character.ID;
                        this.lastConnection = System.currentTimeMillis() + "";
                        this.level = requested.character.level;
                        this.name = requested.character.nickName;
                        this.rank = 0;
                        this.experience = "0";
                        this.rights = GuildRightsBitEnum.GUILD_RIGHT_NONE;
                        this.sex = requested.character.sexe == 1;
                        this.achievementPoints = requested.character.achievementPoints;
                        this.alignmentSide = requested.character.alignmentSide.value;
                        GuildDAO.Insert(this);
                    }
                }, this.requested.character);
                this.requester.character.guild.registerPlayer(requested.character);
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
            this.requester.send(new GuildInvitationStateRecruterMessage(this.requested.character.nickName, GuildInvitationStateEnum.GUILD_INVITATION_CANCELED));
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
