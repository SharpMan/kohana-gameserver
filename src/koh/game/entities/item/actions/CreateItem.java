package koh.game.entities.item.actions;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.*;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 * Created by Melancholia on 12/12/15.
 */
public class CreateItem  extends ItemAction {

    private final boolean send;
    private final ItemTemplate templat;
    private final int count;

    public CreateItem(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.templat = DAO.getItemTemplates().getTemplate(Integer.parseInt(args[0]));
        this.count = templat.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET ? 1 : Integer.parseInt(args[1]);
        this.send = (args.length >2 && args[2].equals("1")) ? true : false;
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell))
            return false;

        if (templat == null) {
            return false;
        }

        if(count > 0) {
            InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), templat.getId(), 63, p.getID(), count, EffectHelper.generateIntegerEffect(templat.getPossibleEffects(), EffectGenerationType.NORMAL, templat instanceof Weapon));
            if (p.getInventoryCache().add(item, true)) {
                item.setNeedInsert(true);
            }
        }
        else{
            p.getInventoryCache().safeDelete(templat.getId(), -count);
        }

        if(send){
            p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, count > 0 ? 21 : 22, new String[]{String.valueOf(count >0 ? count : -count),String.valueOf(templat.getId())} ));
        }

        return true;
    }
}
