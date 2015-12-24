package koh.game.entities.item.animal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import koh.look.EntityLookParser;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Couple;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class MountTemplate {

    @Getter
    private int Id;
    @Getter
    private EntityLook entityLook;
    @Getter
    private ArrayList<Couple<Integer, Double>> stats = new ArrayList<>();
    @Getter
    private int scroolId;

    public MountTemplate(ResultSet result) throws SQLException {
        this.Id = result.getInt("id");
        this.scroolId =result.getInt("scroll_id");
        this.entityLook = EntityLookParser.fromString(result.getString("look"));
        if (!result.getString("stats").isEmpty()) {
            for (String stat : result.getString("stats").split("\\|")) {
                String[] infos = stat.split("=");
                Couple<Integer, Double> c = new Couple<Integer, Double>(Integer.parseInt(infos[0]), (infos.length > 1 ? Double.parseDouble(infos[1]) : 0));
                stats.add(c);
            }
        }
    }
}
