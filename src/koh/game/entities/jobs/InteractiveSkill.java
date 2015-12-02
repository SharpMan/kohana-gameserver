package koh.game.entities.jobs;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveSkill {

    public int ID;
    public String type;
    public byte parentJobId;
    public boolean isForgemagus;
    public int[] modifiableItemTypeId;
    public int gatheredRessourceItem;
    public int craftableItemIds[];
    public int interactiveId;
    public String useAnimation;
    public int elementActionId;
    public boolean isRepair;
    public int cursor;
    public boolean availableInHouse, clientDisplay;
    public int levelMin;

}
