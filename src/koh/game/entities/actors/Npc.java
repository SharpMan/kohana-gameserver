package koh.game.entities.actors;

import koh.game.dao.DAO;
import koh.game.entities.actors.npc.NpcTemplate;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayNpcInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayNpcWithQuestInformations;
import koh.protocol.types.game.context.roleplay.quest.GameRolePlayNpcQuestFlag;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Enumerable;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class Npc extends IGameActor {

    private int npcId, artwork;
    @Getter
    private short cellID;
    private boolean sex;
    private GameContextActorInformations contextActorInformations;
    private int[] questsToValid, questsToStart;

    public Npc(ResultSet result) throws SQLException {
        super();
        this.mapid = result.getInt("map");
        this.cellID = result.getShort("cell");
        this.direction = result.getByte("direction");
        this.sex = result.getBoolean("sex");
        this.npcId = result.getInt("id");
        this.artwork = result.getInt("artwork");
        this.questsToStart = Enumerable.stringToIntArray(result.getString("quests_to_start"));
        this.questsToValid = Enumerable.stringToIntArray(result.getString("quests_to_valid"));
    }

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
                this.contextActorInformations = new GameRolePlayNpcWithQuestInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), (short) this.getTemplate().getId(), this.sex, this.artwork, new GameRolePlayNpcQuestFlag(this.questsToValid, this.questsToStart));
            } else {
                this.contextActorInformations = new GameRolePlayNpcInformations(this.ID, this.getEntityLook(), this.getEntityDispositionInformations(character), (short) this.getTemplate().getId(), this.sex, this.artwork);
            }
        }
        return contextActorInformations;
    }
}
