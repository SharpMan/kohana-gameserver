package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.GuildDAO;
import koh.game.entities.guilds.GuildMember;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
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
    public boolean Accept() {
        if (!super.Declin()) {
            return false;
        }

        try {
            this.Requester.Send(new GuildInvitationStateRecruterMessage(this.Requested.Character.NickName, GuildInvitationStateEnum.GUILD_INVITATION_OK));
            this.Requested.Send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_OK));

            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            if (this.Requester.Character.Guild != null) {
                this.Requester.Character.Guild.addMember(new GuildMember(this.Requester.Character.Guild.Entity.GuildID) {
                    {
                        this.AccountID = Requested.getAccount().ID;
                        this.Breed = Requested.Character.Breed;
                        this.CharacterID = Requested.Character.ID;
                        this.LastConnection = System.currentTimeMillis() + "";
                        this.Level = Requested.Character.Level;
                        this.Name = Requested.Character.NickName;
                        this.Rank = 0;
                        this.Experience = "0";
                        this.Rights = GuildRightsBitEnum.GUILD_RIGHT_NONE;
                        this.Sex = Requested.Character.Sexe == 1;
                        this.achievementPoints = Requested.Character.achievementPoints;
                        this.alignmentSide = Requested.Character.AlignmentSide.value;
                        GuildDAO.Insert(this);
                    }
                }, this.Requested.Character);
                this.Requester.Character.Guild.registerPlayer(Requested.Character);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.Requester.SetBaseRequest(null);
            this.Requested.SetBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean Declin() {
        if (!super.Declin()) {
            return false;
        }

        try {
            this.Requester.Send(new GuildInvitationStateRecruterMessage(this.Requested.Character.NickName, GuildInvitationStateEnum.GUILD_INVITATION_CANCELED));
            this.Requested.Send(new GuildInvitationStateRecrutedMessage(GuildInvitationStateEnum.GUILD_INVITATION_CANCELED));

            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.Requester.SetBaseRequest(null);
            this.Requested.SetBaseRequest(null);
        }
        return true;
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum Action) {
        return false;
    }

}
