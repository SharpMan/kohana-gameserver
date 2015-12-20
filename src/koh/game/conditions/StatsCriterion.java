package koh.game.conditions;

import java.util.Arrays;
import java.util.HashMap;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.StatsEnum;
import java.lang.Integer;

/**
 *
 * @author Neo-Craft
 */
public class StatsCriterion extends Criterion {

    private final static HashMap<String, StatsEnum> CriterionsBinds = new HashMap<String, StatsEnum>() {
        {
            put("CA", StatsEnum.Agility);
            put("CC", StatsEnum.Chance);
            put("CS", StatsEnum.Strength);
            put("CI", StatsEnum.Intelligence);
            put("CW", StatsEnum.Wisdom);
            put("CV", StatsEnum.Vitality);
            put("CL", StatsEnum.Vitality); //TODO: currentLife
            put("CM", StatsEnum.MovementPoints);
            put("CP", StatsEnum.ActionPoints);
            put("Ct", StatsEnum.Add_TackleEvade);
            put("CT", StatsEnum.Add_TackleBlock);

        }
    };
    private static final HashMap<String, StatsEnum> CriterionsStatsBaseBinds = new HashMap<String, StatsEnum>() {
        {
            put("Ca", StatsEnum.Agility);
            put("Cc", StatsEnum.Chance);
            put("Cs", StatsEnum.Strength);
            put("Ci", StatsEnum.Intelligence);
            put("Cw", StatsEnum.Wisdom);
            put("Cv", StatsEnum.Vitality);

        }
    };

    private final static String[] EXTRA_CRITERIONS = new String[]{"Ce", "CE", "CD", "CH"};

    public String Identifier;

    public StatsEnum Field;

    public boolean Base;

    public Integer Comparand;

    public StatsCriterion(String identifier) {
        this.Identifier = identifier;
    }

    public static boolean IsStatsIdentifier(String identifier) {
        return StatsCriterion.CriterionsBinds.containsKey(identifier) || StatsCriterion.CriterionsStatsBaseBinds.containsKey(identifier) || Arrays.stream(EXTRA_CRITERIONS).anyMatch(x -> x.equalsIgnoreCase(identifier));
    }

    @Override
    public boolean eval(Player character) {

        if (this.Field != null) {
            return this.Compare((Comparable<Integer>) (this.Base ? character.getStats().getBase(Field) : character.getStats().getTotal(Field)), this.Comparand);
        }
        //return this.Compare<Integer>((Comparable<Integer>) (this.Base ? character.stats.getBase(Field) : character.stats.getTotal(Field)), this.Comparand);
        switch (this.Identifier) {
            case "Ce":
                return this.Compare((Comparable<Integer>) character.getEnergy(), this.Comparand);
            case "CE":
                //return this.Compare((Comparable<Integer>) character.EnergyMax, (Short) this.Comparand);
                return true;
            case "CD":
                return true;
            case "CH":
                return true;
            default:
                throw new Error(String.format("Cannot eval StatsCriterion {0}, {1} is not a stats identifier", this, this.Identifier));
        }
    }

    @Override
    public void Build() {
        if (StatsCriterion.CriterionsBinds.containsKey(this.Identifier)) {
            this.Field = StatsCriterion.CriterionsBinds.get(this.Identifier);
        } else if (StatsCriterion.CriterionsStatsBaseBinds.containsKey(this.Identifier)) {
            this.Field = StatsCriterion.CriterionsStatsBaseBinds.get(this.Identifier);
            this.Base = true;
        } else if (!Arrays.stream(StatsCriterion.EXTRA_CRITERIONS).anyMatch(x -> x.equalsIgnoreCase(this.Identifier))) //else if (!Enumerable.Any<string>((IEnumerable<string>) StatsCriterion.EXTRA_CRITERIONS, (Func<string, bool>) (entry => entry == this.IDENTIFIER)))
        {
            throw new Error(String.format("Cannot build StatsCriterion, {0} is not a stats identifier", this.Identifier));
        }
        try {
            this.Comparand = Integer.parseInt(literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build StatsCriterion, {0} is not a valid integer", this.literal));
        }
    }

    @Override
    public String toString() {
        return this.FormatToString(this.Identifier);
    }

}
