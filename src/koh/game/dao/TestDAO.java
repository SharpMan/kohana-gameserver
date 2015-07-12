package koh.game.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import koh.game.MySQL;
import static koh.game.entities.item.EffectHelper.SerializeEffectInstanceDice;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class TestDAO {

    //15110
    public synchronized static void SetMaxEffects(int id, EffectInstance[] Effects) {
        try {
            PreparedStatement p = MySQL.prepareQuery("UPDATE `item_pets` set max_effects = ? WHERE id = ?;", MySQL.Connection());
            p.setBytes(1, SerializeEffectInstanceDice(Effects).array());
            p.setInt(2, id);
            p.execute();

            MySQL.closePreparedStatement(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void SetEffects(int id, EffectInstance[] Effects) {
        try {

            EffectInstanceDice Effect = (EffectInstanceDice) Arrays.stream(Effects).filter(x -> x.effectId == 128).findFirst().get();
            Effects = (EffectInstance[]) ArrayUtils.removeElement(Effects, Effect);

            PreparedStatement p = MySQL.prepareQuery("UPDATE `item_templates` set possible_effects = ? WHERE id = ?;", MySQL.Connection());
            p.setBytes(1, SerializeEffectInstanceDice2(Effects));
            p.setInt(2, id);
            p.execute();

            MySQL.closePreparedStatement(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static byte[] SerializeEffectInstanceDice2(EffectInstance[] effects) {
        IoBuffer buff = IoBuffer.allocate(1);
        buff.setAutoExpand(true);

        buff.putInt(effects.length);
        for (EffectInstance e : effects) {
            ((EffectInstanceDice) e).toBinary(buff);
        }
        return buff.array();
    }

}
