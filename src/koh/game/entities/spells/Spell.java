package koh.game.entities.spells;

import koh.game.dao.DAO;
import koh.game.dao.api.SpellDAO;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class Spell {

    private int id;
    private int typeId;
    private int iconId;
    private boolean verbose_casttype; //UseParamCache ?
    @Getter
    private SpellLevel spellLevels[];

    public Spell(ResultSet result) throws SQLException {
        id = result.getInt("id");
        typeId = result.getInt("type_id");
        iconId = result.getInt("icon_id");
        verbose_casttype = result.getBoolean("verbose_cast");
        spellLevels = new SpellLevel[result.getString("spell_levels").split(",").length];
        for (int i = 0; i < result.getString("spell_levels").split(",").length; i++) {
            this.spellLevels[i] = DAO.getSpells().findLevel(Integer.parseInt(result.getString("spell_levels").split(",")[i]));

        }
    }

    //public String nameIdtype, descriptionIdtype;
    //public String scriptParamstype;
    //public String scriptParamsCriticaltype;
    //public int scriptIdtype, scriptIdCriticaltype;
    public SpellLevel getSpellLevel(int Level) {
        if (Level > spellLevels.length) {
            Level = this.spellLevels.length;
        }
        return spellLevels[Level - 1];
    }
}
