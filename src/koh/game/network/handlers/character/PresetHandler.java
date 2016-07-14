package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.character.preset.PresetEntity;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.FightState;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.PresetDeleteResultEnum;
import koh.protocol.client.enums.PresetSaveResultEnum;
import koh.protocol.client.enums.PresetUseResultEnum;
import koh.protocol.messages.game.inventory.preset.*;
import koh.protocol.types.game.inventory.preset.Preset;
import koh.protocol.types.game.inventory.preset.PresetItem;

/**
 * Created by Melancholia on 6/29/16.
 */
public class PresetHandler {


    private static final byte[] EMPTY_ARRAY = new byte[0];

    @HandlerAttribute(ID = InventoryPresetUseMessage.MESSAGE_ID)
    public static void handleInventoryPresetUseMessage(WorldClient client, InventoryPresetUseMessage message){
        if((client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE)){
            client.send(new InventoryPresetUseResultMessage(message.presetId, PresetUseResultEnum.PRESET_USE_ERR_UNKNOWN, EMPTY_ARRAY));
            return;
        }
        else {
            final Preset preset = client.getCharacter().getPresets().get(message.presetId);
            if(preset == null){
                client.send(new InventoryPresetUseResultMessage(message.presetId, PresetUseResultEnum.PRESET_USE_ERR_BAD_PRESET_ID, EMPTY_ARRAY));
            }
            else{
                if(preset.mount && client.getCharacter().getMountInfo().mount != null && !client.getCharacter().getMountInfo().isToogled){
                    client.getCharacter().getMountInfo().onRiding();
                    client.getCharacter().getMountInfo().save();
                }
                InventoryItem weapon = null;
                boolean partial = false;
                for (PresetItem object : preset.objects) {
                    final InventoryItem item = client.getCharacter().getInventoryCache().find(object.objUid);
                    if(item == null){
                        partial = true;
                    }
                    else if(item.isEquiped()){
                        continue;
                    }
                    else if(item.isWeapon()){
                        weapon = item;
                    }else{
                        client.getCharacter().getInventoryCache().moveItem(object.objUid, CharacterInventoryPositionEnum.valueOf(object.position),1);
                    }
                }
                if(weapon != null){
                    client.getCharacter().getInventoryCache().moveItem(weapon.getID(), CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON,1);
                }
                client.send(new InventoryPresetUseResultMessage(message.presetId, partial ? PresetUseResultEnum.PRESET_USE_OK_PARTIAL : PresetUseResultEnum.PRESET_USE_OK, EMPTY_ARRAY));
            }
        }
    }

    @HandlerAttribute(ID = InventoryPresetDeleteMessage.MESSAGE_ID)
    public static void handleInventoryPresetDeleteMessage(WorldClient client, InventoryPresetDeleteMessage message){
        final Preset preset = client.getCharacter().getPresets().get(message.presetId);
        if(preset == null){
            client.send(new InventoryPresetDeleteResultMessage(message.presetId, PresetDeleteResultEnum.PRESET_DEL_ERR_BAD_PRESET_ID));
        }else{
            if(client.getCharacter().getPresets().remove(message.presetId, client.getCharacter().getID()))
                client.send(new InventoryPresetDeleteResultMessage(message.presetId, PresetDeleteResultEnum.PRESET_DEL_OK));
            else
                client.send(new InventoryPresetDeleteResultMessage(message.presetId, PresetDeleteResultEnum.PRESET_DEL_ERR_UNKNOWN));
        }
    }


    @HandlerAttribute(ID = InventoryPresetSaveMessage.MESSAGE_ID)
    public static void handleInventoryPresetSaveMessage(WorldClient client, InventoryPresetSaveMessage message){
        if(client.getCharacter().getInventoryCache().getEquipedItems().count() < 3){
            PlayerController.sendServerErrorMessage(client," Vous devez possessez au moins 3 items");
        }
        else if(client.getCharacter().getPresets().size() > 99){
            client.send(new InventoryPresetSaveResultMessage(message.presetId, PresetSaveResultEnum.PRESET_SAVE_ERR_TOO_MANY));
        }
        /*else if(client.getCharacter().getShortcuts().myShortcuts.size() > 98){
            client.send(new InventoryPresetSaveResultMessage(message.presetId, PresetSaveResultEnum.PRESET_SAVE_ERR_TOO_MANY));
        }*/
        else {
            final Preset preset = client.getCharacter().getPresets().get(message.presetId);
            if(preset != null){
                preset.symbolId = message.symbolId;
                preset.mount = client.getCharacter().getMountInfo().isToogled;
                //TODO clear old obj
                preset.objects = client.getCharacter().getInventoryCache().getEquipedItems()
                        .map(i -> new PresetItem((byte)i.getPosition(), i.getTemplateId(), i.getID())).toArray(PresetItem[]::new);
                final PresetEntity entity = client.getCharacter().getPresets().getEntity(message.presetId);
                entity.setInformations(preset.serializeInformations());
                client.send(new InventoryPresetUpdateMessage(preset));
                DAO.getPresets().update(client.getCharacter().getID(),entity);
            }
            else {
                final Preset obj = new Preset(message.presetId,
                        message.symbolId,
                        client.getCharacter().getMountInfo().isToogled,
                        client.getCharacter().getInventoryCache().getEquipedItems()
                                .map(i -> new PresetItem((byte) i.getPosition(), i.getTemplateId(), i.getID())).toArray(PresetItem[]::new));

                final PresetEntity entity = new PresetEntity(client.getCharacter().getID(),message.presetId,obj.serializeInformations(),obj);
                DAO.getPresets().insert(entity);

                client.getCharacter().getPresets().add(obj, entity);
                client.send(new InventoryPresetUpdateMessage(obj));
            }

            client.send(new InventoryPresetSaveResultMessage(message.presetId, PresetSaveResultEnum.PRESET_SAVE_OK));
        }
    }

}
