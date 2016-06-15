package koh.game.entities.command;

import koh.game.network.WorldClient;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.messages.game.moderation.PopupWarningMessage;

/**
 * Created by Melancholia on 6/4/16.
 */
public class FmCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "Affiche le menu de forgemagerie";
    }



    @Override
    public void apply(WorldClient client, String[] args) {
        client.send(new PopupWarningMessage((byte) 0,"Forgemagerie", "Bienvenue sur le système de Forgemagerie\n\n\n" +
                (client.getCharacter().getInventoryCache().hasItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT) ?
                "Le cout de forgemagerie de "+ client.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT).getTemplate().getNameId() +" est de " + ExoCommand.computePrice(client,CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT) +" tokens\n" : "") +
                (client.getCharacter().getInventoryCache().hasItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE) ?
                        "Le cout de forgemagerie de "+ client.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE).getTemplate().getNameId() +" est de " + ExoCommand.computePrice(client,CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE) +" tokens\n" : "") +
                (client.getCharacter().getInventoryCache().hasItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON) ?
                "Le cout de fromagerie de "+ client.getCharacter().getInventoryCache().getItemInSlot(CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON).getTemplate().getNameId() +" est de 0 tokens\n\n" : "")+
                "Pour effectuer la forgemagerie, tapez .exo coiffe/cape pa/pm/po\n" +
                "Pour effectuer la forgemagerie de l'arme tapez .arme terre/eau/feu/air"));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 0;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}
