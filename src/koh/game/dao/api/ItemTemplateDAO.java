package koh.game.dao.api;

import koh.game.Logs;
import koh.game.MySQL;
import koh.game.entities.item.*;
import koh.game.entities.item.animal.FoodItem;
import koh.game.entities.item.animal.MonsterBooster;
import koh.game.entities.item.animal.Pets;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.EffectInstanceInteger;
import koh.game.utils.Settings;
import koh.game.utils.StringUtil;
import koh.patterns.services.api.Service;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static koh.game.MySQL.executeQuery;
import static koh.game.entities.item.InventoryItem.DeserializeEffects;

/**
 *
 * @author Neo-Craft
 */
public abstract class ItemTemplateDAO implements Service {

    private static final Logger logger = LogManager.getLogger(ItemTemplateDAO.class);


    public abstract void initInventoryCache(int player, Map<Integer, InventoryItem> cache, String table);
    //public abstract void distinctItems();
    //public abstract void initNextId();

    public abstract boolean create(InventoryItem item, boolean clear, String table);
    public abstract boolean save(InventoryItem item, boolean clear, String table);
    public abstract boolean delete(InventoryItem item, String table);

    //public abstract int loadAllItemTypes();
    //public abstract int loadAllItemSets();
    //public abstract int loadAllPets();
    //public abstract int loadAll();
    //public abstract int loadAllWeapons();

    public static EffectInstance[] readEffectInstance(IoBuffer buf) {
        EffectInstance[] possibleEffectstype = new EffectInstance[buf.getInt()];
        for (int i = 0; i < possibleEffectstype.length; ++i) {
            int classID = buf.getInt();
            switch (classID) {
                case 1:
                    possibleEffectstype[i] = new EffectInstance(buf);
                    break;
                case 3:
                    possibleEffectstype[i] = new EffectInstanceDice(buf);
                    break;
                case 2:
                    possibleEffectstype[i] = new EffectInstanceInteger(buf);
                    break;
                case -1431655766:
                    break;
                default:
                    logger.warn("Unknown effectInstance classId " + classID);
                    //throw new Error("Unknown classDI " + classID);
                    break;
            }
        }
        return possibleEffectstype;
    }

}
