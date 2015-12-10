package koh.game.entities.spells;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class LearnableSpell {

    private int ID;
    @Getter
    private int spell;
    @Getter
    private int obtainLevel;
    @Getter
    private int breedID;

    public LearnableSpell(ResultSet result) throws SQLException {
        ID = result.getInt("id");
        spell = result.getInt("spell");
        obtainLevel = result.getInt("obtain_level");
        breedID = result.getInt("breed_id");
    }
}
