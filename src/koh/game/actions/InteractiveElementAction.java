package koh.game.actions;

import java.util.HashMap;
import java.util.Map;
import koh.game.Main;
import koh.game.actions.interactive.*;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.JobDAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.InteractiveActionEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.interactive.InteractiveUsedMessage;
import koh.protocol.types.game.interactive.InteractiveElementSkill;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveElementAction extends GameAction {

    public static final Map<InteractiveActionEnum, InteractiveAction> Handlers = new HashMap<InteractiveActionEnum, InteractiveAction>() {
        {
            this.put(InteractiveActionEnum.ROAD_TO_INCARNAM, new Incarnam());
            this.put(InteractiveActionEnum.USE, new InteractiveUsage());
            this.put(InteractiveActionEnum.ZAAP_TELEPORT, new ZaapUse());
            this.put(InteractiveActionEnum.SAVE, new SavePos());
            this.put(InteractiveActionEnum.ACCESS, new Access());
            this.put(InteractiveActionEnum.TELEPORT, new TeleporterUse());
        }
    };

    public static boolean canDoAction(int SkillID, Player Actor) {
        return Handlers.get(InteractiveActionEnum.valueOf(SkillID)) != null && Handlers.get(InteractiveActionEnum.valueOf(SkillID)).isEnabled(Actor);
    }

    static {
        JobDAO.Skills.values().stream().filter(x -> x.gatheredRessourceItem != -1).forEach(Skill -> Handlers.put(InteractiveActionEnum.valueOf(Skill.ID), new Collect(Skill)));
    }

    public InteractiveElementSkill Skill;

    public InteractiveActionEnum Action;
    public int ElementID;

    public InteractiveElementAction(Player Actor, InteractiveElementSkill Skill, int ElementID) {
        super(GameActionTypeEnum.INTERACTIVE_ELEMENT, Actor);
        this.Skill = Skill;
        this.Action = InteractiveActionEnum.valueOf(Skill.skillId);
        this.ElementID = ElementID;
        Main.Logs().writeDebug(Skill.skillId + " used");
    }

    @Override
    public void Execute() {
        if (Action == null || !Handlers.containsKey(Action)) {
            Main.Logs().writeDebug(Action + " Id not implanted");
            PlayerController.SendServerMessage(((Player) Actor).Client, "L'utilisation de cet object intéractif est indisponnible pour le moment ...");
            Actor.Send(new BasicNoOperationMessage());
            return;
        }
        ((Player) this.Actor).CurrentMap.sendToField(new InteractiveUsedMessage(Actor.ID, ElementID, Skill.skillId, Handlers.get(Action).GetDuration()));
        Handlers.get(Action).Execute((Player) this.Actor, ElementID);

        super.Execute();
    }

    @Override
    public void Abort(Object[] Args) {
        if (this.Handlers.get(Action) != null) {
            this.Handlers.get(Action).Abort((Player) this.Actor, ElementID);
        }
        super.Abort(Args);

    }

    @Override
    public void EndExecute() throws Exception {
        if (this.Handlers.get(Action) != null) {
            this.Handlers.get(Action).Leave((Player) this.Actor, ElementID);
        }
        super.EndExecute();
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        if (ActionType == GameActionTypeEnum.ZAAP) {
            return true;
        }
        return false;
    }

}
