package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;

/**
 * Created by Melancholia on 6/4/16.
 */
public class WeaponExoCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final StatsEnum stat,stat2;
        switch (args[0].toLowerCase()){
            case "air":
                stat = StatsEnum.DAMAGE_AIR;
                stat2 = StatsEnum.STEAL_AIR;
                break;
            case "feu":
                stat = StatsEnum.DAMAGE_FIRE;
                stat2 = StatsEnum.STEAL_AIR;
                break;
            case "terre":
                stat = StatsEnum.DAMAGE_EARTH;
                stat2 = StatsEnum.STEAL_AIR;
                break;
            case "eau":
                stat = StatsEnum.DAMAGE_WATER;
                stat2 = StatsEnum.STEAL_AIR;
                break;
            default:
                PlayerController.sendServerErrorMessage(client,"Vous devez spécifier air/eau/feu/terre");
                return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON);
        if(item == null){
            PlayerController.sendServerErrorMessage(client,"Vous ne portez pas de CaC");
            return;
        }
        final ObjectEffect damageNeutre = item.getEffect(StatsEnum.DAMAGE_NEUTRAL.value());
        final ObjectEffect volNeutre = item.getEffect(StatsEnum.STEAL_NEUTRAL.value());

        if(damageNeutre == null && volNeutre == null){
            PlayerController.sendServerErrorMessage(client,"Votre arme ne contient pas de dommage neutrals");
            return;
        }
        if(damageNeutre != null){
            damageNeutre.actionId = stat.value();
        }
        if(volNeutre != null){
            volNeutre.actionId = stat2.value();
        }
        item.getEffects$Notify();

        client.send(new ObjectModifiedMessage(item.getObjectItem()));
        PlayerController.sendServerMessage(client, "Vôtre item a été modifié avec succès");



    }

    @Override
    public boolean can(WorldClient client) {
        if(!client.canGameAction(GameActionTypeEnum.EXCHANGE)){
            return false;
        }
        return true;
    }

    @Override
    public int roleRestrained() {
        return 0;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
