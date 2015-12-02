package koh.game.entities.actors;

import koh.game.dao.DAO;
import koh.game.dao.mysql.NpcDAOImpl;
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

    public int npcId, artwork;
    public short cellID;
    public boolean sex;
    public GameContextActorInformations contextActorInformations;
    public int[] questsToValid, questsToStart;

    //Todo QuestTODO:
    public NpcTemplate getTemplate() {
        return DAO.getNpcs().findTemplate(this.npcId);
    }

    @Override
    public EntityLook getEntityLook() {
        if (entityLook == null) {
            this.entityLook = this.getTemplate().getEntityLook();
        }
        return entityLook;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        if (contextActorInformations == null) {
            if (this.questsToStart.length > 0 || this.questsToValid.length > 0) {
                this.contextActorInformations = new GameRolePlayNpcWithQuestInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), (short) this.getTemplate().id, this.sex, this.artwork, new GameRolePlayNpcQuestFlag(this.questsToValid, this.questsToStart));
            } else {
                this.contextActorInformations = new GameRolePlayNpcInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), (short) this.getTemplate().id, this.sex, this.artwork);
            }
        }
        return contextActorInformations;
    }
}
