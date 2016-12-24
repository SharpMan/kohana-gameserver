package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemTemplate;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 8/8/16.
 */
public class ItemCommand implements PlayerCommand {

    private Map<Integer, ArrayList<Long>> spawns = new HashMap<>();


    @Override
    public String getDescription() {
        return "Add item arg1 quantity arg2 type arg3 to me";
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        int id = Integer.parseInt(args[0]),
                qua = Integer.parseInt(args[1]);
        EffectGenerationType type = EffectGenerationType.NORMAL;
        if (args[2].equalsIgnoreCase("max"))
            type = EffectGenerationType.MAX_EFFECTS;
        else if (args[2].equalsIgnoreCase("min"))
            type = EffectGenerationType.MIN_EFFECTS;



        if ((id == 13470 || id == 12736 || id == 11792 || id == 11563 || id == 958) && client.getAccount().accountData.right < 5)
            id = 8876;

        final ItemTemplate template = DAO.getItemTemplates().getTemplate(id);

        if (template == null){
            client.send(new ConsoleMessage((byte) 0, "Inexistant item"));
            return;
        }
        if(client.getAccount().accountData.right < 6 && (template.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_DOFUS || template.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_SHIELD)){
            client.send(new ConsoleMessage((byte) 0, "Dofus forbidden"));
            return;
        }
        if (template.getTypeId() != 76)
            qua = 1;



        if(!spawns.containsKey(client.getAccount().getId())){
            spawns.put(client.getAccount().getId() , new ArrayList<>());
        }

        if(spawns.get(client.getAccount().getId()).stream().filter(x ->  System.currentTimeMillis() < x + (24* 360 * 1000)).count() > 15){
            return;
        }

        if (template.getTypeId() == 76){
            if(qua > 100){
                qua = 100;
            }
            if(qua > 10){
                final int res = qua / 10;
                for(int i = 0; i < res; i++){
                    spawns.get(client.getAccount().getId()).add(System.currentTimeMillis());
                }
            }
            else spawns.get(client.getAccount().getId()).add(System.currentTimeMillis());
        }
        else
            spawns.get(client.getAccount().getId()).add(System.currentTimeMillis());

        final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), id, 63, client.getCharacter().getID(), qua, EffectHelper.generateIntegerEffect(template.getPossibleEffects(), type, template.isWeapon()));
        if (client.getCharacter().getInventoryCache().add(item, true))
            item.setNeedInsert(true);

        client.send(new ConsoleMessage((byte)0, String.format("%s  added to your inventory with %s stats", template.getNameId(), type.toString())));

    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 3;
    }

    @Override
    public int argsNeeded() {
        return 3;
    }
}
