package koh.game.actions.interactive;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import koh.commons.CancellableExecutorRunnable;
import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.jobs.InteractiveSkill;
import koh.game.utils.Settings;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.interactive.InteractiveElementUpdatedMessage;
import koh.protocol.messages.game.interactive.InteractiveUseEndedMessage;
import koh.protocol.messages.game.interactive.StatedElementUpdatedMessage;
import koh.protocol.messages.game.inventory.items.ObtainedItemMessage;
import koh.protocol.messages.game.inventory.items.ObtainedItemWithBonusMessage;

/**
 *
 * @author Neo-Craft
 */
public class Collect implements InteractiveAction {

    private final InteractiveSkill Skill;
    public float AgeBonus;
    public boolean Aborted;

    public Collect(InteractiveSkill Skill) {
        this.Skill = Skill;
    }

    @Override
    public boolean isEnabled(Player Actor) {
        try {
            return Actor.myJobs.GetJob(Skill.parentJobId).jobLevel >= Skill.levelMin;
        } catch (Exception e) {
            Main.Logs().writeError(String.format("Enabled %s with SkillLevel %s", Skill.parentJobId, Skill.levelMin));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void Execute(Player Actor, int Element) {
        if (!this.isEnabled(Actor)) {
            Actor.Send(new BasicNoOperationMessage());
            Actor.Client.DelGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        if (Actor.CurrentMap.GetStatedElementById(Element).elementState == 2) {
            PlayerController.SendServerMessage(Actor.Client, "Impossible de collecter un item déjà en recolte.");
            Actor.Client.DelGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        Actor.CurrentMap.GetStatedElementById(Element).elementState = 2;
        this.AgeBonus = Actor.CurrentMap.GetInteractiveElementStruct(Element).AgeBonus;
        Actor.CurrentMap.GetInteractiveElementStruct(Element).AgeBonus = -1;

        Actor.CurrentMap.sendToField(Player -> Player.Send(new InteractiveElementUpdatedMessage(Actor.CurrentMap.toInteractiveElement(Player, Element))));
        Actor.CurrentMap.sendToField(new StatedElementUpdatedMessage(Actor.CurrentMap.GetStatedElementById(Element)));
        new CancellableExecutorRunnable(Actor.CurrentMap.getArea().BackGroundWorker, this.GetDuration() * 100) {
            @Override
            public void run() {
                try {
                    Actor.Client.EndGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
                } catch (Exception e) {
                }
            }
        };

    }

    @Override
    public int GetDuration() {
        return 30;
    }

    @Override
    public void Leave(Player Actor, int Element) {
        if (Aborted) {
            return;
        }
        Actor.CurrentMap.sendToField(new InteractiveUseEndedMessage(Element, this.Skill.ID));
        int quantityGathered = EffectHelper.RandomValue(Actor.myJobs.GetJob(Skill.parentJobId).Quantity(Skill.levelMin));
        int bonusQuantity = EffectHelper.RandomValue(Actor.myJobs.GetJob(Skill.parentJobId).JobEntity(Skill.levelMin).bonusMin, Actor.myJobs.GetJob(Skill.parentJobId).JobEntity(Skill.levelMin).bonusMax);
        if (AgeBonus > 0) {
            bonusQuantity += (int) ((float) bonusQuantity * AgeBonus / 100);
        }
        InventoryItem Item = InventoryItem.Instance(ItemDAO.NextID++, Skill.gatheredRessourceItem, 63, Actor.ID, bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered, EffectHelper.GenerateIntegerEffect(ItemDAO.Cache.get(Skill.gatheredRessourceItem).possibleEffects, EffectGenerationType.Normal, false));
        if (Actor.InventoryCache.Add(Item, true)) {
            Item.NeedInsert = true;
        }
        Actor.myJobs.GetJob(Skill.parentJobId).gatheringItems += bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered;
        Actor.Send(bonusQuantity > 0 ? new ObtainedItemWithBonusMessage(Skill.gatheredRessourceItem, quantityGathered, bonusQuantity) : new ObtainedItemMessage(Skill.gatheredRessourceItem, quantityGathered));
        Actor.myJobs.addExperience(Actor, Skill.parentJobId, Actor.myJobs.GetJob(Skill.parentJobId).JobEntity(Skill.levelMin).xpEarned * Settings.GetIntElement("Job.Rate"));
        Actor.CurrentMap.GetStatedElementById(Element).elementState = 1;
        Actor.CurrentMap.GetStatedElementById(Element).deadAt = System.currentTimeMillis();
        Actor.CurrentMap.GetInteractiveElementStruct(Element).AgeBonus = -1;

        Actor.CurrentMap.sendToField(Player -> Player.Send(new InteractiveElementUpdatedMessage(Actor.CurrentMap.toInteractiveElement(Player, Element))));
        Actor.CurrentMap.sendToField(new StatedElementUpdatedMessage(Actor.CurrentMap.GetStatedElementById(Element)));

    }

    @Override
    public void Abort(Player Actor, int Element) {
        this.Aborted = true;
        Actor.CurrentMap.GetStatedElementById(Element).elementState = 0;
        Actor.CurrentMap.GetInteractiveElementStruct(Element).AgeBonus = (short) this.AgeBonus;

        Actor.CurrentMap.sendToField(Player -> Player.Send(new InteractiveElementUpdatedMessage(Actor.CurrentMap.toInteractiveElement(Player, Element))));
        Actor.CurrentMap.sendToField(new StatedElementUpdatedMessage(Actor.CurrentMap.GetStatedElementById(Element)));

    }

}
