package koh.game.entities.actors.character;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import java.util.ArrayList;
import java.util.Iterator;
import koh.game.Main;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.StatsEnum;

public class ConditionParser_Old {

    public static boolean validConditions(Player perso, String req) {
        if ((req == null) || (req.equals(""))) {
            return true;
        }
        if (req.contains("BI")) {
            return false;
        }
        /*if (perso.get_compte().get_gmLvl() >= 5) {
         return true;
         }*/

        Jep jep = new Jep();
        if (req.contains("PO")) {
            req = havePO(req, perso, "PO");
        }
       /* if (req.contains("Po")) {
            req = havePO(req, perso, "Po");
        }
        if (req.contains("Qa")) {
            req = havePO(req, perso, "Qa");
        }*/
        req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=");
        try {
            jep.addVariable("CI", perso.stats.getTotal(StatsEnum.Intelligence));
            jep.addVariable("CV", perso.stats.getTotal(StatsEnum.Vitality));
            jep.addVariable("CA", perso.stats.getTotal(StatsEnum.Agility));
            jep.addVariable("CW", perso.stats.getTotal(StatsEnum.Wisdom));
            jep.addVariable("CC", perso.stats.getTotal(StatsEnum.Chance));
            jep.addVariable("CS", perso.stats.getTotal(StatsEnum.Strength));

            jep.addVariable("Ci", perso.stats.getBase(StatsEnum.Intelligence));
            jep.addVariable("Cs", perso.stats.getBase(StatsEnum.Strength));
            jep.addVariable("Cv", perso.stats.getBase(StatsEnum.Vitality));
            jep.addVariable("Ca", perso.stats.getBase(StatsEnum.Agility));
            jep.addVariable("Cw", perso.stats.getBase(StatsEnum.Wisdom));
            jep.addVariable("Cc", perso.stats.getBase(StatsEnum.Chance));

            jep.addVariable("Ps", perso.alignmentSide);
            jep.addVariable("Pa", perso.alignmentValue);
            jep.addVariable("PP", perso.alignmentGrade);
            jep.addVariable("PL", perso.level);
            jep.addVariable("PK", perso.kamas);
            jep.addVariable("PG", perso.breed);
            jep.addVariable("PS", perso.sexe);
            jep.addVariable("PZ", Boolean.valueOf(true));

            jep.addVariable("MiS", perso.ID);

            jep.parse(req);
            Object result = jep.evaluate();
            boolean ok = false;
            if (result != null) {
                ok = Boolean.valueOf(result.toString()).booleanValue();
            }
            return ok;
        } catch (JepException e) {
            Main.Logs().writeError("An error occurred: " + e.getMessage());
        }
        return true;
    }

    public static String havePO(String cond, Player perso, String toReplace) {
        String[] cut = cond.replaceAll("[ ()]", "").split("[|&]");

        ArrayList<Integer> value = new ArrayList<>(cut.length);

        for (String cur : cut) {
            if (!cur.contains(toReplace)) {
                continue;
            }
            if (cur.split("[=]").length < 2) {
                Main.Logs().writeError("False .. Condition " + cond);
                continue;
            }
            if (perso != null && perso.inventoryCache.hasItemId(Integer.parseInt(cur.split("[=]")[1]))) {
                value.add(Integer.valueOf(Integer.parseInt(cur.split("[=]")[1])));
            } else {
                value.add(Integer.valueOf(-1));
            }
        }
        for (Iterator<Integer> localIterator = value.iterator(); localIterator.hasNext();) {
            int curValue = (localIterator.next()).intValue();

            cond = cond.replaceFirst(toReplace, curValue + "");
        }

        return cond;
    }

}
