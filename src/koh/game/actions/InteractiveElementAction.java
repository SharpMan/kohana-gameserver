package koh.game.actions;

import java.util.HashMap;
import java.util.Map;
import koh.game.Main;
import koh.game.actions.interactive.*;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.InteractiveActionEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.interactive.InteractiveUsedMessage;
import koh.protocol.types.game.interactive.InteractiveElementSkill;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveElementAction extends GameAction {

    private static final Logger logger = LogManager.getLogger(InteractiveElementAction.class);

    public static final Map<InteractiveActionEnum, InteractiveAction> HANDLERS = new HashMap<InteractiveActionEnum, InteractiveAction>() {
        {
            this.put(InteractiveActionEnum.ROAD_TO_INCARNAM, new Incarnam());
            this.put(InteractiveActionEnum.USE, new InteractiveUsage());
            this.put(InteractiveActionEnum.ZAAP_TELEPORT, new ZaapUse());
            this.put(InteractiveActionEnum.SAVE, new SavePos());
            this.put(InteractiveActionEnum.ACCESS, new Access());
            this.put(InteractiveActionEnum.TELEPORT, new TeleporterUse());
        }
    };

    public static boolean canDoAction(int skillID, Player actor) {
        return HANDLERS.get(InteractiveActionEnum.valueOf(skillID)) != null && HANDLERS.get(InteractiveActionEnum.valueOf(skillID)).isEnabled(actor);
    }

    static {
        DAO.getJobTemplates().consumeSkills((x -> x.gatheredRessourceItem != -1),(Skill -> HANDLERS.put(InteractiveActionEnum.valueOf(Skill.ID), new Collect(Skill))));
    }

    public InteractiveElementSkill skill;

    public InteractiveActionEnum action;
    public int elementID;

    public InteractiveElementAction(Player actor, InteractiveElementSkill skill, int elementID) {
        super(GameActionTypeEnum.INTERACTIVE_ELEMENT, actor);
        this.skill = skill;
        this.action = InteractiveActionEnum.valueOf(skill.skillId);
        this.elementID = elementID;
        logger.debug("Skill ID {} used",skill.skillId);
    }

    @Override
    public void execute() {
        if (action == null || !HANDLERS.containsKey(action)) {
            logger.debug("Action {} id not implanted",action);
            PlayerController.sendServerMessage(((Player) actor).client, "L'utilisation de cet object intéractif est indisponnible pour le moment ...");
            actor.send(new BasicNoOperationMessage());
            return;
        }
        ((Player) this.actor).currentMap.sendToField(new InteractiveUsedMessage(actor.ID, elementID, skill.skillId, HANDLERS.get(action).getDuration()));
        HANDLERS.get(action).execute((Player) this.actor, elementID);

        super.execute();
    }

    @Override
    public void abort(Object[] Args) {
        if (this.HANDLERS.get(action) != null) {
            this.HANDLERS.get(action).abort((Player) this.actor, elementID);
        }
        super.abort(Args);

    }

    @Override
    public void endExecute() throws Exception {
        if (this.HANDLERS.get(action) != null) {
            this.HANDLERS.get(action).leave((Player) this.actor, elementID);
        }
        super.endExecute();
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        if (ActionType == GameActionTypeEnum.ZAAP) {
            return true;
        }
        return false;
    }

}
