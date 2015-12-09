package koh.game.actions.interactive;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.jobs.InteractiveSkill;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.interactive.InteractiveElementUpdatedMessage;
import koh.protocol.messages.game.interactive.InteractiveUseEndedMessage;
import koh.protocol.messages.game.interactive.StatedElementUpdatedMessage;
import koh.protocol.messages.game.inventory.items.ObtainedItemMessage;
import koh.protocol.messages.game.inventory.items.ObtainedItemWithBonusMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class Collect implements InteractiveAction {

    private static final Logger logger = LogManager.getLogger(InteractiveAction.class);

    private final InteractiveSkill Skill;
    public float AgeBonus;
    public boolean Aborted;

    public Collect(InteractiveSkill Skill) {
        this.Skill = Skill;
    }

    @Override
    public boolean isEnabled(Player actor) {
        try {
            return actor.myJobs.getJob(Skill.getParentJobId()).jobLevel >= Skill.getLevelMin();
        } catch (Exception e) {
            logger.error("Enabled {} with SkillLevel {}", Skill.getParentJobId(), Skill.getLevelMin());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void execute(Player actor, int element) {
        if (!this.isEnabled(actor)) {
            actor.send(new BasicNoOperationMessage());
            actor.client.delGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        if (actor.currentMap.getStatedElementById(element).elementState == 2) {
            PlayerController.sendServerMessage(actor.client, "Impossible de collecter un item déjà en recolte.");
            actor.client.delGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        actor.currentMap.getStatedElementById(element).elementState = 2;
        this.AgeBonus = actor.currentMap.getInteractiveElementStruct(element).ageBonus;
        actor.currentMap.getInteractiveElementStruct(element).ageBonus = -1;

        actor.currentMap.sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(actor.currentMap.toInteractiveElement(Player, element))));
        actor.currentMap.sendToField(new StatedElementUpdatedMessage(actor.currentMap.getStatedElementById(element)));
        new CancellableScheduledRunnable(actor.currentMap.getArea().backGroundWorker, this.getDuration() * 100) {
            @Override
            public void run() {
                try {
                    actor.client.endGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
                } catch (Exception e) {
                }
            }
        };

    }

    @Override
    public int getDuration() {
        return 30;
    }

    @Override
    public void leave(Player player, int element) {
        if (Aborted) {
            return;
        }
        player.currentMap.sendToField(new InteractiveUseEndedMessage(element, this.Skill.getID()));
        int quantityGathered = EffectHelper.randomValue(player.myJobs.getJob(Skill.getParentJobId()).quantity(Skill.getLevelMin()));
        int bonusQuantity = EffectHelper.randomValue(player.myJobs.getJob(Skill.getParentJobId()).jobEntity(Skill.getLevelMin()).getBonusMin(), player.myJobs.getJob(Skill.getParentJobId()).jobEntity(Skill.getLevelMin()).getBonusMax());
        if (AgeBonus > 0) {
            bonusQuantity += (int) ((float) bonusQuantity * AgeBonus / 100);
        }
        InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), Skill.getGatheredRessourceItem(), 63, player.ID, bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered, EffectHelper.generateIntegerEffect(DAO.getItemTemplates().getTemplate(Skill.getGatheredRessourceItem()).getPossibleEffects(), EffectGenerationType.Normal, false));
        if (player.inventoryCache.add(item, true)) {
            item.setNeedInsert(true);
        }
        player.myJobs.getJob(Skill.getParentJobId()).gatheringItems += bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered;
        player.send(bonusQuantity > 0 ? new ObtainedItemWithBonusMessage(Skill.getGatheredRessourceItem(), quantityGathered, bonusQuantity) : new ObtainedItemMessage(Skill.getGatheredRessourceItem(), quantityGathered));
        player.myJobs.addExperience(player, Skill.getParentJobId(), player.myJobs.getJob(Skill.getParentJobId()).jobEntity(Skill.getLevelMin()).getXpEarned() * DAO.getSettings().getIntElement("job.Rate"));
        player.currentMap.getStatedElementById(element).elementState = 1;
        player.currentMap.getStatedElementById(element).deadAt = System.currentTimeMillis();
        player.currentMap.getInteractiveElementStruct(element).ageBonus = -1;

        player.currentMap.sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(player.currentMap.toInteractiveElement(Player, element))));
        player.currentMap.sendToField(new StatedElementUpdatedMessage(player.currentMap.getStatedElementById(element)));

    }

    @Override
    public void abort(Player Actor, int element) {
        this.Aborted = true;
        Actor.currentMap.getStatedElementById(element).elementState = 0;
        Actor.currentMap.getInteractiveElementStruct(element).ageBonus = (short) this.AgeBonus;

        Actor.currentMap.sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(Actor.currentMap.toInteractiveElement(Player, element))));
        Actor.currentMap.sendToField(new StatedElementUpdatedMessage(Actor.currentMap.getStatedElementById(element)));

    }

}
