package koh.game.entities.actors;

import koh.game.dao.NpcDAO;
import koh.game.entities.actors.npc.NpcTemplate;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayNpcInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayNpcWithQuestInformations;
import koh.protocol.types.game.context.roleplay.quest.GameRolePlayNpcQuestFlag;
import koh.protocol.types.game.look.EntityLook;

/**
 *
 * @author Neo-Craft
 */
public class Npc extends IGameActor {

    public int NpcId, Artwork;
    public short CellID;
    public boolean Sex;
    public GameContextActorInformations ContextInformation;
    public int[] QuestsToValid, QuestsToStart;

    //Todo QuestTODO:
    public NpcTemplate Template() {
        return NpcDAO.Cache.get(this.NpcId);
    }

    @Override
    public EntityLook GetEntityLook() {
        if (entityLook == null) {
            this.entityLook = this.Template().GetEntityLook();
        }
        return entityLook;
    }

    @Override
    public GameContextActorInformations GetGameContextActorInformations(Player character) {
        if (ContextInformation == null) {
            if (this.QuestsToStart.length > 0 || this.QuestsToValid.length > 0) {
                this.ContextInformation = new GameRolePlayNpcWithQuestInformations(this.ID, this.GetEntityLook(), this.GetEntityDispositionInformations(character), (short) this.Template().Id, this.Sex, this.Artwork, new GameRolePlayNpcQuestFlag(this.QuestsToValid, this.QuestsToStart));
            } else {
                this.ContextInformation = new GameRolePlayNpcInformations(this.ID, this.GetEntityLook(), this.GetEntityDispositionInformations(character), (short) this.Template().Id, this.Sex, this.Artwork);
            }
        }
        return ContextInformation;
    }
}
