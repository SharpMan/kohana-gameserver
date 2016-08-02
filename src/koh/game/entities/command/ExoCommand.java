package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.game.network.websocket.handlers.PlayerHandler;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;

/**
 * Created by Melancholia on 6/4/16.
 */
public class ExoCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }


    public static int computePrice(WorldClient client, CharacterInventoryPositionEnum slot){
        return (int) Math.abs(client.getCharacter().getInventoryCache().getItemInSlot(slot).getTemplate().getLevel() * 1.33);
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final CharacterInventoryPositionEnum pos;
        switch (args[0].toLowerCase()){
            case "coiffe":
                pos = CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT;
                break;
            case "cape":
                pos = CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE;
                break;
            default:
                PlayerController.sendServerErrorMessage(client,"Vous devez spécifier coiffe ou cape");
                return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().getItemInSlot(pos);
        if(item == null){
            PlayerController.sendServerErrorMessage(client,"Vous ne portez pas de "+args[0]);
            return;
        }
        final StatsEnum stat;
        switch (args[1].toLowerCase()){
            case "pa":
                stat = StatsEnum.ACTION_POINTS;
                break;
            case "pm":
                stat = StatsEnum.MOVEMENT_POINTS;
                break;
            case "po":
                stat = StatsEnum.ADD_RANGE;
                break;
            case "invoc":
                stat = StatsEnum.ADD_SUMMON_LIMIT;
                break;
            default:
                PlayerController.sendServerErrorMessage(client,"Vous devez spécifier pa ou pm ou po ou invoc");
                return;
        }
        if(item.hasEffect(stat.value())){
            PlayerController.sendServerErrorMessage(client,"Votre item donne déjà "+((ObjectEffectInteger)item.getEffect(stat.value())).value+" "+args[1]);
            return;
        }
        if(stat != StatsEnum.ADD_RANGE && stat != StatsEnum.ADD_SUMMON_LIMIT){
            if(item.hasEffect(StatsEnum.ACTION_POINTS.value()) || item.hasEffect(StatsEnum.MOVEMENT_POINTS.value())){
                PlayerController.sendServerErrorMessage(client,"Votre item donne déjà 1 pa ou 1 pm");
                return;
            }
        }
        final int price = computePrice(client, pos);
        final InventoryItem tokens = client.getCharacter().getInventoryCache().getItemInTemplate(13470);
        if (tokens == null || (double) tokens.getQuantity() < price) {
            PlayerController.sendServerErrorMessage(client,"Vous ne possedez pas "+price+" tokens");
            return;
        }

        item.getEffects$Notify().add(new ObjectEffectInteger(stat.value(),1));
        item.getStats().addItem(stat,1);
        client.getCharacter().getStats().addItem(stat, 1);
        client.send(new ObjectModifiedMessage(item.getObjectItem()));
        client.getCharacter().refreshStats();



        client.getCharacter().getInventoryCache().updateObjectquantity(tokens, tokens.getQuantity() - price);

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
        return 2;
    }
}
