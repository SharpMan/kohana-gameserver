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

    private final InteractiveSkill skill;
    public float ageBonus;
    public boolean Aborted;

    public Collect(InteractiveSkill Skill) {
        this.skill = Skill;
    }

    @Override
    public boolean isEnabled(Player actor) {
        try {
            if(skill.getParentJobId() == 26 && skill.getLevelMin() == 20){
                return false;
            }
            return actor.getMyJobs().getJob(skill.getParentJobId()).jobLevel >= skill.getLevelMin();
        } catch (Exception e) {
            logger.error("Enabled {} with SkillLevel {}", skill.getParentJobId(), skill.getLevelMin());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void execute(Player actor, int element) {
        if (!this.isEnabled(actor)) {
            actor.send(new BasicNoOperationMessage());
            actor.getClient().delGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        if (actor.getCurrentMap().getStatedElementById(element).elementState == 2) {
            PlayerController.sendServerMessage(actor.getClient(), "Impossible de collecter un item déjà en recolte.");
            actor.getClient().delGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            return;
        }
        actor.getCurrentMap().getStatedElementById(element).elementState = 2;
        this.ageBonus = actor.getCurrentMap().getInteractiveElementStruct(element).ageBonus;
        actor.getCurrentMap().getInteractiveElementStruct(element).ageBonus = -1;

        actor.getCurrentMap().sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(actor.getCurrentMap().toInteractiveElement(Player, element))));
        actor.getCurrentMap().sendToField(new StatedElementUpdatedMessage(actor.getCurrentMap().getStatedElementById(element)));
        new CancellableScheduledRunnable(actor.getCurrentMap().getArea().getBackGroundWorker(), this.getDuration() * 100) {
            @Override
            public void run() {
                try {
                    actor.getClient().endGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
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
        player.getCurrentMap().sendToField(new InteractiveUseEndedMessage(element, this.skill.getID()));
        int quantityGathered = EffectHelper.randomValue(player.getMyJobs().getJob(skill.getParentJobId()).quantity(skill.getLevelMin()));
        int bonusQuantity = EffectHelper.randomValue(player.getMyJobs().getJob(skill.getParentJobId()).jobEntity(skill.getLevelMin()).getBonusMin(), player.getMyJobs().getJob(skill.getParentJobId()).jobEntity(skill.getLevelMin()).getBonusMax());
        if (ageBonus > 0) {
            bonusQuantity += (int) ((float) bonusQuantity * ageBonus / 100);
        }
        InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), skill.getGatheredRessourceItem(), 63, player.getID(), bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered, EffectHelper.generateIntegerEffect(DAO.getItemTemplates().getTemplate(skill.getGatheredRessourceItem()).getPossibleEffects(), EffectGenerationType.NORMAL, false));
        if (player.getInventoryCache().add(item, true)) {
            item.setNeedInsert(true);
        }
        player.getMyJobs().getJob(skill.getParentJobId()).gatheringItems += bonusQuantity > 0 ? quantityGathered + bonusQuantity : quantityGathered;
        player.send(bonusQuantity > 0 ? new ObtainedItemWithBonusMessage(skill.getGatheredRessourceItem(), quantityGathered, bonusQuantity) : new ObtainedItemMessage(skill.getGatheredRessourceItem(), quantityGathered));
        player.getMyJobs().addExperience(player, skill.getParentJobId(), player.getMyJobs().getJob(skill.getParentJobId()).jobEntity(skill.getLevelMin()).getXpEarned() * DAO.getSettings().getIntElement("Job.Rate"));
        player.getCurrentMap().getStatedElementById(element).elementState = 1;
        player.getCurrentMap().getStatedElementById(element).deadAt = System.currentTimeMillis();
        player.getCurrentMap().getInteractiveElementStruct(element).ageBonus = -1;

        player.getCurrentMap().sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(player.getCurrentMap().toInteractiveElement(Player, element))));
        player.getCurrentMap().sendToField(new StatedElementUpdatedMessage(player.getCurrentMap().getStatedElementById(element)));

    }

    @Override
    public void abort(Player Actor, int element) {
        this.Aborted = true;
        Actor.getCurrentMap().getStatedElementById(element).elementState = 0;
        Actor.getCurrentMap().getInteractiveElementStruct(element).ageBonus = (short) this.ageBonus;

        Actor.getCurrentMap().sendToField(Player -> Player.send(new InteractiveElementUpdatedMessage(Actor.getCurrentMap().toInteractiveElement(Player, element))));
        Actor.getCurrentMap().sendToField(new StatedElementUpdatedMessage(Actor.getCurrentMap().getStatedElementById(element)));

    }

}
