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

    private final static HashMap<String, StatsEnum> CRITERIONS_BINDS = new HashMap<String, StatsEnum>() {
        {
            put("CA", StatsEnum.AGILITY);
            put("CC", StatsEnum.CHANCE);
            put("CS", StatsEnum.STRENGTH);
            put("CI", StatsEnum.INTELLIGENCE);
            put("CW", StatsEnum.WISDOM);
            put("CV", StatsEnum.VITALITY);
            put("CL", StatsEnum.VITALITY); //TODO: currentLife
            put("CM", StatsEnum.MOVEMENT_POINTS);
            put("CP", StatsEnum.ACTION_POINTS);
            put("Ct", StatsEnum.ADD_TACKLE_EVADE);
            put("CT", StatsEnum.ADD_TACKLE_BLOCK);

        }
    };
    private static final HashMap<String, StatsEnum> CRITERIONS_STATS_BASE_BINDS = new HashMap<String, StatsEnum>() {
        {
            put("Ca", StatsEnum.AGILITY);
            put("Cc", StatsEnum.CHANCE);
            put("Cs", StatsEnum.STRENGTH);
            put("Ci", StatsEnum.INTELLIGENCE);
            put("Cw", StatsEnum.WISDOM);
            put("Cv", StatsEnum.VITALITY);
            //TODO additional stats
            put("ca", StatsEnum.AGILITY);
            put("cc", StatsEnum.CHANCE);
            put("cs", StatsEnum.STRENGTH);
            put("ci", StatsEnum.INTELLIGENCE);
            put("cw", StatsEnum.WISDOM);
            put("cv", StatsEnum.VITALITY);

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

    public static boolean isStatsIdentifier(String identifier) {
        return StatsCriterion.CRITERIONS_BINDS.containsKey(identifier) || StatsCriterion.CRITERIONS_STATS_BASE_BINDS.containsKey(identifier) || Arrays.stream(EXTRA_CRITERIONS).anyMatch(x -> x.equalsIgnoreCase(identifier));
    }

    @Override
    public boolean eval(Player character) {

        if (this.Field != null) {
            return this.Compare((Comparable<Integer>) (this.Base ? character.getStats().getBase(Field) : character.getStats().getTotal(Field)), this.Comparand);
        }
        //return this.Compare<Integer>((Comparable<Integer>) (this.base ? character.stats.getBase(Field) : character.stats.getTotal(Field)), this.Comparand);
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
        if (StatsCriterion.CRITERIONS_BINDS.containsKey(this.Identifier)) {
            this.Field = StatsCriterion.CRITERIONS_BINDS.get(this.Identifier);
        } else if (StatsCriterion.CRITERIONS_STATS_BASE_BINDS.containsKey(this.Identifier)) {
            this.Field = StatsCriterion.CRITERIONS_STATS_BASE_BINDS.get(this.Identifier);
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
